import os
import dto.ChatRequest as obj

import Service.chatBot as chatService
import Service.GenerateTranscript as script

import errorHandler

from flask import Flask, request, abort, jsonify


from custom_pytube import YouTube

from custom_pytube.innertube import _default_clients
from youtube_transcript_api import YouTubeTranscriptApi
from youtube_transcript_api.formatters import TextFormatter

_default_clients["ANDROID"]["context"]["client"]["clientVersion"] = "19.08.35"
_default_clients["IOS"]["context"]["client"]["clientVersion"] = "19.08.35"
_default_clients["ANDROID_EMBED"]["context"]["client"]["clientVersion"] = "19.08.35"
_default_clients["IOS_EMBED"]["context"]["client"]["clientVersion"] = "19.08.35"
_default_clients["IOS_MUSIC"]["context"]["client"]["clientVersion"] = "6.41"
_default_clients["ANDROID_MUSIC"] = _default_clients["ANDROID_CREATOR"]

from Constants import GENERATE_TRANSCRIPT, CHAT, ARTICLE_SUMMARY, DOC_SUMMARY, YOUTUBE_DOWNLOADER
from dotenv import load_dotenv


load_dotenv()


def create_app():
    return Flask(__name__)


app = create_app()

app.app_context().push()


@app.route(YOUTUBE_DOWNLOADER, methods=['POST'])
def download_video():
    try:
        authenticate(request=request)
    except Exception as e:
        return errorHandler.ErrorHandler().handle_unauthorized_request(message=str(e))

    data = request.json
    if 'url' not in data:
        return jsonify({'error': 'URL is required'}), 400
    url = data['url']
    try:
        yt = YouTube(url)
        print("URL: " + url)
        video_id = yt.video_id

        # Fetch transcript
        transcript_list = YouTubeTranscriptApi.get_transcript(video_id, languages=['en'])

        # Optional: Format transcript to plain text
        formatter = TextFormatter()
        transcriptx_text = formatter.format_transcript(transcript_list)

        return jsonify({'transcript': transcriptx_text})
    except Exception as e:
        return jsonify({'error': str(e)}), 500


@app.route(GENERATE_TRANSCRIPT, methods=['POST'])
def generate_transcript():
    try:
        authenticate(request=request)
        file = request.files['file']
        if not file.filename:
            return errorHandler.ErrorHandler().handle_bad_request(message="File is missing")
    except Exception as e:
        if e.code == 401:
            return errorHandler.ErrorHandler().handle_unauthorized_request(message=str(e))
        else:
            return errorHandler.ErrorHandler().handle_bad_request(message=str(e))

    return script.GenerateTranscriptService().get_transcript(multipartFile=request.files['file'])


@app.route(CHAT, methods=['POST'])
def chat():
    try:
        authenticate(request=request)
        data = request.get_json()
    except Exception as e:
        if e.code == 401:
            return errorHandler.ErrorHandler().handle_unauthorized_request(message=str(e))
        else:
            return errorHandler.ErrorHandler().handle_bad_request(message=str(e))

    charRequestDTO = obj.ChatRequest(data["transcript"], data["question"])
    return chatService.ChatBot().chat(request_dto=charRequestDTO, file_type="TRANSCRIPT")


@app.route(ARTICLE_SUMMARY, methods=['POST'])
def article_summary():
    try:
        authenticate(request=request)
        data = request.get_json()
    except Exception as e:
        if e.code == 401:
            return errorHandler.ErrorHandler().handle_unauthorized_request(message=str(e))
        else:
            return errorHandler.ErrorHandler().handle_bad_request(message=str(e))

    return chatService.ChatBot().chat(request_dto=data["article"], file_type="ARTICLE")


@app.route(DOC_SUMMARY, methods=['POST'])
def document_summary():
    try:
        authenticate(request=request)
        file = request.files['file']
        if not file.filename:
            return errorHandler.ErrorHandler().handle_bad_request(message="File is missing")
    except Exception as e:
        if e.code == 401:
            return errorHandler.ErrorHandler().handle_unauthorized_request(message=str(e))
        else:
            return errorHandler.ErrorHandler().handle_bad_request(message=str(e))

    return script.GenerateTranscriptService().get_doc_summary(multipartFile=request.files['file'])


def authenticate(request):
    try:
        api_key = request.headers.get('Authorization')
        if api_key is None or not api_key.__eq__(str(os.environ.get('API_KEY'))):
            abort(401)
    except:
        abort(401)


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=False)
