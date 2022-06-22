package ch.post.logistics.mailpiece.processor.processing

import ch.post.logistics.mailpiece.processor.processing.MailpieceProcessor.Companion.MAILPIECE_STORE
import ch.post.logistics.mailpiece.v1.*
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.TestInputTopic
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.test.TestRecord
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.kafka.support.serializer.JsonSerializer
import java.time.ZonedDateTime
import java.util.*

class MailpieceProcessorTests {

    private val config: Properties = mapOf(
        StreamsConfig.APPLICATION_ID_CONFIG to "mailpiece-processor",
        StreamsConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
        StreamsConfig.APPLICATION_SERVER_CONFIG to "localhost:7070",
        StreamsConfig.PROCESSING_GUARANTEE_CONFIG to StreamsConfig.EXACTLY_ONCE_V2,
        StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG to Serdes.String().javaClass.name,
        StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG to Serdes.String().javaClass.name,
    ).toProperties()

    private lateinit var testDriver: TopologyTestDriver
    private lateinit var mailpieceTable: KeyValueStore<String, Mailpiece>
    private lateinit var mailpieceEventTestInputTopic: TestInputTopic<String, MailpieceEvent>

    @BeforeEach
    fun beforeEach() {
        val builder = StreamsBuilder()
        val mailpieceProcessor = MailpieceProcessor(
            ObjectMapper()
                .registerModule(JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        )
        mailpieceProcessor.mailpieceEventStreamToTable(builder)
        testDriver = TopologyTestDriver(builder.build(), config)

        mailpieceEventTestInputTopic = testDriver.createInputTopic(
            "logistics.Mailpiece-event",
            Serdes.String().serializer(),
            JsonSerializer()
        )
        mailpieceTable = testDriver.getKeyValueStore(MAILPIECE_STORE)
    }

    @Test
    fun test_mailpiece_events_are_processed_into_mailpiece_state() {
        val ingested = MailpieceEvent()
            .withId("99000000001")
            .withTimestamp(ZonedDateTime.now())
            .withIngested(
                Ingested()
                    .withPriority(Priority.A)
                    .withZip("7000")
            )
        mailpieceEventTestInputTopic.pipeInput(TestRecord(ingested.id, ingested))
        Assertions.assertNotNull(mailpieceTable[ingested.id])
        Assertions.assertEquals(ingested.id, mailpieceTable[ingested.id].id)
        Assertions.assertEquals(MailpieceState.INGESTED, mailpieceTable[ingested.id].state)
        Assertions.assertEquals(ingested.ingested.priority, mailpieceTable[ingested.id].priority)
        Assertions.assertEquals(1, mailpieceTable[ingested.id].events.count())
        Assertions.assertEquals(MailpieceState.INGESTED, mailpieceTable[ingested.id].events[0].state)
        Assertions.assertEquals(ingested.ingested.zip, mailpieceTable[ingested.id].events[0].zip)

        val delivered = MailpieceEvent()
            .withId(ingested.id)
            .withTimestamp(ZonedDateTime.now())
            .withDelivered(
                Delivered()
                    .withZip("8000")
            )
        mailpieceEventTestInputTopic.pipeInput(TestRecord(delivered.id, delivered))
        Assertions.assertEquals(MailpieceState.DELIVERED, mailpieceTable[delivered.id].state)
        Assertions.assertEquals(ingested.ingested.priority, mailpieceTable[delivered.id].priority)
        Assertions.assertEquals(2, mailpieceTable[delivered.id].events.count())
        Assertions.assertEquals(MailpieceState.DELIVERED, mailpieceTable[delivered.id].events[1].state)
        Assertions.assertEquals(delivered.delivered.zip, mailpieceTable[delivered.id].events[1].zip)
    }

    @AfterEach
    fun after() {
        testDriver.close()
    }
}