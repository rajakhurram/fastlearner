export const environment = {
  production: true,
  isProductionServer: true,

  basePath: `http://34.9.170.10`,
  loginUrl: `http://34.9.170.10:8443/auth`,
  baseUrl: `http://34.9.170.10:8443/api/v1/`,
  videoUrl: `http://34.9.170.10:8443`,
  imageUrl: `http://34.9.170.10:8443`,
  applicationCourseContentUrl: `http://34.9.170.10:8443/student/course-content/`,
  applicationCourseDetailsUrl: `http://34.9.170.10:8443/student/course-details/`,

  // Goolge Login Credentials
  googleClientId: '{{google-client-id}}',
  googleSecretKey: '{{google-secret-key}}',
};
