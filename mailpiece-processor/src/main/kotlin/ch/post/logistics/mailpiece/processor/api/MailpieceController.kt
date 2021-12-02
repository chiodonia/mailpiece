package ch.post.logistics.mailpiece.processor.api

import ch.post.logistics.mailpiece.processor.service.MailpieceService
import ch.post.logistics.mailpiece.v1.Mailpiece
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/mailpiece")
class MailpieceController(val mailpieceService: MailpieceService) {

    private val logger: Logger = LoggerFactory.getLogger(MailpieceController::class.java)

    @GetMapping(params = ["id"])
    fun mailpiece(@RequestParam("id") id: String): Mono<Mailpiece?> {
        logger.debug("Querying mailpiece {}", id)
        return mailpieceService[id]
    }

}