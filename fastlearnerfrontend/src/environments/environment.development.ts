import {
  localApiBaseUrlPath,
} from 'src/app/core/constants/http.constants';

export const environment = {
  production: false,
  isProductionServer: false,

  // Local server IP
  basePath: `${localApiBaseUrlPath}/`,
  loginUrl: `${localApiBaseUrlPath}:8443/auth`,
  baseUrl: `${localApiBaseUrlPath}:8443/api/v1/`,
  videoUrl: `${localApiBaseUrlPath}:8443`,
  imageUrl: `${localApiBaseUrlPath}:8443`,
  applicationCourseContentUrl: `${localApiBaseUrlPath}/student/course-content/`,
  applicationCourseDetailsUrl: `${localApiBaseUrlPath}/student/course-details/`,

  // Goolge Login Credentials
  googleClientId:
    '{{google-client-id}}',
  googleSecretKey: '{{google-secret-key}}',
};
