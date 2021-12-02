package ch.post.logistics.mailpiece.processor.api

import ch.post.logistics.mailpiece.processor.service.KafkaStreamsService
import org.apache.kafka.streams.state.KeyValueIterator
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/kstreams")
class KafkaStreamsController(val kafkaStreamsService: KafkaStreamsService) {

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

    @GetMapping("/stores/{name}/get")
    fun store(@PathVariable name: String): KeyValueIterator<String, *> {
        return kafkaStreamsService.store(name)!!.all()
    }

    @GetMapping(value = ["/stores/{name}/get"], params = ["key"])
    fun stores(@PathVariable name: String, @RequestParam(value = "key") key: String): Any? {
        return kafkaStreamsService.store(name, key)
    }

}