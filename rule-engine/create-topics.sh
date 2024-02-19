#!/usr/bin/env bash

 docker exec -it kafka kafka-topics --bootstrap-server kafka:9092 --create --topic foo --partitions 3
 docker exec -it kafka kafka-topics --bootstrap-server kafka:9092 --create --topic bar --partitions 3


# docker exec -it kafka kafka-console-consumer --bootstrap-server kafka:9092 --topic foo --from-beginning --property   print.partition=true --property print.key=true
# docker exec -it kafka kafka-console-producer --bootstrap-server kafka:9092 --topic foo