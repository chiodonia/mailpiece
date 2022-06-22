package ch.post.logistics.mailpiece.processor.service

import ch.post.logistics.mailpiece.v1.Mailpiece
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Mono

@Controller
class MailpieceServiceRemoteStateStoreController(val service: MailpieceService) {
    companion object {
        const val FIND_MAILPIECE_BY_KEY = "findMailpiece"
    }

    @MessageMapping(FIND_MAILPIECE_BY_KEY)
    fun findMailpiece(key: String): Mono<Mailpiece> {
        return service.findMailpieceLocally(key)
    }

}