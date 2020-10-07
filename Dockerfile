########################################
# create base image with basic stuff
# and boring long downloads

FROM ubuntu:bionic as build_base

ENV LC_ALL=C.UTF-8
ENV LANG=C.UTF-8

RUN apt update && apt install -y \
    openjdk-8-jdk-headless \
    wget

RUN mkdir -p /opt/xlike/tree-tagger && \
    wget -O /opt/xlike/tree-tagger/tree-tagger.tar.gz https://www.cis.uni-muenchen.de/~schmid/tools/TreeTagger/data/tree-tagger-linux-3.2.3.tar.gz 

RUN mkdir -p /opt/xlike/gradle

COPY ./xlike_hr/gradlew /opt/xlike/gradlew/gradlew
COPY ./xlike_hr/gradle /opt/xlike/gradlew/gradle
COPY ./xlike_hr/.gradle /opt/xlike/gradlew/.gradle

RUN cd /opt/xlike/gradlew/ && ./gradlew help && echo $HOME

RUN apt install -y python3-pip


########################################
# build xlike_hr fat jar

FROM build_base as build_xlike_java

COPY --from=build_base /root/.gradle $HOME/.gradle

COPY ./xlike_hr /build/xlike_hr

WORKDIR /build/xlike_hr/

RUN ./gradlew shadowJar


########################################
# build annotator PEX

FROM build_base as build_annotator_python

RUN apt install -y git
RUN pip3 install pex

COPY ./annotator /build/annotator

# pack python app to a PEX file with application:main as an entry point
# later in the pipline we just need python3 and pex file for annotator
# to run
RUN cd /build/annotator && pex -o annotator.pex -r requirements.txt -e annotator.application:main


########################################
# deploy xlike_hr

FROM ubuntu:bionic as deployment_xlike

ENV LC_ALL=C.UTF-8
ENV LANG=C.UTF-8

RUN apt update && apt install -y openjdk-8-jre-headless

# take xlike_hr and friends from the build images
COPY --from=build_base /opt/xlike/tree-tagger/tree-tagger.tar.gz /service/third-party/tree-tagger.tar.gz
RUN cd /service/third-party && mkdir tree-tagger && tar -xf tree-tagger.tar.gz -C tree-tagger
COPY --from=build_xlike_java /build/xlike_hr/build/libs/xlike_hr-1.0-SNAPSHOT-all.jar /service/xlike_hr.jar

COPY ./resources/data/ /service/data/

# in case XLIKE_PORT changes, annotator's .config
# file should be updated accordingly
ENV XLIKE_PORT=8081
EXPOSE 8081

ENTRYPOINT java -jar /service/xlike_hr.jar


########################################
# image for deploying only annotator

FROM ubuntu:bionic as deployment_annotator

ENV LC_ALL=C.UTF-8
ENV LANG=C.UTF-8

RUN apt update -y && apt install -y python3 python3-distutils

# snatch annotator from the build image
COPY --from=build_annotator_python /build/annotator/annotator.pex /service/annotator.pex

# these variables are (or should be) respected by annotator.pex and xlike_hr.jar
# these are also default ports; 
ENV ANNOTATOR_PORT=8080
EXPOSE 8080

ENTRYPOINT PEX_SCRIPT=gunicorn /service/annotator.pex annotator.application:app -b :${ANNOTATOR_PORT:-8080} 2>&1


########################################
# final image containing only the stuff
# essential for deployment

FROM deployment_xlike as deployment

ENV LC_ALL=C.UTF-8
ENV LANG=C.UTF-8

RUN apt update -y && apt install -y python3 python3-distutils

# snatch annotator from the build image
COPY --from=build_annotator_python /build/annotator/annotator.pex /service/annotator.pex

COPY ./run-services.sh /service/run-services.sh

# this works together with run-services.sh script which will redirect
# services' output to this directory
RUN mkdir /service/log

# these variables are (or should be) respected by annotator.pex and xlike_hr.jar
# these are also default ports; 
ENV ANNOTATOR_PORT=8080
EXPOSE 8080

ENTRYPOINT /service/run-services.sh