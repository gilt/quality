FROM giltarchitecture/play_scala_2_3_7:0.1

MAINTAINER architecture@gilt.com

ADD . /usr/share/quality

WORKDIR /usr/share/quality

RUN sbt -Dsbt.ivy.home=.ivy2 clean stage

RUN ln -s /usr/share/quality/api/target/universal/stage /usr/share/quality-api
RUN ln -s /usr/share/quality/www/target/universal/stage /usr/share/quality-www
