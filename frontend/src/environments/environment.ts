export const environment = {
  production: true,
  isProductionServer: true,

  basePath: `http://136.119.202.42`,
  loginUrl: `http://136.119.202.42:8443/auth`,
  baseUrl: `http://136.119.202.42:8443/api/v1/`,
  videoUrl: `http://136.119.202.42:8443`,
  imageUrl: `http://136.119.202.42:8443`,
  applicationCourseContentUrl: `http://136.119.202.42:8443/student/course-content/`,
  applicationCourseDetailsUrl: `http://136.119.202.42:8443/student/course-details/`,

  // Goolge Login Credentials
  googleClientId: '{{google-client-id}}',
  googleSecretKey: '{{google-secret-key}}',
};
