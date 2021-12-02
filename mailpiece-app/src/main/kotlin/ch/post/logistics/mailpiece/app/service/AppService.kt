package ch.post.logistics.mailpiece.app.service

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import java.util.stream.Collectors
import javax.annotation.PostConstruct

@Service
class AppService(val client: WebClient) {

    @PostConstruct
    fun init() {
        print(
            executeStatements(
                listOf(
                    "CREATE OR REPLACE STREAM MAILPIECE_STREAM (KEY VARCHAR KEY, id VARCHAR, timestamp VARCHAR, ingested STRUCT<zip VARCHAR>, delivered STRUCT<zip VARCHAR>) WITH (KAFKA_TOPIC='logistics.Mailpiece-event', VALUE_FORMAT='JSON')",
                    "CREATE SOURCE TABLE IF NOT EXISTS MAILPIECE_TABLE (KEY VARCHAR PRIMARY KEY, id VARCHAR, state VARCHAR, product VARCHAR, events ARRAY<STRUCT<timestamp VARCHAR, zip VARCHAR, state VARCHAR>>) WITH (KAFKA_TOPIC='logistics.Mailpiece-state', VALUE_FORMAT='JSON')",
                ),
                mapOf(
                    "ksql.streams.auto.offset.reset" to "earliest"
                )
            )
        )
    }

    fun executeStatements(statements: List<String>, streamsProperties: Map<String, String>): JsonNode? {
        val response = client
            .post().uri("/ksql")
            .header(HttpHeaders.ACCEPT, "application/vnd.ksql.v1+json")
            .header(HttpHeaders.CONTENT_TYPE, "application/json")
            .body(
                BodyInserters.fromValue(
                    ExecuteStatementsRequest(
                        statements.stream().collect(Collectors.joining("; ")) + ";",
                        streamsProperties
                    )
                )
            )
            .retrieve()
        return response.bodyToMono(JsonNode::class.java).block()
    }

    fun queryStream(
        sql: String,
        properties: Map<String, String>,
        sessionVariables: Map<String, String>,
        streamsProperties: Map<String, String>
    ): Flux<String> {
        val response = client
            .post().uri("/query-stream")
            .header(HttpHeaders.ACCEPT, "application/vnd.ksqlapi.delimited.v1")
            .header(HttpHeaders.CONTENT_TYPE, "application/json")
            .body(
                BodyInserters.fromValue(
                    QueryStreamRequest(
                        "$sql;",
                        properties,
                        sessionVariables,
                        streamsProperties
                    )
                )
            )
            .retrieve()
        return response.bodyToFlux(String::class.java)
    }

}

data class ExecuteStatementsRequest(
    val ksql: String,
    val streamsProperties: Map<String, String>
)

data class QueryStreamRequest(
    val sql: String,
    val properties: Map<String, String>,
    val sessionVariables: Map<String, String>,
    val streamsProperties: Map<String, String>
)
