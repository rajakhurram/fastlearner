# 🔑 Jenkins Credentials Configuration Guide

This document provides the required credentials to configure the Jenkins server for **FastLearner AI**.  
Each credential should be securely stored in Jenkins under:

**Manage Jenkins → Credentials → (System) → Global Credentials**

⚠️ **Important:**
- Do not hardcode credentials in code or configuration files.
- Always use Jenkins credentials binding.

---

## 1. JWT (Authentication Tokens)

| ID                     | Description                     | Value Type  | Example                                |
|------------------------|---------------------------------|-------------|----------------------------------------|
| `JWT_SECRET`           | Secret key for JWT signing      | Secret Text | `your_jwt_secret_here`                 |
| `JWT_ENCRYPTION_KEY`   | Key used for JWT encryption     | Secret Text | `your_jwt_encryption_key`              |
| `JWT_REFRESH_EXPIRATION` | Refresh token expiry duration | Secret Text | `86400000` (in ms → 1 day)             |
| `JWT_EXPIRATION`       | Access token expiry duration    | Secret Text | `3600000` (in ms → 1 hour)             |

---

## 2. Mail Service

| ID              | Description                     | Value Type           | Example                  |
|-----------------|---------------------------------|----------------------|--------------------------|
| `MAIL_USERNAME` | SMTP username (email)           | Secret Text          | `your_email@gmail.com`   |
| `MAIL_PASSWORD` | SMTP password / App password    | Secret Text          | `your_app_password`      |
| `AG_MAIL_CRED`  | Application mail credentials    | Username with Password | `xyz@gmail.com / ******` |

---

## 3. User & Session Configurations

| ID                  | Description                  | Value Type  | Example            |
|---------------------|------------------------------|-------------|--------------------|
| `OTP_EXPIRY`        | OTP expiry duration (in ms)  | Secret Text | `300000` (5 mins)  |
| `USER_SESSION_EXPIRY` | User session expiry duration | Secret Text | `86400000` (1 day) |

---

## 4. OpenAI / NLP Services

| ID                  | Description                     | Value Type  | Example             |
|---------------------|---------------------------------|-------------|---------------------|
| `OPENAI_API_KEY`    | API key for OpenAI services     | Secret Text | `sk-***************`|
| `TRANSCRIPT_AUTH_KEY` | API key for transcript service | Secret Text | `your_transcript_key` |

---

## 5. Google Cloud Platform (GCP)

| ID                  | Description             | Value Type  | Example                                      |
|---------------------|-------------------------|-------------|----------------------------------------------|
| `GCP_PROJECT_ID`    | Project ID              | Secret Text | `fastlearner-12345`                          |
| `GCP_BUCKET_URL`    | Storage bucket URL      | Secret Text | `https://storage.googleapis.com/your-bucket` |
| `GCP_BUCKET_NAME`   | Storage bucket name     | Secret Text | `your_bucket_name`                           |
| `GCP_SERVICE_ACCOUNT` | Service account key file | Secret File | `xyz-429d9c0edfed.json`                      |

---

## 6. Google OAuth Clients

| ID                        | Description            | Value Type  | Example                                   |
|---------------------------|------------------------|-------------|-------------------------------------------|
| `GOOGLE_CLIENT_ID`        | Web client ID          | Secret Text | `1234567890-xxxx.apps.googleusercontent.com` |
| `ANDROID_GOOGLE_CLIENT_ID`| Android OAuth client ID| Secret Text | `1234567890-android.apps.googleusercontent.com` |
| `IOS_GOOGLE_CLIENT_ID`    | iOS OAuth client ID    | Secret Text | `1234567890-ios.apps.googleusercontent.com` |

---

## 7. YouTube / Payment / Stripe

| ID                | Description         | Value Type  | Example                  |
|-------------------|---------------------|-------------|--------------------------|
| `YOUTUBE_API_KEY` | YouTube API Key     | Secret Text | `AIza************`       |
| `PAYMENT_API_KEY` | Payment API Key     | Secret Text | `your_payment_api_key`   |
| `STRIPE_SECRET_KEY` | Stripe Secret Key | Secret Text | `sk_live_********`       |

---

## 8. Database & Messaging

| ID                  | Description              | Value Type             | Example                      |
|---------------------|--------------------------|------------------------|------------------------------|
| `DB_CREDENTIALS`    | PostgreSQL credentials   | Username with Password | `postgres / ********`        |
| `RABBIT_MQ_CRED`    | RabbitMQ credentials     | Username with Password | `admin / ********`           |
| `ELASTIC_SEARCH_CRED` | Elasticsearch credentials | Username with Password | `elastic / ********`         |

---

# ✅ Notes
1. Store each credential exactly as per its **ID** (so Jenkins pipelines can fetch them easily).
2. Use **Jenkins Credentials Binding Plugin** to inject credentials into environment variables in pipelines.
3. Never expose these values in logs or commits.  