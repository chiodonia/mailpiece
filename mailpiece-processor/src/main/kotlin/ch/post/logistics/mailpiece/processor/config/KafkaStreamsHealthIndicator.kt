package ch.post.logistics.mailpiece.processor.config

import org.apache.kafka.streams.KafkaStreams
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.ReactiveHealthIndicator
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class KafkaStreamsHealthIndicator(val kafkaStreamsService: KafkaStreamsService) : ReactiveHealthIndicator {
    val logger: Logger = LoggerFactory.getLogger(KafkaStreamsHealthIndicator::class.java)
    override fun health(): Mono<Health> {
        val state = kafkaStreamsService.state()
        return if (state == KafkaStreams.State.RUNNING) {
            logger.debug("KStream state is {}", state)
            Mono.just(
                Health.Builder()
                    .up()
                    .build()
            )
        } else {
            logger.warn("KStream state is {}", state)
            Mono.just(
                Health.Builder()
                    .down()
                    .withDetail("KStreams", state)
                    .build()
            )
        }
    }
}