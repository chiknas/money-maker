#
# Build stage
#
FROM maven:3.8.6 as BUILD
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package -DskipTests

#
# Package stage
#
FROM amazoncorretto:11
COPY --from=build /home/app/target/money-maker-1.0-SNAPSHOT-jar-with-dependencies.jar /usr/local/lib/money-maker.jar
ENTRYPOINT ["java","-jar","/usr/local/lib/money-maker.jar"]
