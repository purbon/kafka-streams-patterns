package com.purbon.kafka.streams.config;

import com.purbon.kafka.streams.topologies.FilterTopology;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties("filter.config")
@Data
@NoArgsConstructor
public class FilterConfig {

   private String table_key_id_column;
   private String table_min_column;
   private String table_max_column;
   private String table_key_schema_db_field;
   private String table_key_schema_dbschema_field;
   private String table_key_schema_table_field;
   private String table_key_schema;

   public Map<String, String> asMap() {
      Map<String, String> config = new HashMap<>();
      config.put(FilterTopology.KEY_ID, table_key_id_column);
      config.put(FilterTopology.MIN_COL, table_min_column);
      config.put(FilterTopology.MAX_COL, table_max_column);
      config.put(FilterTopology.KEY_AVRO_SCHEMA,table_key_schema);
      config.put(FilterTopology.KEY_SCHEMA_DB_FIELD, table_key_schema_db_field);
      config.put(FilterTopology.KEY_SCHEMA_SCHEMA_FIELD, table_key_schema_dbschema_field);
      config.put(FilterTopology.KEY_SCHEMA_TABLE_FIELD, table_key_schema_table_field);

      return config;
   }

}
