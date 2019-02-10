FROM java:8
WORKDIR / 
ADD target/webServer-0.0.1-SNAPSHOT.jar ChatServer.jar 
ADD index.html index.html
ADD main.css main.css
ADD main.js main.js
ADD chatroom.html chatroom.html 
EXPOSE 8080  
CMD java -jar ChatServer.jar