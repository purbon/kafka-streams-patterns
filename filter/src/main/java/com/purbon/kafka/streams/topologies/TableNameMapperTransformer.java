package com.purbon.kafka.streams.topologies;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class TableNameMapperTransformer implements Transformer<GenericRecord, GenericRecord, KeyValue<Tuple<GenericRecord, GenericRecord>, GenericRecord>> {

    ProcessorContext context;
    Map<String, String> config;

    public TableNameMapperTransformer(Map<String, String> config) {
        this.config = config;
    }

    @Override
    public void init(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public KeyValue<Tuple<GenericRecord, GenericRecord>, GenericRecord> transform(GenericRecord key, GenericRecord value) {
        var tuple = new Tuple<GenericRecord, GenericRecord>();
        tuple.left = null;

        if (value != null) {
            Header sourceTableHeader = context.headers().lastHeader("__source_table");
            Header sourceSchemaHeader = context.headers().lastHeader("__source_schema");
            Header sourceDbHeader = context.headers().lastHeader("__source_db");

            String table = new String(sourceTableHeader.value(), StandardCharsets.UTF_8);
            String schema = new String(sourceSchemaHeader.value(), StandardCharsets.UTF_8);
            String db = new String(sourceDbHeader.value(), StandardCharsets.UTF_8);

            tuple.left = buildConfigTableAvroKey(db, schema, table);
        }

        tuple.right = key;
        return KeyValue.pair(tuple, value);

    }

    @Override
    public void close() {

    }

    protected GenericRecord buildConfigTableAvroKey(String databasename, String schemaName, String tableName) {
        String userSchema = config.get(FilterTopology.KEY_AVRO_SCHEMA);
        Schema.Parser parser = new Schema.Parser();
        Schema schema = parser.parse(userSchema);
        GenericRecord avroRecord = new GenericData.Record(schema);
        avroRecord.put(config.get(FilterTopology.KEY_SCHEMA_DB_FIELD), databasename);
        avroRecord.put(config.get(FilterTopology.KEY_SCHEMA_SCHEMA_FIELD), schemaName);
        avroRecord.put(config.get(FilterTopology.KEY_SCHEMA_TABLE_FIELD), tableName);
        return avroRecord;
    }

}
