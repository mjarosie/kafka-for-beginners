# java-consumer

To run the consumer, make sure the kafka cluster is up first,
then `cd` to this directory and run:

```bash
docker run -v $(pwd):/app/java-consumer -v $(pwd)/.m2:/root/.m2 \
-w /app/java-consumer --network kafka-cluster maven:3-adoptopenjdk-11 \
mvn compile exec:java -Dexec.mainClass="com.mjarosie.ConsumerDemo"
```

We're reusing Maven Local Repository between containers to save bandwidth,
time and reduce lengthy Maven logs produced when pulling dependencies.

You can change the name of the class that you want to run for exploring different examples.