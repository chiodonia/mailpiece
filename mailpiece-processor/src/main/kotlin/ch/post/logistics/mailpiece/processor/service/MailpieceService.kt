package ch.post.logistics.mailpiece.processor.service

import ch.post.logistics.mailpiece.processor.Application.Companion.MAILPIECE_TABLE
import ch.post.logistics.mailpiece.processor.Application.Companion.QUERY_REMOTE_STATE_STORE_BY_KEY
import ch.post.logistics.mailpiece.processor.service.remote.QueryStateStoreByKeyRequest
import ch.post.logistics.mailpiece.v1.Mailpiece
import org.apache.kafka.streams.KeyQueryMetadata
import org.apache.kafka.streams.state.HostInfo
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class MailpieceService(val kafkaStreamsService: KafkaStreamsService, val builder: RSocketRequester.Builder) {

    private val logger = LoggerFactory.getLogger(MailpieceService::class.java)

    operator fun get(id: String): Mono<Mailpiece?> {
        logger.debug(
            "Local hostInfo is {}:{}",
            kafkaStreamsService.hostInfo().host(),
            kafkaStreamsService.hostInfo().port()
        )
        val metadata: KeyQueryMetadata =
            kafkaStreamsService.metadata(MAILPIECE_TABLE, id) ?: throw NoSuchElementException()
        return if (kafkaStreamsService.hostInfo() == metadata.activeHost()) {
            getLocal(MAILPIECE_TABLE, id)
        } else {
            if (metadata.activeHost() == HostInfo.unavailable()) {
                logger.warn("Host metadata is currently unavailable")
                throw RuntimeException("Host metadata is currently unavailable")
            } else {
                getRemote(metadata.activeHost(), MAILPIECE_TABLE, id)
            }
        }
    }

    fun getLocal(store: String, id: String): Mono<Mailpiece?> {
        logger.debug(
            "Query local state-store {} for mailpiece id {}",
            store,
            id
        )
        val mailpiece = kafkaStreamsService.store(store)!!.get(id)
        return if (mailpiece == null) {
            Mono.empty()
        } else {
            Mono.just(mailpiece as Mailpiece)
        }
    }

    private fun getRemote(hostInfo: HostInfo, store: String, id: String): Mono<Mailpiece?> {
        logger.debug(
            "Query remote state-store {} on {}:{} for mailpiece id {}",
            store,
            hostInfo.host(),
            hostInfo.port(),
            id
        )
        val requester = builder.tcp(hostInfo.host(), hostInfo.port())
        return requester
            .route(QUERY_REMOTE_STATE_STORE_BY_KEY)
            .data(
                QueryStateStoreByKeyRequest(
                    kafkaStreamsService.hostInfo().host(),
                    kafkaStreamsService.hostInfo().port(),
                    store,
                    id
                )
            )
            .retrieveMono(object : ParameterizedTypeReference<Mailpiece>() {})
            .doOnError { throwable ->
                logger.error(
                    "{} failed",
                    QUERY_REMOTE_STATE_STORE_BY_KEY,
                    throwable
                )
            }
            .onErrorMap { throwable -> NoSuchElementException("$id not found") }
    }

}