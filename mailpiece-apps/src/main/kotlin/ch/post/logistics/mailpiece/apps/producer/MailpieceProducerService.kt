package ch.post.logistics.mailpiece.apps.producer

import ch.post.logistics.mailpiece.v1.Delivered
import ch.post.logistics.mailpiece.v1.Ingested
import ch.post.logistics.mailpiece.v1.MailpieceEvent
import ch.post.logistics.mailpiece.v1.Priority
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import kotlin.random.Random

@Service
class MailpieceProducerService(kafkaProperties: KafkaProperties) {

    private val logger = LoggerFactory.getLogger(MailpieceProducerService::class.java)

    private val zip = listOf("6982", "6500", "6900", "3030", "1000", "4040", "2020", "9000", "1212", "5050")
    private val products = listOf(Priority.A, Priority.B)
    private val producer = KafkaProducer(
        kafkaProperties.buildProducerProperties(),
        StringSerializer(),
        jsonSerializer<MailpieceEvent>()
    )

    fun generate(nr: Int, from: Int, delay: Long): Flow<MailpieceEvent> = channelFlow {
        for (i in from until from + nr) {
            launch {
                val id = id(i)
                delay(Random.nextLong(delay))
                val ingested = ingested(id)
                produce(ingested)
                send(ingested)

                delay(Random.nextLong(delay))
                val delivered = delivered(id)
                produce(delivered)
                send(delivered)
            }
        }
    }

    private fun ingested(id: String): MailpieceEvent {
        return MailpieceEvent()
            .withId(id)
            .withTimestamp(ZonedDateTime.now())
            .withIngested(
                Ingested()
                    .withZip(zip())
                    .withPriority(priority())
            )
    }

    private fun delivered(id: String): MailpieceEvent {
        return MailpieceEvent()
            .withId(id)
            .withTimestamp(ZonedDateTime.now())
            .withDelivered(
                Delivered().withZip(zip())
            )
    }

    private fun produce(event: MailpieceEvent) {
        producer.send(ProducerRecord("logistics.Mailpiece-event", event.id, event)) { metadata, exeption ->
            if (metadata.hasOffset()) {
                logger.trace(
                    "Event {} produced: {}/{}@{}",
                    event,
                    metadata!!.topic(),
                    metadata.partition(),
                    metadata.offset()
                )
            } else {
                logger.error("Unable to produced event {}", event, exeption)
            }
        }
    }

    private fun id(i: Int): String {
        return "99000000${i.toString().padStart(10, '0')}"
    }

    private fun zip(): String {
        return zip[Random.nextInt(0, zip.size)]
    }

    private fun priority(): Priority {
        return products[Random.nextInt(0, products.size)]
    }

    private fun <T> jsonSerializer(): JsonSerializer<T> {
        return JsonSerializer<T>(
            ObjectMapper()
                .registerModule(JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        )
            .noTypeInfo()
    }

}
