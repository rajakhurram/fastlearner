import os
from http import HTTPStatus

from llama_index import GPTVectorStoreIndex
from llama_index import SimpleDirectoryReader
from flask import jsonify, make_response

import errorHandler


class ChatBot:
    # Get current directory
    _path = os.path.normpath(os.getcwd())

    def ask(self, question, filename):
        documents = SimpleDirectoryReader(input_files=[f'{self._path}/{filename}']).load_data()

        index = GPTVectorStoreIndex(documents)
        query_engine = index.as_query_engine()
        response = query_engine.query(f"{question}")

        return response

    def save_file(self, data, filename):
        with open(filename, "w") as file:
            # Write to the file
            file.write(data)

    def chat(self, request_dto, file_type):
        if file_type == "ARTICLE":
            self.save_file(data=request_dto, filename="article.txt")
            question = "Give me the summary"
            filename = "article.txt"
        else:
            transcript = request_dto.transcript
            question = request_dto.question
            # save the text in the .txt file
            self.save_file(data=transcript, filename="transcript.txt")
            filename = "transcript.txt"

        try:
            response = self.ask(question, filename)
            if os.path.exists(self._path + filename):
                os.remove(self._path + filename)
        except Exception as e:
            return errorHandler.ErrorHandler().handle_internal_server_request(message=str(e))

        if file_type == "ARTICLE":
            return make_response(
                jsonify({
                    'message': "Answered Successfully",
                    'status': HTTPStatus.OK.value,
                    'code': HTTPStatus.OK.phrase,
                    'data': {
                        "summary": str(response)
                    }
                }), HTTPStatus.OK)
        else:
            return make_response(
                jsonify({
                    'message': "Answered Successfully",
                    'status': HTTPStatus.OK.value,
                    'code': HTTPStatus.OK.phrase,
                    'data': {
                        "answer": str(response)
                    }
                }), HTTPStatus.OK)
