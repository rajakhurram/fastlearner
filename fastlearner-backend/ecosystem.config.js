// ecosystem.config.js
module.exports = {
  apps : [{
    name   : "fastlearner",
    script : "/usr/lib/jvm/java-17-openjdk-amd64/bin/java", // Path to your Java executable
    args   : [
        "-jar",
        "/var/lib/jenkins/workspace/fl-backend-os/target/FastLearner-0.0.1-SNAPSHOT.jar"
    ],
    // Define the environment variables here.
    // PM2 will pick these up from the shell it was launched from.
    env: {
      NODE_ENV: "test", // Example other env var
      GOOGLE_APPLICATION_CREDENTIALS: process.env.GOOGLE_APPLICATION_CREDENTIALS,
      DB_USERNAME: process.env.DB_USERNAME, // This tells PM2 to take the value from its launching environment
      DB_PASSWORD: process.env.DB_PASSWORD, // Same here

      JWT_SECRET: process.env.JWT_SECRET,
      JWT_EXPIRATION: process.env.JWT_EXPIRATION,
      JWT_REFRESH_EXPIRATION: process.env.JWT_REFRESH_EXPIRATION,
      JWT_ENCRYPTION_KEY: process.env.JWT_ENCRYPTION_KEY,

      MAIL_USERNAME: process.env.MAIL_USERNAME,
      MAIL_PASSWORD: process.env.MAIL_PASSWORD,

      OTP_EXPIRY: process.env.OTP_EXPIRY,
      USER_SESSION_EXPIRY: process.env.USER_SESSION_EXPIRY,

      OPENAI_API_KEY: process.env.OPENAI_API_KEY,

      TRANSCRIPT_AUTH_KEY: process.env.TRANSCRIPT_AUTH_KEY,

      ES_USERNAME: process.env.ES_USERNAME,
      ES_PASSWORD: process.env.ES_PASSWORD,

      GCP_PROJECT_ID: process.env.GCP_PROJECT_ID,
      GCP_BUCKET_URL: process.env.GCP_BUCKET_URL,
      GCP_BUCKET_NAME: process.env.GCP_BUCKET_NAME,

      RABBITMQ_USERNAME: process.env.RABBITMQ_USERNAME,
      RABBITMQ_PASSWORD: process.env.RABBITMQ_PASSWORD,

      GOOGLE_CLIENT_ID: process.env.GOOGLE_CLIENT_ID,
      ANDROID_GOOGLE_CLIENT_ID: process.env.ANDROID_GOOGLE_CLIENT_ID,
      IOS_GOOGLE_CLIENT_ID: process.env.IOS_GOOGLE_CLIENT_ID,

      YOUTUBE_API_KEY: process.env.YOUTUBE_API_KEY,

      STRIPE_SECRET_KEY: process.env.STRIPE_SECRET_KEY,

      PAYMENT_API_KEY: process.env.PAYMENT_API_KEY,

    },
  }]
};