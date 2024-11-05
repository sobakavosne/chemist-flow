FROM openjdk:21-jdk-slim

WORKDIR /app

RUN apt-get update && apt-get install -y curl \
  && rm -rf /var/lib/apt/lists/*

RUN curl -Lo /usr/local/bin/sbt-launch.jar https://repo1.maven.org/maven2/org/scala-sbt/sbt-launch/1.9.6/sbt-launch-1.9.6.jar

RUN echo '#!/bin/sh\nexec java -jar /usr/local/bin/sbt-launch.jar "$@"' > /usr/local/bin/sbt \
  && chmod +x /usr/local/bin/sbt

COPY project/build.properties project/build.properties

COPY build.sbt .

COPY project/ project/

RUN sbt update

COPY src/ ./src/

RUN sbt compile

COPY . .

EXPOSE 8081

CMD ["sbt", "run"]
