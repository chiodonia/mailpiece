package ch.post.logistics.mailpiece.processor.service.remote

import ch.post.logistics.mailpiece.processor.Application.Companion.QUERY_REMOTE_STATE_STORE_BY_KEY
import ch.post.logistics.mailpiece.processor.service.MailpieceService
import ch.post.logistics.mailpiece.v1.Mailpiece
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Mono

@Controller
class RemoteStateStoreController(val mailpieceService: MailpieceService) {

    val logger: Logger = LoggerFactory.getLogger(RemoteStateStoreController::class.java)

    @MessageMapping(QUERY_REMOTE_STATE_STORE_BY_KEY)
    fun queryByKey(request: QueryStateStoreByKeyRequest): Mono<Mailpiece?> {
        logger.debug("Query by key: {}", request)
        return mailpieceService.getLocal(request.store, request.id)
    }

}