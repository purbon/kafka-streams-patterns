package com.purbon.kafka.streams.topologies;

import com.purbon.kafka.streams.model.Card;
import com.purbon.kafka.streams.model.Store;
import com.purbon.kafka.streams.model.Transaction;
import com.purbon.kafka.streams.model.TransactionE;
import com.purbon.kafka.streams.model.Tuple;
import com.purbon.kafka.streams.serdes.CustomSerdes;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.JoinWindows;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.StreamJoined;
import org.apache.kafka.streams.kstream.ValueJoiner;
import org.springframework.kafka.support.KafkaStreamBrancher;

import java.time.Duration;
import java.util.Map;

public class TransactionTopologyBuilder implements TopologyBuilder<Long, TransactionE> {

    public static final String TRANSACTION_TOPIC = "transactions";
    public static final String CREDITCARDS_TOPIC = "creditCards";
    public static final String STORES_TOPIC = "stores";

    public static final String PENDING_TOPIC = "pending";
    public static final String FINAL_TOPIC = "finance";

    private final Serde<Transaction> txSerde;
    private final Serde<TransactionE> txESerde;
    private final Serde<Card> cardSerde;
    private final Serde<Store> storeSerde;
    private final Duration durationInSeconds;

    public TransactionTopologyBuilder(CustomSerdes customSerdes, Map<String, ?> serdesConfig, long durationInSeconds) {
       this.txSerde = customSerdes.transactionSerde(serdesConfig, false);
       this.txESerde = customSerdes.transactionESerde(serdesConfig, false);

       this.cardSerde = customSerdes.cardSerde(serdesConfig, false);
       this.storeSerde = customSerdes.storeSerde(serdesConfig, false);

       this.durationInSeconds = Duration.ofSeconds(durationInSeconds);
    }

    @Override
    public KStream<Long, TransactionE> build(StreamsBuilder builder) {

        var transactionStream = builder.stream(TRANSACTION_TOPIC, Consumed.with(Serdes.Long(), txSerde));

        var creditCardLookupTable = transactionStream
                .map((txId, transaction) -> KeyValue.pair(transaction.getCardId(), txId))
                .toTable(Named.as("CreditCardLookup"), Materialized.with(Serdes.Long(), Serdes.Long()));

        var storeLookupTable = transactionStream
                .map((txId, transaction) -> KeyValue.pair(transaction.getStoreId(), txId))
                .toTable(Named.as("StoreLookup"), Materialized.with(Serdes.Integer(), Serdes.Long()));

        var creditCards = buildCreditCardStream(builder, creditCardLookupTable, cardSerde);

        var stores = buildStoresStream(builder, storeLookupTable, storeSerde);

        var enrichedTxs = transactionStream
                .mapValues(tx -> new TransactionE(tx.getTransactionId(), "", ""))
                .leftJoin(creditCards, new ValueJoiner<TransactionE, Card, TransactionE>() {
                    @Override
                    public TransactionE apply(TransactionE transactionE, Card card) {
                        if (card != null) {
                            transactionE.setCardOwner(card.getOwner());
                        }
                        return transactionE;
                    }
                }, JoinWindows.of(durationInSeconds), StreamJoined.with(Serdes.Long(), txESerde, cardSerde))
                .leftJoin(stores, new ValueJoiner<TransactionE, Store, TransactionE>() {
                    @Override
                    public TransactionE apply(TransactionE tx, Store store) {
                        if (store != null) {
                            tx.setStoreName(store.getName());
                        }
                        return tx;
                    }
                }, JoinWindows.of(durationInSeconds), StreamJoined.with(Serdes.Long(), txESerde, storeSerde));

        return new KafkaStreamBrancher<Long, TransactionE>()
                .branch( (k,v) -> v.getCardOwner().toString().isEmpty() || v.getStoreName().toString().isEmpty(), ks -> ks.process(DropAllProcessor::new))
                .defaultBranch( ks -> ks.to(FINAL_TOPIC, Produced.with(Serdes.Long(), txESerde)))
                .onTopOf(enrichedTxs);
    }

    private KStream<Long, Store> buildStoresStream(StreamsBuilder builder,
                                                   KTable<Integer, Long> storeLookupTable,
                                                   Serde<Store> storeSerde) {

        return builder.stream(STORES_TOPIC, Consumed.with(Serdes.Integer(), storeSerde))
                      .join(storeLookupTable, (store, txId) -> new Tuple<>(txId, store))
                      .map((integer, tuple) -> KeyValue.pair(tuple.getLeft(), tuple.getRight()));
    }

    private KStream<Long, Card> buildCreditCardStream(StreamsBuilder builder,
                                                      KTable<Long, Long> creditCardLookupTable,
                                                      Serde<Card> cardSerde) {
        return builder.stream(CREDITCARDS_TOPIC, Consumed.with(Serdes.Long(), cardSerde))
                .join(creditCardLookupTable, new ValueJoiner<Card, Long, Tuple<Long, Card>>() {
                    @Override
                    public Tuple<Long, Card> apply(Card card, Long txId) {
                        return new Tuple<>(txId, card);
                    }
                }).map(new KeyValueMapper<Long, Tuple<Long, Card>, KeyValue<Long, Card>>() {
                    @Override
                    public KeyValue<Long, Card> apply(Long cardId, Tuple<Long, Card> cardTuple) {
                        return KeyValue.pair(cardTuple.getLeft(), cardTuple.getRight());
                    }
                });
    }
}
