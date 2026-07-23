import {
  localApiBaseUrlPath,
} from 'src/app/core/constants/http.constants';

export const environment = {
  production: false,
  isProductionServer: false,

  // Local server IP
  basePath: `http://34.134.191.51`,
  loginUrl: `http://34.134.191.51:8443/auth`,
  baseUrl: `http://34.134.191.51:8443/api/v1/`,
  videoUrl: `http://34.134.191.51:8443`,
  imageUrl: `http://34.134.191.51:8443`,
  graderServiceBasePath: `http://34.134.191.51:8443`,
  applicationCourseContentUrl: `http://34.134.191.51/student/course-content/`,
  applicationCourseDetailsUrl: `http://34.134.191.51/student/course-details/`,

  // Goolge Login Credentials
  googleClientId:
    '{{google-client-id}}',
  googleSecretKey: '{{google-secret-key}}',
};
