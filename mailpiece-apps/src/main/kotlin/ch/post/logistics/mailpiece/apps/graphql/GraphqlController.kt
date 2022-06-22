package ch.post.logistics.mailpiece.apps.graphql

import org.springframework.graphql.data.method.annotation.BatchMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SubscriptionMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.LocalDateTime
import java.util.stream.Stream

@Controller
class GreetingsController {

    @SubscriptionMapping
    fun greetings(): Flux<Greeting> {
        return Flux.fromStream(Stream.generate { Greeting("Hello!", LocalDateTime.now()) })
            .delayElements(Duration.ofMillis(500))
            .take(10)
    }

    @QueryMapping
    fun greeting(): Greeting {
        return Greeting("Hello!", LocalDateTime.now())
    }

    @BatchMapping
    fun greetingFrom(greetings: List<Greeting>): Mono<Map<Greeting, List<From>>> {
        return Mono.just(greetings.associateBy({ it }, { listOf(From("Me"), From("You")) }))
    }

}

data class Greeting(val greeting: String, val timestamp: LocalDateTime)
data class From(val name: String)