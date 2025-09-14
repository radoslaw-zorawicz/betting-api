FROM maven:3.9-eclipse-temurin-24 AS build
WORKDIR /app

COPY pom.xml ./

RUN --mount=type=cache,target=/root/.m2 mvn -B -q dependency:go-offline

COPY src ./src

# Run full build pipeline: compile, test, integration-test, verify, package
# No skip flags; 'verify' triggers Failsafe's IT + verification if configured.
RUN --mount=type=cache,target=/root/.m2 mvn -B -DskipTests=false -Dmaven.test.skip=false verify

RUN bash -lc 'set -euo pipefail; jar=$(ls -1 target/*.jar | head -n1); cp "$jar" /app/app.jar'


# ---------- RUNTIME STAGE: Temurin JRE 24 ----------
FROM eclipse-temurin:24-jre

ENV TZ=UTC \
    JAVA_OPTS="" \
    SPRING_PROFILES_ACTIVE=local \
    SERVER_PORT=8080

# Non-root user
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

WORKDIR /app
COPY --from=build /app/app.jar /app/app.jar

EXPOSE 8080


ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
