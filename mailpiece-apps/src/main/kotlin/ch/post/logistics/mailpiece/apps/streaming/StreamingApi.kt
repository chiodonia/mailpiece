package ch.post.logistics.mailpiece.apps.streaming

import ch.post.logistics.mailpiece.v1.Mailpiece
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
class StreamingApi(val service: StreamingService) {

    @GetMapping("/streaming/delivered", params = ["zip", "take"])
    fun delivered(
        @RequestParam("zip") zip: String,
        @RequestParam(name = "take") take: Long,
    ): Flux<StreamingService.Delivered> {
        return service.delivered(zip)
            .take(take)
    }

    @GetMapping("/streaming/mailpiece", params = ["id"])
    fun findMailpiece(@RequestParam("id") id: String): Mono<Mailpiece> {
        return service.findMailpiece(id)
    }

}