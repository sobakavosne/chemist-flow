FROM openjdk:21-jdk-slim

WORKDIR /app

COPY build.sbt .
COPY project/ project/

RUN \
  apt-get update && apt-get install -y \
  curl \
  && curl -Lo sbt.deb https://repo.scala-sbt.org/scalasbt/debian/sbt-1.9.1.deb \
  && dpkg -i sbt.deb \
  && rm sbt.deb

RUN sbt update

COPY . .

RUN sbt compile

EXPOSE 8081

CMD ["sbt", "run"]
