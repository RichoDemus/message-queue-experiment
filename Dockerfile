FROM develar/java:8u45

COPY build/libs/message-queue-experiment-all.jar /message-queue-all.jar

ENTRYPOINT ["java","-jar","/message-queue-all.jar"]
