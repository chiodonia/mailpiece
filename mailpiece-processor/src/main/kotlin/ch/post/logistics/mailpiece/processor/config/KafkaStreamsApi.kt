package ch.post.logistics.mailpiece.processor.config

import org.apache.kafka.streams.query.KeyQuery
import org.apache.kafka.streams.query.RangeQuery
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/kstreams")
class KafkaStreamsApi(val kafkaStreamsService: KafkaStreamsService) {

    @GetMapping("/topology")
    fun topology(): String {
        return kafkaStreamsService.topology().toString()
    }

    @GetMapping("/state")
    fun state(): String {
        return kafkaStreamsService.state().name
    }

    @GetMapping("/stores")
    fun stores(): Set<String> {
        return kafkaStreamsService.stores()
    }

    @GetMapping("/stores/{name}/data")
    fun store(@PathVariable name: String): Any? {
        return kafkaStreamsService.store(name, RangeQuery.withNoBounds())
    }

    @GetMapping(value = ["/stores/{name}/data"], params = ["key"])
    fun stores(@PathVariable name: String, @RequestParam(value = "key") key: String): Any? {
        return kafkaStreamsService.store(name, KeyQuery.withKey(key))
    }

}