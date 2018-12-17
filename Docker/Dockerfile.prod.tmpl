FROM ubuntu:16.04

RUN apt-get update && apt-get install -y openjdk-8-jdk sudo cron iptables
RUN mkdir /opt/snmp
COPY ./src/opt/snmp /opt/snmp/
#COPY ./src/config/mediatorserver.conf /etc/
COPY ./src/mediatorserver.tmpl.conf /etc/
COPY ./src/start.sh /root/
COPY ./src/crontab /etc/cron/crontab
RUN adduser --gecos '' --disabled-login mediators

COPY crontab /etc/cron/crontab
RUN crontab /etc/cron/crontab
RUN chown -R mediators:mediators /opt/snmp 
RUN touch /var/log/mediatorserver.log && chown mediators:mediators /var/log/mediatorserver.log
 
RUN chown mediators:mediators /root/start.sh && chmod 777 /root/start.sh
#RUN echo "user ALL=(root) NOPASSWD:ALL" > /etc/sudoers.d/user && \
#    chmod 0440 /etc/sudoers.d/user

#CMD ["su", "-", "mediators", "-c", "/root/start.sh"]  
CMD /root/start.sh
EXPOSE 162/udp

