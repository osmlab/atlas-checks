# Use base image: https://github.com/keeganwitt/docker-gradle
FROM gradle:jdk11

RUN mkdir -p /app/atlas-checks
COPY . /app/atlas-checks

ENTRYPOINT ["/app/atlas-checks/gradlew"]
