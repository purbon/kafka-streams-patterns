#!/usr/bin/env bash

docker exec broker bash -c "kafka-console-producer --broker-list broker:9092 --topic oltp.dbo.tablea"

docker exec -it schema-registry /bin/bash

kafka-avro-console-producer --topic oltp.dbo.tablea \
                            --bootstrap-server broker:9092 \
                            --property key.schema="$(< /opt/app/schema/key_detail.avsc)" \
                            --property value.schema="$(< /opt/app/schema/order_detail.avsc)" \
                            --property parse.key=true \
                            --property key.separator="#"

##
# {"number":"122345"}#{"number": 2343434, "date": 1596490462, "shipping_address": "456 Everett St, Palo Alto, 94301 CA, USA", "subtotal": 99.0, "shipping_cost": 0.0, "tax": 8.91, "grand_total": 107.91}
# {"number":"256743"}#{"number": 2343435, "date": 1596491687, "shipping_address": "518 Castro St, Mountain View, 94305 CA, USA", "subtotal": 25.0, "shipping_cost": 0.0, "tax": 2.91, "grand_total": 27.91}
##