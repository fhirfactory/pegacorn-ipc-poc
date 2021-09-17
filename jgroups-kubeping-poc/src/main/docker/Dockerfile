FROM openjdk:11

# Replace the default wildfly welcome content page for the URL /, with a blank html page, so the application server is not easily exposed to callers.
#RUN mv $JBOSS_HOME/welcome-content/index.html $JBOSS_HOME/welcome-content/index-bak.html
#COPY /src/main/webapp/index.html $JBOSS_HOME/welcome-content/index.html

# deploy the application
RUN mkdir -p /usr/opt/poc/testnode
COPY *-with-dependencies.jar /usr/opt/poc/testnode/PegacornNodeCLI.jar
COPY *.yaml /usr/opt/poc/testnode/
WORKDIR /usr/opt/poc/testnode
RUN ls

EXPOSE 8080 9990

ARG IMAGE_BUILD_TIMESTAMP
ENV IMAGE_BUILD_TIMESTAMP=${IMAGE_BUILD_TIMESTAMP}
RUN echo IMAGE_BUILD_TIMESTAMP=${IMAGE_BUILD_TIMESTAMP}

CMD	java -jar PegacornNodeCLI.jar EchoPointServer