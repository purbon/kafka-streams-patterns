#!/usr/bin/env bash

set -e
echo '{"f1": "value1-a"}'
echo '{"f1": "value2-a"}'
echo '{"f1": "value3-a"}'

docker-compose exec kafka bash -c "kinit -k -t /var/lib/secret/kafka-admin.key admin/for-kafka && kafka-avro-console-producer --broker-list kafka:9093 --property schema.registry.url=http://schema-registry:8081 --topic server1.dbo.tablea  --property value.schema=\"{'type':'record','name':'myrecord','fields':[{'name':'f1','type':'string'}]}\""