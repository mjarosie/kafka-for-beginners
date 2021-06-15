# twitter-producer

Pulls sample tweets from Twitter and produces these to a Kafka topic.

To run the producer, make sure the kafka cluster is up first.

You'll have to retrieve your Twitter credentials and configure the app to use these by creating a `twitter4j.properties`
file in this directory. The file contents should look like this:

```
debug=false # can be set to true if required 
oauth.consumerKey=<your API key>
oauth.consumerSecret=<your API secret>
oauth.accessToken=<your access token>
oauth.accessTokenSecret=<your access token secret>
```

Having set this up, `cd` to this directory and run:

```bash
docker run --rm -v $(pwd):/app/twitter-producer \
-w /app/twitter-producer --network kafka-cluster \
hseeberger/scala-sbt:11.0.2-oraclelinux7_1.4.7_2.13.4 \
sbt run
```

When you run into `sbt thinks that server is already booting because of this exception` error,
add `-Dsbt.server.forcestart=true --batch` options to `sbt` command:

```bash
docker run --rm -v $(pwd):/app/twitter-producer \
-w /app/twitter-producer --network kafka-cluster \
hseeberger/scala-sbt:11.0.2-oraclelinux7_1.4.7_2.13.4 \
sbt -Dsbt.server.forcestart=true --batch run
```
