package com.purbon.kafka.streams.config;

import com.purbon.kafka.streams.config.filter.Column;
import com.purbon.kafka.streams.config.filter.Key;
import com.purbon.kafka.streams.topologies.FilterTopology;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties("filter.config.table")
@Setter
@Getter
@NoArgsConstructor
public class FilterConfig {

   private Key key;
   private Column column;

   public Map<String, String> asMap() {
      Map<String, String> config = new HashMap<>();
      config.put(FilterTopology.KEY_ID, key.getId().getColumn());
      config.put(FilterTopology.MIN_COL, column.getMin());
      config.put(FilterTopology.MAX_COL, column.getMax());
      config.put(FilterTopology.KEY_AVRO_SCHEMA, key.getSchema().getJson());
      config.put(FilterTopology.KEY_SCHEMA_DB_FIELD, key.getSchema().getField().getDb());
      config.put(FilterTopology.KEY_SCHEMA_SCHEMA_FIELD, key.getSchema().getField().getDbschema());
      config.put(FilterTopology.KEY_SCHEMA_TABLE_FIELD, key.getSchema().getField().getTable());

      return config;
   }

}
