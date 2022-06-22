package ch.post.logistics.mailpiece.processor.service

import ch.post.logistics.mailpiece.processor.processing.MailpieceProcessor.Companion.MAILPIECE_STORE
import ch.post.logistics.mailpiece.processor.service.MailpieceServiceRemoteStateStoreController.Companion.FIND_MAILPIECE_BY_KEY
import ch.post.logistics.mailpiece.v1.Mailpiece
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.KeyQueryMetadata
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.query.KeyQuery
import org.apache.kafka.streams.query.StateQueryRequest
import org.apache.kafka.streams.state.HostInfo
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.kafka.config.StreamsBuilderFactoryBean
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class MailpieceService(
    val streamsBuilderFactoryBean: StreamsBuilderFactoryBean,
    val builder: RSocketRequester.Builder
) {

    private val logger = LoggerFactory.getLogger(MailpieceService::class.java)

    fun findMailpiece(id: String): Mono<Mailpiece> {
        val metadata: KeyQueryMetadata =
            metadata(id)
        return if (hostInfo() == metadata.activeHost()) {
            findMailpieceLocally(id)
        } else {
            if (metadata.activeHost() == HostInfo.unavailable()) {
                logger.warn("Host metadata is currently unavailable")
                throw RuntimeException("Host metadata is currently unavailable")
            } else {
                findMailpieceRemotely(metadata.activeHost(), id)
            }
        }
    }

    fun findMailpieceLocally(key: String): Mono<Mailpiece> {
        logger.debug(
            "Querying local state-store {}: key={}",
            MAILPIECE_STORE,
            key
        )

        //onlyPartitionResult returns an exception is no elements are found!
        return try {
            return Mono.just(
                kafkaStreams().query<Mailpiece>(
                    StateQueryRequest.inStore(MAILPIECE_STORE)
                        .withQuery(KeyQuery.withKey(key))
                ).onlyPartitionResult.result
            )
        } catch (e: IllegalArgumentException) {
            Mono.empty()
        }

    }

    private fun findMailpieceRemotely(hostInfo: HostInfo, key: String): Mono<Mailpiece> {
        logger.debug(
            "Querying state-store {} hosted on {}:{}: key={}",
            MAILPIECE_STORE,
            hostInfo.host(),
            hostInfo.port(),
            key
        )
        val requester = builder.tcp(hostInfo.host(), hostInfo.port())
        return requester
            .route(FIND_MAILPIECE_BY_KEY)
            .data(key)
            .retrieveMono(object : ParameterizedTypeReference<Mailpiece>() {})
            .doOnError { throwable ->
                logger.error(
                    "{} failed",
                    FIND_MAILPIECE_BY_KEY,
                    throwable
                )
            }
            .onErrorMap { NoSuchElementException("$key not found") }
    }

    private fun hostInfo(): HostInfo {
        return HostInfo.buildFromEndpoint(streamsBuilderFactoryBean.streamsConfiguration!![StreamsConfig.APPLICATION_SERVER_CONFIG] as String)
    }

    private fun metadata(key: String): KeyQueryMetadata {
        return kafkaStreams().queryMetadataForKey(MAILPIECE_STORE, key, Serdes.String().serializer())
    }

    private fun kafkaStreams(): KafkaStreams {
        return streamsBuilderFactoryBean.kafkaStreams!!
    }

}