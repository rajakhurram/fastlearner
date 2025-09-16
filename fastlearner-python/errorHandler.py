from http.client import BAD_REQUEST, INTERNAL_SERVER_ERROR, UNAUTHORIZED, NOT_FOUND
from flask import make_response, jsonify
from http import HTTPStatus


class ErrorHandler:

    @staticmethod
    def init_app(app):
        @app.errorhandler(BAD_REQUEST)
        def handle_bad_request(e=None, message="Bad request"):
            return make_response(
                jsonify({
                    'message': str(message),
                    'status': HTTPStatus.BAD_REQUEST.value,
                    'code': HTTPStatus.BAD_REQUEST.phrase
                }), BAD_REQUEST)

        @app.errorhandler(INTERNAL_SERVER_ERROR)
        def handle_internal_server_request(e=None, message="Internal Server Error"):
            return make_response(
                jsonify({
                    'message': str(message),
                    'status': HTTPStatus.INTERNAL_SERVER_ERROR.value,
                    'code': HTTPStatus.INTERNAL_SERVER_ERROR.phrase
                }), INTERNAL_SERVER_ERROR)

        @app.errorhandler(UNAUTHORIZED)
        def handle_unauthorized_request(e=None, message="Unauthorized"):
            return make_response(
                jsonify({
                    'message': str(message),
                    'status': HTTPStatus.UNAUTHORIZED.value,
                    'code': HTTPStatus.UNAUTHORIZED.phrase
                }), UNAUTHORIZED)

        @app.errorhandler(NOT_FOUND)
        def handle_not_found_request(e=None, message="Not Found"):
            return make_response(
                jsonify({
                    'message': str(message),
                    'status': HTTPStatus.NOT_FOUND.value,
                    'code': HTTPStatus.NOT_FOUND.phrase
                }), NOT_FOUND)
