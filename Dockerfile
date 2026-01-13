# ---- build stage ----
    FROM maven:3.9-eclipse-temurin-21 AS build
    WORKDIR /app
    
    # Cache deps first (faster rebuilds)
    COPY pom.xml ./
    RUN mvn -q -DskipTests dependency:go-offline
    
    # Build
    COPY src ./src
    RUN mvn -q -DskipTests package
    
    # ---- runtime stage ----
    FROM eclipse-temurin:21-jre
    WORKDIR /app
    
    # Copy the built jar (reliable across versions)
    COPY --from=build /app/target/*.jar /app/app.jar
    
    EXPOSE 8080
    ENV SERVER_PORT=8080
    
    ENTRYPOINT ["java","-jar","/app/app.jar"]
    