FROM jetty:9.4.18
USER root

COPY ./wait-for-cassandra-and-start.sh /wait-for-cassandra-and-start.sh
COPY ./webdefault.xml /usr/local/jetty/etc/webdefault.xml

RUN echo "deb http://archive.debian.org/debian stretch main contrib non-free" > /etc/apt/sources.list

RUN apt update
RUN apt install -y netcat
RUN chmod +x /wait-for-cassandra-and-start.sh


CMD /wait-for-cassandra-and-start.sh && java -DGS_SERVER_ID=1 -Djetty.deploy.scanInterval=0 -Xmx512m \
    -Xdebug -agentlib:jdwp=transport=dt_socket,address=9000,server=y,suspend=n \
    -jar /usr/local/jetty/start.jar
