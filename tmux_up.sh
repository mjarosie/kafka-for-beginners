#!/bin/bash

CWD=$(pwd)
SESSION_NAME="kafka"

tmux detach > /dev/null

set -- $(stty size)
tmux new-session -d -s $SESSION_NAME -x "$2" -y "$(($1 - 1))"

tmux new-window -t $SESSION_NAME:1 -n 'kafka-utils'
tmux new-window -t $SESSION_NAME:2 -n 'java-producer'
tmux new-window -t $SESSION_NAME:3 -n 'java-consumer'
tmux new-window -t $SESSION_NAME:4 -n 'scala-producer'
tmux new-window -t $SESSION_NAME:5 -n 'twitter-producer'
tmux new-window -t $SESSION_NAME:6 -n 'twitter-consumer'

tmux select-window -t $SESSION_NAME:0
tmux rename-window 'kafka-cluster'
tmux send-keys "docker-compose up" C-m

tmux select-window -t $SESSION_NAME:1
tmux send-keys "docker exec -it kafka-for-beginners_kafka_1 /bin/bash"

tmux select-window -t $SESSION_NAME:2
tmux send-keys "cd producers/java-producer" C-m

tmux select-window -t $SESSION_NAME:3
tmux send-keys "cd consumers/java-consumer" C-m
tmux send-keys "docker run -v \$(pwd):/app/java-consumer -v \$(pwd)/.m2:/root/.m2 \
-w /app/java-consumer --network kafka-cluster maven:3-adoptopenjdk-11 \
mvn compile exec:java -Dexec.mainClass=\"com.mjarosie.ConsumerDemo\""

tmux select-window -t $SESSION_NAME:4
tmux send-keys "cd producers/scala-producer" C-m

tmux select-window -t $SESSION_NAME:5
tmux send-keys "cd producers/twitter-producer" C-m
tmux send-keys "docker run -it --rm -v \$(pwd):/app/twitter-producer \
-w /app/twitter-producer --network kafka-cluster \
hseeberger/scala-sbt:11.0.2-oraclelinux7_1.4.7_2.13.4 \
sbt -Dsbt.server.forcestart=true --batch run"

tmux select-window -t $SESSION_NAME:6
tmux send-keys "cd consumers/twitter-consumer" C-m
tmux send-keys "docker run -it --rm -v \$(pwd):/app/twitter-consumer \
-w /app/twitter-consumer --network kafka-cluster \
hseeberger/scala-sbt:11.0.2-oraclelinux7_1.4.7_2.13.4 \
sbt -Dsbt.server.forcestart=true --batch run"


tmux attach -t $SESSION_NAME
