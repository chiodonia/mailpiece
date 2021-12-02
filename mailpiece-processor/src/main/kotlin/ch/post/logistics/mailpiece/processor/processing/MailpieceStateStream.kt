package ch.post.logistics.mailpiece.processor.processing

import ch.post.logistics.mailpiece.processor.Application.Companion.MAILPIECE_TABLE
import ch.post.logistics.mailpiece.v1.Event
import ch.post.logistics.mailpiece.v1.Mailpiece
import ch.post.logistics.mailpiece.v1.MailpieceEvent
import ch.post.logistics.mailpiece.v1.MailpieceState
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.*
import org.apache.kafka.streams.processor.TimestampExtractor
import org.apache.kafka.streams.state.Stores
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.support.serializer.JsonSerde

@Configuration
class MailpieceStateStream(val objectMapper: ObjectMapper) {

    @Bean
    fun mailpieceEventStreamToTable(builder: StreamsBuilder): KTable<String, Mailpiece>? {
        val mailpieceTable: KTable<String, Mailpiece> = builder.table(
            "logistics.Mailpiece-state",
            Consumed.with(
                Serdes.String(),
                JsonSerde(Mailpiece::class.java, objectMapper).noTypeInfo()
            ).withName("mailpiece-state-table"),
            Materialized.`as`<String, Mailpiece>(Stores.persistentKeyValueStore(MAILPIECE_TABLE))
                .withKeySerde(Serdes.String())
                .withValueSerde(JsonSerde(Mailpiece::class.java, objectMapper).noTypeInfo())
        )
        builder.stream(
            "logistics.Mailpiece-event",
            Consumed.with(
                Serdes.String(),
                JsonSerde(MailpieceEvent::class.java, objectMapper).noTypeInfo()
            ).withTimestampExtractor(MailpieceEventTimestampExtractor()).withName("mailpiece-event-stream")
        ).leftJoin(
            mailpieceTable,
            MailpieceValueJoiner(),
            Joined.`as`("join-mailpiece-event-stream-with-mailpiece-table")
        ).to(
            "logistics.Mailpiece-state",
            Produced.with(
                Serdes.String(),
                JsonSerde(Mailpiece::class.java, objectMapper).noTypeInfo()
            ).withName("mailpiece-state-stream")
        )

        return mailpieceTable
    }

    class MailpieceValueJoiner : ValueJoiner<MailpieceEvent, Mailpiece?, Mailpiece> {

        val logger: Logger = LoggerFactory.getLogger(MailpieceValueJoiner::class.java)

        override fun apply(mailpieceEvent: MailpieceEvent, mailpiece: Mailpiece?): Mailpiece {
            logger.debug("Joining {} with {}", mailpieceEvent, mailpiece)
            val newMailpiece = mailpiece ?: Mailpiece()
            newMailpiece.withId(mailpieceEvent.id)
            if (mailpieceEvent.ingested != null) {
                newMailpiece.state = MailpieceState.INGESTED
                newMailpiece.product = mailpieceEvent.ingested.product
                newMailpiece.events.add(
                    Event()
                        .withZip(mailpieceEvent.ingested.zip)
                        .withTimestamp(mailpieceEvent.timestamp)
                        .withState(MailpieceState.INGESTED)
                )
            }
            if (mailpieceEvent.delivered != null) {
                newMailpiece.state = MailpieceState.DELIVERED
                newMailpiece.events.add(
                    Event()
                        .withZip(mailpieceEvent.delivered.zip)
                        .withTimestamp(mailpieceEvent.timestamp)
                        .withState(MailpieceState.DELIVERED)
                )
            }
            return newMailpiece
        }
    }

    class MailpieceEventTimestampExtractor : TimestampExtractor {
        override fun extract(record: ConsumerRecord<Any, Any>, timestamp: Long): Long {
            return if (record.value() is MailpieceEvent) {
                (record.value() as MailpieceEvent).timestamp.toEpochSecond()
            } else {
                timestamp
            }
        }
    }
}