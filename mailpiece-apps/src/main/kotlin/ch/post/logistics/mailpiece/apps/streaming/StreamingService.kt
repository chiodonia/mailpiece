package ch.post.logistics.mailpiece.apps.streaming

import ch.post.logistics.mailpiece.v1.Mailpiece
import com.fasterxml.jackson.databind.json.JsonMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import javax.annotation.PostConstruct

@Service
class StreamingService(val ksql: KsqlService) {

    private val logger = LoggerFactory.getLogger(StreamingService::class.java)

    private val mapper = JsonMapper.builder()
        .findAndAddModules()
        .build();

    data class Delivered(val id: String, val timestamp: String, val delivered: String)

    fun delivered(zip: String): Flux<Delivered> {
        return ksql.toJson(
            ksql.query(
                """
                        SELECT STRUCT(`id`:=ID, `timestamp`:=TIMESTAMP, `delivered`:=DELIVERED->ZIP)
                        FROM MAILPIECE_EVENT_STREAM 
                        WHERE DELIVERED IS NOT NULL AND DELIVERED->ZIP = '${zip}' 
                        EMIT CHANGES
                    """.trimIndent(),
                mapOf("zip" to zip),
                mapOf(
                    "ksql.streams.auto.offset.reset" to "latest"
                )
            )
        ).map { mapper.treeToValue(it, Delivered::class.java) }

    }

    fun findMailpiece(id: String): Mono<Mailpiece> {
        return ksql.toJson(
            ksql.query(
                """
                        SELECT STRUCT(`id`:=ID, `state`:=STATE, `priority`:=PRIORITY, `events`:=EVENTS)
                        FROM MAILPIECE_TABLE 
                        WHERE KEY = '${id}'
                    """.trimIndent(),
                mapOf("id" to id),
                mapOf(
                    "ksql.streams.auto.offset.reset" to "earliest"
                )
            )
        ).elementAt(0)
            .map { mapper.treeToValue(it, Mailpiece::class.java) }
    }

    @PostConstruct
    fun init() {
        ksql.execute(
            listOf(
                """
                    DROP TYPE IF EXISTS LOCATION
                """.trimIndent(),
                """
                    CREATE TYPE LOCATION AS STRUCT<
                        ZIP VARCHAR
                    >
                """.trimIndent(),
                """
                    DROP TYPE IF EXISTS MAILPIECE_EVENT
                """.trimIndent(),
                """
                    CREATE TYPE MAILPIECE_EVENT AS STRUCT<
                        `timestamp` VARCHAR, 
                        `zip` VARCHAR, 
                        `state` VARCHAR
                    >
                """.trimIndent(),
                """
                    CREATE OR REPLACE STREAM MAILPIECE_EVENT_STREAM (
                        KEY VARCHAR KEY, 
                        ID VARCHAR, 
                        TIMESTAMP VARCHAR, 
                        INGESTED LOCATION, 
                        DELIVERED LOCATION 
                    ) WITH (
                        KAFKA_TOPIC='logistics.Mailpiece-event', 
                        VALUE_FORMAT='JSON'
                    )
                 """.trimIndent(),
                """
                    DROP TABLE IF EXISTS MAILPIECE_TABLE
                """.trimIndent(),
                """
                    CREATE SOURCE TABLE IF NOT EXISTS MAILPIECE_TABLE (
                        KEY VARCHAR PRIMARY KEY, 
                        ID VARCHAR, 
                        STATE VARCHAR, 
                        PRIORITY VARCHAR, 
                        EVENTS ARRAY<MAILPIECE_EVENT>
                    ) WITH (
                        KAFKA_TOPIC='logistics.Mailpiece-state', 
                        VALUE_FORMAT='JSON'
                    )
                """.trimIndent()
            ),
            mapOf(
                "ksql.streams.auto.offset.reset" to "earliest"
            )
        )
            .doOnError { logger.error("Executing statements", it) }
            .subscribe { logger.debug("Statements executed successfully: {}", it.toPrettyString()) }
    }

}

