# scala-producer

To run the producer, make sure the kafka cluster is up first,
then `cd` to this directory and run:

```bash
docker run --rm -v $(pwd):/app/scala-producer \
-w /app/scala-producer --network kafka-cluster \
hseeberger/scala-sbt:11.0.2-oraclelinux7_1.4.7_2.13.4 \
sbt run
```

When you run into `sbt thinks that server is already booting because of this exception` error,
add `-Dsbt.server.forcestart=true --batch` options to `sbt` command:

```bash
docker run --rm -v $(pwd):/app/scala-producer \
-w /app/scala-producer --network kafka-cluster \
hseeberger/scala-sbt:11.0.2-oraclelinux7_1.4.7_2.13.4 \
sbt -Dsbt.server.forcestart=true --batch "runMain com.mjarosie.ProducerDemo"
```

You can change the name of the main class that you want to run for exploring different examples.
