package ch.post.logistics.mailpiece.processor.api

import ch.post.logistics.mailpiece.processor.service.MailpieceService
import ch.post.logistics.mailpiece.v1.Mailpiece
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Mono

@Controller
class MailpieceApi(val mailpieceService: MailpieceService) {

    @QueryMapping
    fun findMailpiece(@Argument id: String): Mono<Mailpiece> {
        return mailpieceService.findMailpiece(id)
    }

}