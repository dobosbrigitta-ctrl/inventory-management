FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S inventorymanagenent 
RUN adduser -S inventorymanagenent -G inventorymanagenent

COPY target/inventory-management-*.jar inventory-management.jar

RUN chown -R inventorymanagenent:inventorymanagenent /app

USER inventorymanagenent:inventorymanagenent

EXPOSE 8081

ENTRYPOINT ["java", "-XX:+UseG1GC", "-jar", "inventory-management.jar"]