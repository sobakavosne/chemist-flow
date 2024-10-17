FROM openjdk:21-jdk-slim

WORKDIR /app

RUN \
  apt-get update && apt-get install -y curl \
  && rm -rf /var/lib/apt/lists/*

RUN \
  curl -Lo sbt.deb https://repo.scala-sbt.org/scalasbt/debian/sbt-1.9.1.deb \
  && dpkg -i sbt.deb \
  && rm sbt.deb

COPY build.sbt .
COPY project/ project/

RUN sbt update

COPY src/ ./src/
RUN sbt compile

COPY . .

EXPOSE 8081

CMD ["sbt", "run"]
