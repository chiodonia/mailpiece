package ch.post.logistics.mailpiece.apps.streaming

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.contains
import org.springframework.http.HttpHeaders
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.netty.http.HttpProtocol
import reactor.netty.http.client.HttpClient
import java.util.stream.Collectors
import java.util.stream.StreamSupport


class KsqlService(ksqldbUrl: String) {

    private val mapper = JsonMapper.builder()
        .findAndAddModules()
        .build();

    private val client = WebClient
        .builder()
        .clientConnector(
            ReactorClientHttpConnector(
                HttpClient.create()
                    .wiretap(true)
                    .protocol(HttpProtocol.H2C)
            )
        )
        .baseUrl(ksqldbUrl)
        .build()

    fun execute(
        statements: List<String>,
        streamsProperties: Map<String, String> = mapOf(),
        sessionVariables: Map<String, String> = mapOf(),
    ): Mono<JsonNode> {
        val response = client
            .post().uri("/ksql")
            .header(HttpHeaders.ACCEPT, "application/vnd.ksql.v1+json")
            .body(
                BodyInserters.fromValue(
                    ExecuteStatementsRequest(
                        statements.stream().collect(Collectors.joining("; ")) + ";",
                        streamsProperties,
                        sessionVariables
                    )
                )
            )
            .retrieve()
        return response.bodyToMono(JsonNode::class.java)
    }

    fun query(
        sql: String,
        sessionVariables: Map<String, String>,
        streamsProperties: Map<String, String>,
    ): Flux<String> {
        return client
            .post().uri("/query-stream")
            .header(HttpHeaders.ACCEPT, "application/vnd.ksqlapi.delimited.v1")
            .body(
                BodyInserters.fromValue(
                    QueryRequest(
                        "$sql;",
                        sessionVariables,
                        streamsProperties
                    )
                )
            )
            .retrieve()
            .bodyToFlux(String::class.java)
    }

    fun toJson(
        stream: Flux<String>,
    ): Flux<JsonNode> {
        return stream
            .map { mapper.readTree(it) }
            .filter { !it.contains("queryId") }
            .filter { !it.isEmpty }
            .flatMap {
                Flux.fromStream(StreamSupport.stream(it.spliterator(), false))
            }
    }

}

data class ExecuteStatementsRequest(
    val ksql: String,
    val streamsProperties: Map<String, String>,
    val sessionVariables: Map<String, String>,
)

data class QueryRequest(
    val sql: String,
    val sessionVariables: Map<String, String>,
    val streamsProperties: Map<String, String>,
)
