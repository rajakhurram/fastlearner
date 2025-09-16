import os
from flask import jsonify, make_response
import whisper
from http import HTTPStatus

import Service.chatBot as chat

import errorHandler


class GenerateTranscriptService:
    # Get current directory
    _path = os.path.normpath(os.getcwd())

    _ALLOWED_EXTENSIONS = {'mp4', 'mp3', 'avi'}
    _ALLOWED_DOC_EXTENSIONS = {'pdf'}

    def allowed_file(self, filename):
        return '.' in filename and filename.rsplit('.', 1)[1].lower() in self._ALLOWED_EXTENSIONS

    def allowed_doc_file(self, filename):
        return '.' in filename and filename.rsplit('.', 1)[1].lower() in self._ALLOWED_DOC_EXTENSIONS

    def save_file(self, file):
        # validate the file format
        if not self.allowed_file(filename=file.filename):
            return False
        filename = 'video.mp4'
        file.save(filename)
        return True

    def save_doc(self, file):
        if not self.allowed_doc_file(filename=file.filename):
            return False
        filename = 'doc.pdf'
        file.save(filename)
        return True

    def remove_files(self):
        if os.path.exists(self._path + "/transcript.txt"):
            os.remove(self._path + "/transcript.txt")
        if os.path.exists(self._path + "/video.mp4"):
            os.remove(self._path + "/video.mp4")
        if os.path.exists(self._path + "/doc.pdf"):
            os.remove(self._path + "/doc.pdf")
        if os.path.exists(self._path + "/input_text.txt"):
            os.remove(self._path + "/input_text.txt")

    def write_text_to_file(self, text, filename):
        with open(filename, 'w') as file:
            file.write(text)

    def get_summary(self, duration, transcript_text):
        try:
            filename = "input_text.txt"
            self.write_text_to_file(text=transcript_text, filename=filename)

            summary = chat.ChatBot().ask("Provide me with the summary of 100 words", filename=filename)
            self.remove_files()
        except Exception as e:
            return errorHandler.ErrorHandler().handle_internal_server_request(message=str(e))

        return make_response(
            jsonify({
                'message': "Transcript and Summary Generated Successfully",
                'status': HTTPStatus.OK.value,
                'code': HTTPStatus.OK.phrase,
                'data': {
                    "transcript": transcript_text,
                    "summary": str(summary),
                    "duration": duration
                }
            }), HTTPStatus.OK)

    def split_text_into_chunks(self, text, max_tokens=6000):
        words = text.split()
        chunks = []
        current_chunk = []
        current_length = 0

        for word in words:
            current_length += len(word) + 1  # +1 for the space
            if current_length > max_tokens:
                chunks.append(' '.join(current_chunk))
                current_chunk = []
                current_length = len(word) + 1
            current_chunk.append(word)

        if current_chunk:
            chunks.append(' '.join(current_chunk))

        return chunks

    def get_transcript(self, multipartFile):
        # save video from multipart request
        if not self.save_file(file=multipartFile):
            return errorHandler.ErrorHandler().handle_bad_request(message="Incorrect File Format")

        try:
            model = whisper.load_model('base.en')
            whisper.DecodingOptions(language='en', fp16=False)
            try:
                # Transcribe the audio
                transcript = model.transcribe(f"{self._path}/video.mp4", task="translate")
            except Exception as e:
                return make_response(
                    jsonify({
                        'message': "Failed to process the audio. Ensure the video contains an audio stream.",
                        'status': HTTPStatus.BAD_REQUEST.value,
                        'code': HTTPStatus.BAD_REQUEST.phrase
                    }), HTTPStatus.BAD_REQUEST)

            # Split transcript into chunks
            chunks = self.split_text_into_chunks(transcript["text"], max_tokens=6000)

            # Create a file for the full transcript
            file_name = "transcript.txt"
            with open(file_name, "w") as file:
                file.write(str(transcript["text"]).strip())

            # Generate VTT content from the transcript
            vtt_content = self.generate_vtt_from_transcript(transcript)

            # Save the VTT content to a file
            vtt_file_name = "transcript.vtt"
            with open(vtt_file_name, "w") as vtt_file:
                vtt_file.write(vtt_content)

            # Summarize each chunk and collect the summaries
            summaries = []
            for i, chunk in enumerate(chunks):
                chunk_filename = f"transcript_chunk_{i + 1}.txt"
                with open(chunk_filename, "w") as file:
                    file.write(chunk.strip())

                response = chat.ChatBot().ask("Provide me with the summary of 100 words", filename=chunk_filename)
                summary = response.response
                summaries.append(summary)

                # Remove the chunk file after processing
                os.remove(chunk_filename)

            # Combine all summaries
            combined_summary = " ".join(summaries)

        except Exception as e:
            return errorHandler.ErrorHandler().handle_internal_server_request(message=str(e))

        return make_response(
            jsonify({
                'message': "Transcript and Summaries Generated Successfully",
                'status': HTTPStatus.OK.value,
                'code': HTTPStatus.OK.phrase,
                'data': {
                    "transcript": transcript["text"],
                    "vttContent": vtt_content,
                    "summary": str(combined_summary)
                }
            }), HTTPStatus.OK)

    def get_doc_summary(self, multipartFile):
        # save video from multipart request
        if not self.save_doc(file=multipartFile):
            return errorHandler.ErrorHandler().handle_bad_request(message="Incorrect File Format")

        try:
            summary = chat.ChatBot().ask("Provide me with the summary of 100 words", filename="doc.pdf")
            self.remove_files()
        except Exception as e:
            return errorHandler.ErrorHandler().handle_internal_server_request(message=str(e))

        return make_response(
            jsonify({
                'message': "Document Summary Generated Successfully",
                'status': HTTPStatus.OK.value,
                'code': HTTPStatus.OK.phrase,
                'data': {
                    "summary": str(summary)
                }
            }), HTTPStatus.OK)

    def generate_vtt_from_transcript(self, transcript):
        """
        Helper function to generate VTT content from a transcript.
        """
        vtt_content = "WEBVTT\n\n"

        for segment in transcript['segments']:
            start_time = segment['start']
            end_time = segment['end']
            text = segment['text']

            # Convert times to VTT format (HH:MM:SS.mmm)
            start_time_str = "{:02}:{:02}:{:06.3f}".format(
                int(start_time // 3600),
                int((start_time % 3600) // 60),
                start_time % 60
            )
            end_time_str = "{:02}:{:02}:{:06.3f}".format(
                int(end_time // 3600),
                int((end_time % 3600) // 60),
                end_time % 60
            )

            # Add subtitle entry to the VTT content
            vtt_content += f"{start_time_str} --> {end_time_str}\n{text.strip()}\n\n"

        return vtt_content
