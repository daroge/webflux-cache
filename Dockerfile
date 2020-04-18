FROM adoptopenjdk/maven-openjdk11 as BUILD
WORKDIR /build
COPY pom.xml .
RUN mvn -B -f pom.xml dependency:go-offline
COPY src /build/src
RUN mvn -B install spring-boot:repackage

FROM fabric8/java-alpine-openjdk11-jre
COPY --from=BUILD /build/target/note-app.jar /app/note-app.jar
ADD https://github.com/ufoscout/docker-compose-wait/releases/download/2.2.1/wait /app/wait
RUN chmod +x /app/wait
CMD /app/wait && java -jar /app/note-app.jar
