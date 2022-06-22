package ch.post.logistics.mailpiece.apps.streaming

import ch.post.logistics.mailpiece.v1.Mailpiece
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SubscriptionMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Controller
class StreamingController(val service: StreamingService) {

    @SubscriptionMapping
    fun delivered(@Argument zip: String, @Argument take: Long): Flux<StreamingService.Delivered> {
        return service.delivered(zip)
            .take(take)
    }

    @QueryMapping
    fun findMailpiece(@Argument id: String): Mono<Mailpiece> {
        return service.findMailpiece(id)
    }

}

