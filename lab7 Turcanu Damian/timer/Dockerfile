FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /src
COPY TimerApp.java .
RUN javac -encoding UTF-8 -Xlint:all -d /out TimerApp.java
RUN jlink --add-modules java.base --strip-debug --no-man-pages --no-header-files --compress=zip-6 --output /jre

FROM alpine:3.22
RUN apk add --no-cache libstdc++ && addgroup -S -g 10001 timer && adduser -S -u 10001 -G timer timer
WORKDIR /app
COPY --from=build /out/ /app/
COPY --from=build /jre/ /opt/java/
USER 10001:10001
ENTRYPOINT ["/opt/java/bin/java", "-Dfile.encoding=UTF-8", "-Djava.io.tmpdir=/tmp", "-cp", "/app", "TimerApp"]
