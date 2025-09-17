export class HttpConstants {
  public REQUEST_STATUS = {
    SUCCESS_200: {
      CODE: 200,
      MESSAGE: 'Success!',
    },
    CREATED_201: {
      CODE: 201,
      MESSAGE: 'Created!',
    },
    BAD_REQUEST_400: {
      CODE: 400,
      MESSAGE: 'Bad Request!',
    },
    UNAUTHORIZED_401: {
      CODE: 401,
      MESSAGE: 'Unauthorized!',
    },
    REQUEST_NOT_FOUND_404: {
      CODE: 404,
      MESSAGE: 'No data found',
    },
    ALREADY_EXIST_302: {
      CODE: 302,
      MESSAGE: 'Already Exists!',
    },
    SERVER_ERROR_500: {
      CODE: 500,
      MESSAGE: 'Server Error!',
    },
    ALREADY_LOGGED_IN_409: {
      CODE: 409,
      MESSAGE: 'Already Logged In!',
    },
    NO_DATA_FOUND: {
      CODE: 202,
      MESSAGE: 'No data found',
    },
    CONFLICT_409: {
      CODE: 409,
      MESSAGE: 'Conflict Occured',
    },
  };
  static REQUEST_STATUS: any;
  constructor() {}
}

export const localApiBaseUrlPath = 'http://localhost';
export const googleTrackingId = '{{google-tracking-id}}';
export const googleTagId = '{{google-tag-id}}';
