FROM frolvlad/alpine-oraclejdk8:slim
VOLUME /tmp
ADD example101-v1.jar example101.jar
RUN sh -c 'touch /example101.jar'
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /example101.jar" ]