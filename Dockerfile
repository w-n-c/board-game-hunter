FROM openjdk:8-alpine

COPY target/uberjar/board-game-hunter.jar /board-game-hunter/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/board-game-hunter/app.jar"]
