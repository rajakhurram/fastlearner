export const environment = {
  production: true,
  isProductionServer: true,

  basePath: `http://qualitycenter.fastlearner.ai`,
  loginUrl: `http://qualitycenter.fastlearner.ai:8443/api/v1/auth`,
  baseUrl: `http://qualitycenter.fastlearner.ai:8443/api/v1/`,
  videoUrl: `http://qualitycenter.fastlearner.ai:8443`,
  imageUrl: `http://qualitycenter.fastlearner.ai:8443`,
  applicationCourseContentUrl: `http://qualitycenter.fastlearner.ai/student/course-content/`,
  applicationCourseDetailsUrl: `http://qualitycenter.fastlearner.ai/student/course-details/`,

  googleClientId: '{{google-client-id}}',
  googleSecretKey: '{{google-secret-key}}',
};
