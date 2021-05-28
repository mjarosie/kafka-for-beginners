# java-producer

To run the producer, make sure the kafka cluster is up first,
then `cd` to this directory, make sure `.m2` directory exists and run:

```bash
docker run -v $(pwd):/app/java-producer -v $(pwd)/.m2:/root/.m2 \
-w /app/java-producer --network kafka-cluster maven:3-adoptopenjdk-11 \
mvn compile exec:java -Dexec.mainClass="com.mjarosie.ProducerDemo"
```

We're reusing Maven Local Repository between containers to save bandwidth,
time and reduce lengthy Maven logs produced when pulling dependencies.

You can change the name of the class that you want to run for exploring different examples.