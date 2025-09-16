import {
  localApiBaseUrlPath,
  stagingApiBaseUrlPath,
} from 'src/app/core/constants/http.constants';

export const environment = {
  production: false,
  isProductionServer: false,

  // Local server IP
  basePath: `${stagingApiBaseUrlPath}/`,
  loginUrl: `${stagingApiBaseUrlPath}:8443/auth`,
  baseUrl: `${stagingApiBaseUrlPath}:8443/api/v1/`,
  videoUrl: `${stagingApiBaseUrlPath}:8443`,
  imageUrl: `${stagingApiBaseUrlPath}:8443`,
  graderServiceBasePath: `${stagingApiBaseUrlPath}:8444`,
  applicationCourseContentUrl: `${stagingApiBaseUrlPath}/student/course-content/`,
  applicationCourseDetailsUrl: `${stagingApiBaseUrlPath}/student/course-details/`,


  // // Facebook Credentials
  // facebookAppId: '950942806671676',
  // facebookSecretKey: 'a71eafd39ead80407b0ded56c2ae8b44',

  // Goolge Credentials Production
  // googleClientId:
  //   '903858014561-71mvucn7hhiba6ls6mc6lhhe9bhd064l.apps.googleusercontent.com',
  // googleSecretKey: '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',

  // Goolge Credentials Staging
  googleClientId:
    '{{google-client-id}}',
  googleSecretKey: '{{google-secret-key}}',
};
