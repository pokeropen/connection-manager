FROM java:8
WORKDIR /
ADD target/connection-manager*.jar Connection-Manager.jar
EXPOSE 9898
CMD java -jar Connection-Manager.jar