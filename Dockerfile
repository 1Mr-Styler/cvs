FROM ubuntu:16.04

LABEL version="1.0"
LABEL description="Install any package using SDKMan"

ENV SDKMAN_DIR /root/.sdkman
ENV JAVA_VERSION 8.0.202-zulu

RUN ["mkdir", "-p", "/apps/home"] 
RUN ls -la /apps/home
COPY . /apps/home
RUN apt-get update && apt-get install -y zip curl python2.7 python-pip
WORKDIR /apps/home
RUN pip2 install -r requirements.txt
RUN rm /bin/sh && ln -s /bin/bash /bin/sh
RUN curl -s "https://get.sdkman.io" | bash
RUN chmod a+x "$SDKMAN_DIR/bin/sdkman-init.sh"

RUN set -x \
    && echo "sdkman_auto_answer=true" > $SDKMAN_DIR/etc/config \
    && echo "sdkman_auto_selfupdate=false" >> $SDKMAN_DIR/etc/config \
    && echo "sdkman_insecure_ssl=false" >> $SDKMAN_DIR/etc/config

WORKDIR $SDKMAN_DIR
RUN [[ -s "$SDKMAN_DIR/bin/sdkman-init.sh" ]] && source "$SDKMAN_DIR/bin/sdkman-init.sh" && exec "$@"

RUN source /root/.bashrc

RUN ["mkdir", "-p", "/root/.sdkman/archives"] 
RUN ["mkdir", "-p", "/root/.gradle"] 
COPY ./sdkman/.sdkman/archives/ /root/.sdkman/archives
COPY ./sdkman/.gradle /root/.gradle

RUN source "$SDKMAN_DIR/bin/sdkman-init.sh" && sdk install java $JAVA_VERSION
WORKDIR /apps/home

EXPOSE 8080
EXPOSE 5005
ENTRYPOINT ["./docker-entry.sh"]


