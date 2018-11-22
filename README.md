# Description
Run application by:
- docker
- Idea IDE: run button 
- Maven: mvn spring-boot:run
- java: java -jar target/image_demo-0.1.0.jar

Open URL http://localhost:8080/ in browser

Input data in webform and press Upload button.

The result will be response as list of thumbnail with 100px x 100px in Base64 format

# REST API
- GET / - for demo purpose only, response demo html page

- POST / - required test API, the response will be uploaded thumbnails with 100px x 100px in Base64 format 

# Build
./mvnw install dockerfile:build

# Docker
For start demo please run following command in console:

- docker pull juhnowski/image_demo

- docker run -p 8080:8080 juhnowski/image_demo:latest

# Requirements
- Java 1.8
- Docker version 18.09.0
- Maven 3.5.2


