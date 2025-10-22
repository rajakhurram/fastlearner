export const environment = {
  production: true,
  isProductionServer: true,

  basePath: `http://localhost`,
  loginUrl: `http://localhost:8443/auth`,
  baseUrl: `http://localhost:8443/api/v1/`,
  videoUrl: `http://localhost:8443`,
  imageUrl: `http://localhost:8443`,
  applicationCourseContentUrl: `http://localhost/student/course-content/`,
  applicationCourseDetailsUrl: `http://localhost/student/course-details/`,

  // Goolge Login Credentials
  googleClientId: '{{google-client-id}}',
  googleSecretKey: '{{google-secret-key}}',
};
