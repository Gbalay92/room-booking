# syntax=docker/dockerfile:1

# ---- build: JDK + Maven ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -q dependency:go-offline          # dependency layer: only re-run when pom.xml changes
COPY src ./src
RUN mvn -B -q package -DskipTests            # tests run in CI, not in the image build

# ---- extract Spring Boot layers ----
FROM eclipse-temurin:21-jre AS extract
WORKDIR /extract
COPY --from=build /workspace/target/room-booking-*.jar application.jar
RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

# ---- runtime: JRE only, non-root ----
FROM eclipse-temurin:21-jre
RUN groupadd --system spring && useradd --system --gid spring --no-create-home spring
WORKDIR /app
COPY --from=extract /extract/extracted/dependencies/ ./
COPY --from=extract /extract/extracted/spring-boot-loader/ ./
COPY --from=extract /extract/extracted/snapshot-dependencies/ ./
COPY --from=extract /extract/extracted/application/ ./
ARG GIT_COMMIT=unknown
ENV INFO_APP_COMMIT=${GIT_COMMIT}
USER spring
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "application.jar"]