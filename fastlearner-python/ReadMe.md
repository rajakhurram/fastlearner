# 🚀 FastLearner Python (Main FastLearner Service)

**It is an helper service for our main FastLearner backend app. In this service we are fetching video's transcript, summary and also build a chatbot.**

Welcome to the project! This guide will walk you through setting up the environment, installing dependencies, and getting the application up and running.

---

## ⚙️ System Requirements

* **Operating System**: Ubuntu 18.04 or newer (for Linux users)
* **Python**: Version 3.9 or higher
* **Version Control**: Git

---

## 💻 Local Setup

### Python Environment

1.  **Create a virtual environment**: This is a best practice to manage dependencies and avoid conflicts with your system's Python packages.
    ```bash
    python3 -m venv venv
    ```
2.  **Activate the virtual environment**:
    * **Linux/macOS**:
        ```bash
        source venv/bin/activate
        ```
    * **Windows**:
        ```bash
        .\venv\Scripts\activate
        ```
3.  **Install required Python packages**:
    ```bash
    pip install -r requirements.txt
    ```
4.  **Deactivate the environment** when you're done:
    ```bash
    deactivate
    ```

### Dependency Installation

* **Ubuntu**:
  Update your package list and install the necessary libraries for video processing and database connections.
    ```bash
    sudo apt update
    sudo apt install ffmpeg
    sudo apt install libpq-dev
    sudo apt install python3-pip
    ```
* **Whisper Library**: Install the Whisper library directly from the OpenAI GitHub repository.
    ```bash
    pip install git+[https://github.com/openai/whisper.git](https://github.com/openai/whisper.git)
    ```
* **FFmpeg (Windows, Optional)**:
  If you're on Windows, you'll need to install FFmpeg separately.
    1.  **Download FFmpeg** from the [official website](https://ffmpeg.org/download.html).
    2.  **Extract** the files and **add the `bin` directory to your system's `PATH`** environment variable. For example: `C:\ffmpeg-xxxx\bin`.

---

## 🔑 API Key Setup

API keys are required for the application to function correctly. These keys should be stored as environment variables.

1.  **Locate your keys** in the `secret.txt` file (if provided).
2.  **Set the environment variables**. The recommended method is to add them to your shell profile file (`~/.bashrc` on Linux) to ensure they persist across sessions.
    ```bash
    # Open the file in a text editor
    nano ~/.bashrc

    # Add these lines to the end of the file
    export API_KEY="your_api_key_here" // This key is set because we want to get request only the registered fastlearner backend clients so you can set your own
    export OPENAI_API_KEY="your_openai_key_here"
    export YOUTUBE_API_KEY="your_youtube_client_key_here"
    export YOUTUBE_SECRET_KEY="your_youtube_secret_key_here" 

    # Save and exit, then reload your shell
    source ~/.bashrc
    ```
3.  **Verify** the keys are set correctly:
    ```bash
    echo $API_KEY
    echo $OPENAI_API_KEY
    ```

---

## 🐳 Docker

### Build and Push Image

1.  **Build the Docker image**:
    ```bash
    docker build -t fastlearner-python-img .
    ```
2.  **Check your images**:
    ```bash
    docker images
    ```
3.  **Tag and push to Docker Hub** (if you're a maintainer):
    ```bash
    docker tag fastlearner-python-img qaximbalti/fastlearner-python-img
    docker push qaximbalti/fastlearner-python-img
    ```

### Run on a Hosting Machine (Ubuntu)

1.  **Pull the Docker image**:
    ```bash
    sudo docker pull qaximbalti/fastlearner-python-img
    ```
2.  **Run the container**:
    ```bash
    docker run -p <host-port>:<container-port> fastlearner-python-img
    ```
3.  **Check running containers**:
    ```bash
    docker ps
    ```

---

## 📝 Important Notes

* **Port Mapping**: Remember to adjust the `<host-port>:<container-port>` mapping to suit your machine's requirements.
* **Prerequisites**: The application will not run correctly without **FFmpeg** and the **API keys** being properly configured.