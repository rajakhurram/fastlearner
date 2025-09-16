# 🚀 Fastlearner Backend — Linux Machine Setup

This guide will help you set up all the required **programs, tools, and services** for running the Fastlearner backend.

---

## 📌 Prerequisites

Before starting, make sure your system is **Ubuntu (20.04 or later recommended)** and you have `sudo` privileges.

---

## ⚙️ Installing Java 17

```bash
sudo apt install wget -y
wget https://download.oracle.com/java/17/latest/jdk-17_linux-x64_bin.deb
sudo apt install ./jdk-17_linux-x64_bin.deb

# Verify installation
java -version

# (OPTIONAL) Add alternatives
sudo update-alternatives --install "/usr/bin/java" "java" "/usr/lib/jvm/jdk-17/bin/java" 1

# For uninstalling
sudo apt purge jdk-17
```

📂 [Models Directory](src%2Fmain%2Fjava%2Fcom%2Fvinncorp%2Ffast_learner%2Fmodels)

---

## 🐘 Installing PostgreSQL

🔗 [Official Docs](https://www.postgresql.org/download/linux/ubuntu/)  
🎥 [Video Tutorial](https://www.youtube.com/watch?v=tducLYZzElo)

### Steps:
```bash
# Add PostgreSQL repository
sudo sh -c 'echo "deb https://apt.postgresql.org/pub/repos/apt $(lsb_release -cs)-pgdg main" > /etc/apt/sources.list.d/pgdg.list'

# Import repository signing key
wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | sudo apt-key add -

# Update packages and install PostgreSQL
sudo apt-get update
sudo apt-get -y install postgresql
```

### Set PostgreSQL password:
```bash
sudo -i -u postgres
psql
\password postgres
```

### Enable Similarity Search:
```sql
CREATE EXTENSION pg_trgm;
```

### Database Setup:
```bash
# Switch to postgres user
sudo -i -u postgres

# Open PostgreSQL shell
psql

# Exit PostgreSQL
\q

# Create and connect to a database
createdb <db_name>
psql -d <db_name>
```

---

## 🛠 Installing PgAdmin

🔗 [Download PgAdmin](https://www.pgadmin.org/download/pgadmin-4-apt/)

### Install dependencies:
```bash
sudo apt install curl
```

### Setup repository and install PgAdmin:
```bash
# Add repository key
curl -fsS https://www.pgadmin.org/static/packages_pgadmin_org.pub | sudo gpg --dearmor -o /usr/share/keyrings/packages-pgadmin-org.gpg

# Add repository
sudo sh -c 'echo "deb [signed-by=/usr/share/keyrings/packages-pgadmin-org.gpg] https://ftp.postgresql.org/pub/pgadmin/pgadmin4/apt/$(lsb_release -cs) pgadmin4 main" > /etc/apt/sources.list.d/pgadmin4.list && apt update'

# Install pgAdmin
sudo apt install pgadmin4
```

👉 When prompted, provide a dummy **email** and **password** to access pgAdmin.

---

## 🐍 Installing Python

```bash
sudo apt update
sudo apt install python3

# Check installation
python3 --version
```

---

## 🔎 Installing Elasticsearch

🔗 [Official Docs](https://www.elastic.co/guide/en/elasticsearch/reference/current/targz.html)

### Installation:
```bash
wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-8.12.0-linux-x86_64.tar.gz
tar -xzf elasticsearch-8.12.0-linux-x86_64.tar.gz
cd elasticsearch-8.12.0/

# Run Elasticsearch
./bin/elasticsearch
```

### Run Elasticsearch at startup:
```bash
sudo nano /etc/systemd/system/elasticsearch.service
```

Paste:
```ini
[Unit]
Description=Elasticsearch
After=network.target

[Service]
ExecStart=/home/username/elasticsearch-8.12.0/bin/elasticsearch
WorkingDirectory=/home/username/elasticsearch-8.12.0
User=username
Group=username
Restart=always

[Install]
WantedBy=default.target
```

Enable service:
```bash
sudo systemctl daemon-reload
sudo systemctl enable elasticsearch.service
sudo systemctl start elasticsearch.service
sudo systemctl status elasticsearch.service
```

### Configure index for `course`:
```json
PUT /course
{
  "settings": {
    "index": {
      "analysis": {
        "analyzer": {
          "keyword_analyzer": {
            "type": "custom",
            "tokenizer": "keyword",
            "filter": ["lowercase"]
          }
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "title": {
        "type": "text",
        "analyzer": "keyword_analyzer"
      }
    }
  }
}
```

---

## 🐇 Installing RabbitMQ

🔗 [Installation Guide](https://gcore.com/learning/how-to-install-rabbitmq-ubuntu/)

### Quick Install:
```bash
sudo apt-get update
sudo apt-get install rabbitmq-server
sudo systemctl status rabbitmq-server
sudo rabbitmq-plugins enable rabbitmq_management
```

Commands:
```bash
# Start
sudo systemctl start rabbitmq-server

# Stop
sudo systemctl stop rabbitmq-server

# Remove
sudo apt-get remove rabbitmq-server
```

---

## 🔧 Installing Jenkins (CI/CD)

Jenkins requires **OpenJDK 11**.

```bash
sudo apt update
sudo apt install openjdk-11-jdk -y

# Configure JAVA_HOME
nano ~/.bashrc
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
source ~/.bashrc

# Verify
java -version
```

### Install Jenkins:
```bash
sudo wget -O /usr/share/keyrings/jenkins-keyring.asc   https://pkg.jenkins.io/debian/jenkins.io-2023.key

echo deb [signed-by=/usr/share/keyrings/jenkins-keyring.asc]   https://pkg.jenkins.io/debian binary/ | sudo tee   /etc/apt/sources.list.d/jenkins.list > /dev/null

sudo apt-get update
sudo apt-get install jenkins
sudo systemctl status jenkins
```

---

## 🐍 Python Dependencies

```bash
sudo apt install ffmpeg libpq-dev python3-pip
```

---

## ☕ Maven Installation

```bash
sudo apt install maven
mvn -version
```

---

## 🐳 Docker Installation

🔗 [Install Docker Guide](https://www.digitalocean.com/community/tutorials/how-to-install-and-use-docker-on-ubuntu-20-04)

```bash
sudo apt update
sudo apt install apt-transport-https ca-certificates curl software-properties-common

curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu focal stable"

sudo apt install docker-ce
sudo systemctl status docker
```

---

# 🚀 Deployment

### Build & Run
- Backup database
- Build Docker image for Python app
- Setup Jenkins pipeline
- Run Spring Boot JAR

---

## 🔥 Running Spring Boot JAR as a Service

```bash
sudo nano /etc/systemd/system/fastlearner_be.service
```

Paste:
```ini
[Unit]
Description=Fastlearner Backend Service
Requires=network.target remote-fs.target
After=network.target remote-fs.target

[Service]
Type=simple
User=vinncorp-server
WorkingDirectory=/home/vinncorp-server/fastlearner/backend
ExecStart=/usr/bin/java -jar /home/vinncorp-server/fastlearner/backend/FastLearner-0.0.1-SNAPSHOT.jar
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Enable service:
```bash
sudo systemctl daemon-reload
sudo systemctl enable fastlearner_be.service
sudo systemctl start fastlearner_be.service
sudo systemctl status fastlearner_be.service
```

---

## 🐍 Running Python Docker Image as a Service

```bash
sudo nano /etc/systemd/system/fastlearner_python.service
```

Paste:
```ini
[Unit]
Description=Fastlearner Python Service
Requires=docker.service
After=docker.service

[Service]
Restart=always
ExecStart=/usr/bin/docker run --name fastlearner-python -p 5000:5000 -d qaximbalti/fastlearner-python-img:latest
RestartSec=10s
TimeoutStartSec=0

[Install]
WantedBy=default.target
```

---

## 🌐 Running Angular Docker Image as a Service

```bash
sudo nano /etc/systemd/system/fastlearner_fe.service
```

Paste:
```ini
[Unit]
Description=Fastlearner Frontend Service
Requires=docker.service
After=docker.service

[Service]
Restart=always
ExecStart=/usr/bin/docker run --name fastlearner-fe -p 4200:80 -d qaximbalti/fastlearner-fe-img:latest
RestartSec=10s
TimeoutStartSec=0

[Install]
WantedBy=default.target
```

---

# ✅ Linux Machine Setup Completed!
Your Fastlearner Backend, Python, and Angular services are now ready to run. 🎉
