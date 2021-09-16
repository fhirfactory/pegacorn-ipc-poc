FROM jboss/wildfly

# Replace the default wildfly welcome content page for the URL /, with a blank html page, so the application server is not easily exposed to callers.
#RUN mv $JBOSS_HOME/welcome-content/index.html $JBOSS_HOME/welcome-content/index-bak.html
#COPY /src/main/webapp/index.html $JBOSS_HOME/welcome-content/index.html

# deploy the application
COPY *.war $JBOSS_HOME/standalone/deployments/

COPY setup-env-then-start-wildfly-as-jboss.sh /
COPY start-wildfly.sh /

ARG IMAGE_BUILD_TIMESTAMP
ENV IMAGE_BUILD_TIMESTAMP=${IMAGE_BUILD_TIMESTAMP}
RUN echo IMAGE_BUILD_TIMESTAMP=${IMAGE_BUILD_TIMESTAMP}

EXPOSE 8080 9990 7900

USER root
# Install gosu based on
# 1. https://gist.github.com/rafaeltuelho/6b29827a9337f06160a9
# 2. https://github.com/tianon/gosu
# 3. https://github.com/tianon/gosu/releases/download/1.12/gosu-amd64
COPY gosu-amd64 /usr/local/bin/gosu
RUN chmod +x /usr/local/bin/gosu && \
	chmod +x /setup-env-then-start-wildfly-as-jboss.sh && \
   	chmod +x /start-wildfly.sh

CMD	["/setup-env-then-start-wildfly-as-jboss.sh"]