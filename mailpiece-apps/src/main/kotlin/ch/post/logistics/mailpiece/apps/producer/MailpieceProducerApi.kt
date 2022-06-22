package ch.post.logistics.mailpiece.apps.producer

import ch.post.logistics.mailpiece.v1.MailpieceEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/producer/generate")
class MailpieceProducerApi(val service: MailpieceProducerService) {

    @GetMapping(params = ["nr", "from", "delay"])
    fun generate(
        @RequestParam("nr") nr: Int,
        @RequestParam("from") from: Int,
        @RequestParam("delay") delay: Long,
    ): Flow<String> {
        return service.generate(nr, from, delay).map { event -> toString(event) }
    }

    private fun toString(event: MailpieceEvent): String {
        return "${event.id} - ${if (event.ingested != null) "INGESTED" else "DELIVERED"}<br>"
    }

}
