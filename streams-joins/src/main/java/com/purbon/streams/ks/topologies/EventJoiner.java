package com.purbon.streams.ks.topologies;

import com.purbon.streams.ks.model.envelopes.MessageImpl;
import com.purbon.streams.ks.model.envelopes.MessageStatus;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.JoinWindows;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.kafka.support.KafkaStreamBrancher;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;

public class EventJoiner {

    private String thisTopic;
    private String otherTopic;
    private String targetTopic;
    private String notMatchedTopic;

    public EventJoiner(String thisTopic,
                       String otherTopic,
                       String targetTopic,
                       String notMatchedTopic) {

        this.thisTopic = thisTopic;
        this.otherTopic = otherTopic;
        this.targetTopic = targetTopic;
        this.notMatchedTopic = notMatchedTopic;
    }

    public KStream<String, MessageImpl<String>> build(StreamsBuilder builder) {

        KStream<String, String> thisStream = builder.stream(thisTopic, Consumed.with(Serdes.String(), Serdes.String()));
        KStream<String, String> otherStream = builder.stream(otherTopic, Consumed.with(Serdes.String(), Serdes.String()));


        var stream = thisStream.leftJoin(otherStream,
                (thisValue, otherValue) -> {
                    if (otherValue == null) {
                        var message = String.format("this.value=%s - not found", thisValue);
                        System.out.println("LOG: "+message);
                        return new MessageImpl<>(message, MessageStatus.FAILED);
                    }
                    var message = String.format("this.value=%s other.value=%s", thisValue, otherValue);
                    System.out.println("LOG: "+message);
                    return new MessageImpl<>(message, MessageStatus.SUCCEED);
                }, JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofSeconds(5)));

        return router().onTopOf(stream);
    }


    protected KafkaStreamBrancher<String, MessageImpl<String>> router() {
        KafkaStreamBrancher<String, MessageImpl<String>> brancher = new KafkaStreamBrancher<String, MessageImpl<String>>()
                .branch((key, envelop) -> !envelop.getStatus().equals(MessageStatus.SUCCEED), dlqBranchAction())
                .defaultBranch(defaultBranchAction());
        return brancher;
    }

    protected Consumer<? super KStream<String, MessageImpl<String>>> dlqBranchAction() {
        return  ks -> ks.mapValues(MessageImpl::getPayload).to(notMatchedTopic);
    }

    protected Consumer<? super KStream<String, MessageImpl<String>>> defaultBranchAction() {
        return ks -> ks.mapValues(MessageImpl::getPayload).to(targetTopic);
    }


}