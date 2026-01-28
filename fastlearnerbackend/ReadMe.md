# Linux machine setup for backend:
Following are the Program, Tools and Services required by the Fastlearner Backend.

### Installing JAVA 17:
```
sudo apt install wget -y
wget https://download.oracle.com/java/17/latest/jdk-17_linux-x64_bin.deb
sudo apt install ./jdk-17_linux-x64_bin.deb
java -version
(OPTIONAL) sudo update-alternatives --install "/usr/bin/java" "java" "/usr/lib/jvm/jdk-17/bin/java" 1
for uninstall: sudo apt purge jdk-17
```
[models](src%2Fmain%2Fjava%2Fcom%2Fvinncorp%2Ffast_learner%2Fmodels)
### Installing Postgresql:
[Visit Website:](https://www.postgresql.org/download/linux/ubuntu/)

You can go through the [video tutorial](https://www.youtube.com/watch?v=tducLYZzElo).
OR You can follow below steps:
```
# Create the file repository configuration:
sudo sh -c 'echo "deb https://apt.postgresql.org/pub/repos/apt $(lsb_release -cs)-pgdg main" > /etc/apt/sources.list.d/pgdg.list'

# Import the repository signing key:
wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | sudo apt-key add -

# Update the package lists:
sudo apt-get update

# Install the latest version of PostgreSQL.
# If you want a specific version, use 'postgresql-12' or similar instead of 'postgresql':
sudo apt-get -y install postgresql


## For creating password for postgres user
sudo -i -u postgres
psql
\password postgres (Enter for new password)
```
POSTGRES SIMILARITY FUNCTION INCLUSION USING BELOW QUERY:
```
CREATE EXTENSION pg_trgm;
```

### Now check the postgresql is installed on the system
`sudo -i -u postgres` running this command the username will be changed to postgres.
Now write `psql` and enter, now the terminal will look like below.
```
postgres=# 
NOTE: for exiting \q then enter
postgres@vinncorp:~$ createdb <db_name> (for creating db)
postgres@vinncorp:~$ psql -d <db_name> (now getting in the db)
my_db=#  (Now you are in the my_db database)
```

### Installing PgAdmin
[Visit Website: ](https://www.pgadmin.org/download/pgadmin-4-apt/)

In above website you can find some commands which are as follows but before executing those commands,
Install the ***curl*** if your machine doesn't have one.
```
sudo apt install curl
```
Now after this you can follow as the site's commands or as follows:
```
#
# Setup the repository
#

# Install the public key for the repository (if not done previously):
curl -fsS https://www.pgadmin.org/static/packages_pgadmin_org.pub | sudo gpg --dearmor -o /usr/share/keyrings/packages-pgadmin-org.gpg

# Create the repository configuration file:
sudo sh -c 'echo "deb [signed-by=/usr/share/keyrings/packages-pgadmin-org.gpg] https://ftp.postgresql.org/pub/pgadmin/pgadmin4/apt/$(lsb_release -cs) pgadmin4 main" > /etc/apt/sources.list.d/pgadmin4.list && apt update'

#
# Install pgAdmin
#

# Install for both desktop and web modes:
sudo apt install pgadmin4

# Install for desktop mode only:
sudo apt install pgadmin4-desktop

# Install for web mode only: 
sudo apt install pgadmin4-web 

# Configure the webserver, if you installed pgadmin4-web:
sudo /usr/pgadmin4/bin/setup-web.sh
```
When the last command run then it will asked a dummy email and password by which we can access pgAdmin via web. Now we 
can view the databases after connecting to our db server.

### Installing Python:
Execute following commands.
```
sudo apt update
sudo apt install python3

# Check installed python
sudo python3 or python3
```

### Installing Elasticsearch:
[Visit Website: ](https://www.elastic.co/guide/en/elasticsearch/reference/current/targz.html)
Pass: uLwrmdGOZlPqVfXBpJxv
KEY: eyJ2ZXIiOiI4LjEyLjAiLCJhZHIiOlsiMTI3LjAuMC4xOjkyMDAiXSwiZmdyIjoiYmI3NjQ4OGNjNTg0NzY4MmFlOTA0OGQ2MzA1YmNlNzE3MzdlMmNjYzU1YjYxNTkxZTlkZTgyYjlhNDhjN2M0ZSIsImtleSI6Im40Y21RWTBCM1ItTmZHT09lbFNBOnktOUQ3TWtZUnVPSC00YnFMUFYyalEifQ==
```
wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-8.12.0-linux-x86_64.tar.gz
tar -xzf elasticsearch-8.12.0-linux-x86_64.tar.gz
cd elasticsearch-8.12.0/

# Run
./bin/elasticsearch
```
For running the elasticsearch at startup follow below commands:
```
sudo nano /etc/systemd/system/elasticsearch.service
# Past below in the elasticsearch.service
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
####################

sudo systemctl daemon-reload
sudo systemctl enable elasticsearch.service

sudo systemctl start elasticsearch.service
sudo systemctl status elasticsearch.service
```

### Before running jar
Please update the index setting for the "course" in elastic search using below postman request with basic auth.
```
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

Now create the course data in elasticsearch if the db has any course.

## Installing RabbitMQ:
[Visit Website: ](https://gcore.com/learning/how-to-install-rabbitmq-ubuntu/)
Follow above website's commands, If there is any error i.e ***"sudo systemctl enable rabbitmq-server
Failed to enable unit: Unit file rabbitmq-server.service does not exist."***. then follow below commands.

```
sudo apt-get update
sudo apt-get install rabbitmq-server
sudo systemctl status rabbitmq-server
sudo rabbitmq-plugins enable rabbitmq_management

# If the rabbitmq server is not running then start by below command
sudo systemctl start rabbitmq-server

# For stoping the server
sudo systemctl stop rabbitmq-server

# For uninstalling
sudo apt-get remove rabbitmq-server
```

## For CI/CD Install Jenkins
For jenkins we must have to install the openjdk 11 first and give the JAVA_HOME path of openjdk-11 and also note that the 
jenkins use 8080 port by default so we have to release the 8080 port for the jenkins.
Follow below commands for installing openjdk and jenkins.
java-11-openjdk-amd64  jdk-17-oracle-x64
```
sudo apt update
sudo apt install openjdk-11-jdk -y

# If you already installed the jdk and sets the path then we have to replace with the openjdk-11 path
nano ~/.bashrc

export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

source ~/.bashrc

#Check version
java -version

sudo wget -O /usr/share/keyrings/jenkins-keyring.asc \
  https://pkg.jenkins.io/debian/jenkins.io-2023.key

echo deb [signed-by=/usr/share/keyrings/jenkins-keyring.asc] \
  https://pkg.jenkins.io/debian binary/ | sudo tee \
  /etc/apt/sources.list.d/jenkins.list > /dev/null

sudo apt-get update

sudo apt-get install jenkins

sudo systemctl status jenkins
```
## Python Dependencies
```
# verify if the python3 is installed?
# Below is for video transcript
sudo apt install ffmpeg

sudo apt install libpq-dev
sudo apt update

# Below is the pip installation
sudo apt install python3-pip


```
## Maven Installation
```
sudo apt install maven
mvn -version
```
## Docker Installation
[Installing Docker](https://www.digitalocean.com/community/tutorials/how-to-install-and-use-docker-on-ubuntu-20-04)
```
sudo apt update
sudo apt install apt-transport-https ca-certificates curl software-properties-common
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu focal stable"
apt-cache policy docker-ce

sudo apt install docker-ce
sudo systemctl status docker
```

### HERE THE LINUX MACHINE IS SETUP SUCCESSFULLY.

# DEPLOYMENT
Create Spring Boot jar with specific properties i.e for testing server use ***application-test-env.properties***.
- Backup Database.
- Build docker image of python app.
- [CI/CI with jenkins](https://howtodoinjava.com/devops/setup-jenkins-pipeline-for-spring-boot-app/)
- Now run the jar.

## Jar as a system service in ubuntu
Follow below command:
```
sudo nano /etc/systemd/system/fastlearner_be.service

# Put below data in the file:
[Unit]
Description=Fastlearner backend service
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
#######################

sudo systemctl daemon-reload
sudo systemctl enable fastlearner_be.service
sudo systemctl start fastlearner_be.service
sudo systemctl status fastlearner_be.service
```

## Running python docker image as a service in ubuntu
```
sudo nano /etc/systemd/system/fastlearner_python.service

#Create file with following data
  GNU nano 6.2                                                                       /etc/systemd/system/fastlearner_python.service                                                                                
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
########################

sudo systemctl daemon-reload
sudo systemctl enable fastlearner_python.service
sudo systemctl start fastlearner_python.service
sudo systemctl status fastlearner_python.service
```

## Running angular docker image as a service in ubuntu
```
sudo nano /etc/systemd/system/fastlearner_fe.service

#Create file with following data
  GNU nano 6.2                                                                       /etc/systemd/system/fastlearner_python.service                                                                                
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
########################

sudo systemctl daemon-reload
sudo systemctl enable fastlearner_fe.service
sudo systemctl start fastlearner_fe.service
sudo systemctl status fastlearner_fe.service
```