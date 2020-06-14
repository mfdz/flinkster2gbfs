FROM gradle:jdk11 AS builder
COPY --chown=gradle:gradle kotlin-geojson-moshi/ /home/gradle/src/kotlin-geojson-moshi

WORKDIR /home/gradle/src/kotlin-geojson-moshi
RUN gradle build --stacktrace --no-daemon && mkdir ../libs/ && ls -l build/libs && \
  cp build/libs/geojson-1.0-SNAPSHOT.jar ../libs/

COPY --chown=gradle:gradle ./ /home/gradle/src/
WORKDIR /home/gradle/src/
RUN gradle build --no-daemon
 

FROM amazoncorretto:11
COPY --from=builder /home/gradle/src/build/libs/flinkster2gbfs-0.1-SNAPSHOT.jar /app/flinkster2gbfs-0.1-SNAPSHOT.jar
WORKDIR /data

CMD java -jar /app/flinkster2gbfs-0.1-SNAPSHOT.jar