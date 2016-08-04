FROM java:8
VOLUME /tmp

#Set Environment
ENV http_proxy="http://internal-SquidProxies-2071618043.eu-central-1.elb.amazonaws.com:8080/" \
https_proxy="http://internal-SquidProxies-2071618043.eu-central-1.elb.amazonaws.com:8080/" \
no_proxy="169.254.169.254,127.0.0.1,localhost,db.de,signintra.com,intranet.deutschebahn.com,intranet-test.deutschebahn.com,intranet.dbschenker.com,service.deutschebahn.com,dbplanet.deutschebahn.com"
RUN export INFR_ADDR=10.43.116.135

#Install Filebeat
RUN apt-get update
RUN apt-get install apt-transport-https
RUN set -x
RUN echo "deb https://packages.elastic.co/beats/apt stable main" |  tee -a /etc/apt/sources.list.d/beats.list
RUN wget -qO - https://packages.elastic.co/GPG-KEY-elasticsearch | apt-key add -
RUN apt-get update
RUN apt-get install filebeat

#Configure Filebeat  
ADD filebeat.yml /etc/filebeat/filebeat.yml
ADD start.sh /start.sh

#Copy the Application
ADD seatmanagementservice-0.0.1-SNAPSHOT.jar app.jar
RUN bash -c 'touch /app.jar'

EXPOSE 8080

#Start Filebeat and Application
CMD ["bash", "/start.sh"]