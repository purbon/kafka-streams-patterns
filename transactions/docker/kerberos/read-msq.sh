#!/usr/bin/env bash

set -e

docker-compose exec kafka bash -c "kinit -k -t /var/lib/secret/kafka-admin.key admin/for-kafka && kafka-avro-console-consumer --bootstrap-server kafka:9093 --from-beginning --topic server1.dbo.tablea --property schema.registry.url=http://schema-registry:8081"