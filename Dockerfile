# ---- build stage ----
    FROM eclipse-temurin:21-jdk AS build
    WORKDIR /app
    
    # Better caching: deps layer
    COPY pom.xml ./
    COPY .mvn .mvn
    COPY mvnw ./
    RUN ./mvnw -q -DskipTests dependency:go-offline
    
    # Now copy source and build
    COPY src src
    RUN ./mvnw -q -DskipTests clean package
    
    # ---- runtime stage ----
    FROM eclipse-temurin:21-jre
    WORKDIR /app
    
    # Non-root user (fixed uid)
    RUN useradd -m -u 10001 appuser
    USER appuser
    
    # Copy only the runnable jar (not *.jar.original)
    COPY --from=build /app/target/*SNAPSHOT.jar /app/app.jar
    
    ENV JAVA_OPTS=""
    EXPOSE 8080
    ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
    