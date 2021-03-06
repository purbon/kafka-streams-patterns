/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package com.purbon.kafka.streams.model;

import org.apache.avro.specific.SpecificData;
import org.apache.avro.message.BinaryMessageEncoder;
import org.apache.avro.message.BinaryMessageDecoder;
import org.apache.avro.message.SchemaStore;

@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class Store extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  private static final long serialVersionUID = 5605655170412658713L;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"Store\",\"namespace\":\"com.purbon.kafka.streams.model\",\"fields\":[{\"name\":\"store_id\",\"type\":\"int\"},{\"name\":\"name\",\"type\":\"string\"}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }

  private static SpecificData MODEL$ = new SpecificData();

  private static final BinaryMessageEncoder<Store> ENCODER =
      new BinaryMessageEncoder<Store>(MODEL$, SCHEMA$);

  private static final BinaryMessageDecoder<Store> DECODER =
      new BinaryMessageDecoder<Store>(MODEL$, SCHEMA$);

  /**
   * Return the BinaryMessageDecoder instance used by this class.
   */
  public static BinaryMessageDecoder<Store> getDecoder() {
    return DECODER;
  }

  /**
   * Create a new BinaryMessageDecoder instance for this class that uses the specified {@link SchemaStore}.
   * @param resolver a {@link SchemaStore} used to find schemas by fingerprint
   */
  public static BinaryMessageDecoder<Store> createDecoder(SchemaStore resolver) {
    return new BinaryMessageDecoder<Store>(MODEL$, SCHEMA$, resolver);
  }

  /** Serializes this Store to a ByteBuffer. */
  public java.nio.ByteBuffer toByteBuffer() throws java.io.IOException {
    return ENCODER.encode(this);
  }

  /** Deserializes a Store from a ByteBuffer. */
  public static Store fromByteBuffer(
      java.nio.ByteBuffer b) throws java.io.IOException {
    return DECODER.decode(b);
  }

  @Deprecated public int store_id;
  @Deprecated public java.lang.CharSequence name;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>.
   */
  public Store() {}

  /**
   * All-args constructor.
   * @param store_id The new value for store_id
   * @param name The new value for name
   */
  public Store(java.lang.Integer store_id, java.lang.CharSequence name) {
    this.store_id = store_id;
    this.name = name;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call.
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return store_id;
    case 1: return name;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  // Used by DatumReader.  Applications should not call.
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: store_id = (java.lang.Integer)value$; break;
    case 1: name = (java.lang.CharSequence)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'store_id' field.
   * @return The value of the 'store_id' field.
   */
  public java.lang.Integer getStoreId() {
    return store_id;
  }

  /**
   * Sets the value of the 'store_id' field.
   * @param value the value to set.
   */
  public void setStoreId(java.lang.Integer value) {
    this.store_id = value;
  }

  /**
   * Gets the value of the 'name' field.
   * @return The value of the 'name' field.
   */
  public java.lang.CharSequence getName() {
    return name;
  }

  /**
   * Sets the value of the 'name' field.
   * @param value the value to set.
   */
  public void setName(java.lang.CharSequence value) {
    this.name = value;
  }

  /**
   * Creates a new Store RecordBuilder.
   * @return A new Store RecordBuilder
   */
  public static com.purbon.kafka.streams.model.Store.Builder newBuilder() {
    return new com.purbon.kafka.streams.model.Store.Builder();
  }

  /**
   * Creates a new Store RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new Store RecordBuilder
   */
  public static com.purbon.kafka.streams.model.Store.Builder newBuilder(com.purbon.kafka.streams.model.Store.Builder other) {
    return new com.purbon.kafka.streams.model.Store.Builder(other);
  }

  /**
   * Creates a new Store RecordBuilder by copying an existing Store instance.
   * @param other The existing instance to copy.
   * @return A new Store RecordBuilder
   */
  public static com.purbon.kafka.streams.model.Store.Builder newBuilder(com.purbon.kafka.streams.model.Store other) {
    return new com.purbon.kafka.streams.model.Store.Builder(other);
  }

  /**
   * RecordBuilder for Store instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<Store>
    implements org.apache.avro.data.RecordBuilder<Store> {

    private int store_id;
    private java.lang.CharSequence name;

    /** Creates a new Builder */
    private Builder() {
      super(SCHEMA$);
    }

    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(com.purbon.kafka.streams.model.Store.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.store_id)) {
        this.store_id = data().deepCopy(fields()[0].schema(), other.store_id);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.name)) {
        this.name = data().deepCopy(fields()[1].schema(), other.name);
        fieldSetFlags()[1] = true;
      }
    }

    /**
     * Creates a Builder by copying an existing Store instance
     * @param other The existing instance to copy.
     */
    private Builder(com.purbon.kafka.streams.model.Store other) {
            super(SCHEMA$);
      if (isValidValue(fields()[0], other.store_id)) {
        this.store_id = data().deepCopy(fields()[0].schema(), other.store_id);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.name)) {
        this.name = data().deepCopy(fields()[1].schema(), other.name);
        fieldSetFlags()[1] = true;
      }
    }

    /**
      * Gets the value of the 'store_id' field.
      * @return The value.
      */
    public java.lang.Integer getStoreId() {
      return store_id;
    }

    /**
      * Sets the value of the 'store_id' field.
      * @param value The value of 'store_id'.
      * @return This builder.
      */
    public com.purbon.kafka.streams.model.Store.Builder setStoreId(int value) {
      validate(fields()[0], value);
      this.store_id = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /**
      * Checks whether the 'store_id' field has been set.
      * @return True if the 'store_id' field has been set, false otherwise.
      */
    public boolean hasStoreId() {
      return fieldSetFlags()[0];
    }


    /**
      * Clears the value of the 'store_id' field.
      * @return This builder.
      */
    public com.purbon.kafka.streams.model.Store.Builder clearStoreId() {
      fieldSetFlags()[0] = false;
      return this;
    }

    /**
      * Gets the value of the 'name' field.
      * @return The value.
      */
    public java.lang.CharSequence getName() {
      return name;
    }

    /**
      * Sets the value of the 'name' field.
      * @param value The value of 'name'.
      * @return This builder.
      */
    public com.purbon.kafka.streams.model.Store.Builder setName(java.lang.CharSequence value) {
      validate(fields()[1], value);
      this.name = value;
      fieldSetFlags()[1] = true;
      return this;
    }

    /**
      * Checks whether the 'name' field has been set.
      * @return True if the 'name' field has been set, false otherwise.
      */
    public boolean hasName() {
      return fieldSetFlags()[1];
    }


    /**
      * Clears the value of the 'name' field.
      * @return This builder.
      */
    public com.purbon.kafka.streams.model.Store.Builder clearName() {
      name = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Store build() {
      try {
        Store record = new Store();
        record.store_id = fieldSetFlags()[0] ? this.store_id : (java.lang.Integer) defaultValue(fields()[0]);
        record.name = fieldSetFlags()[1] ? this.name : (java.lang.CharSequence) defaultValue(fields()[1]);
        return record;
      } catch (java.lang.Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumWriter<Store>
    WRITER$ = (org.apache.avro.io.DatumWriter<Store>)MODEL$.createDatumWriter(SCHEMA$);

  @Override public void writeExternal(java.io.ObjectOutput out)
    throws java.io.IOException {
    WRITER$.write(this, SpecificData.getEncoder(out));
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumReader<Store>
    READER$ = (org.apache.avro.io.DatumReader<Store>)MODEL$.createDatumReader(SCHEMA$);

  @Override public void readExternal(java.io.ObjectInput in)
    throws java.io.IOException {
    READER$.read(this, SpecificData.getDecoder(in));
  }

}
