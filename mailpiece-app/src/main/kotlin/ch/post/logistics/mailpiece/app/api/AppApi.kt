package ch.post.logistics.mailpiece.app.api

import ch.post.logistics.mailpiece.app.service.AppService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
class AppApi(val service: AppService) {
    @GetMapping("/ingested", params = ["zip"])
    fun stream(@RequestParam("zip") zip: String): Flux<String> {
        return service.queryStream(
            "SELECT * FROM MAILPIECE_STREAM WHERE INGESTED IS NOT NULL AND INGESTED->ZIP = '\${zip}' EMIT CHANGES",
            mapOf(),
            mapOf("zip" to zip),
            mapOf(
                "ksql.streams.auto.offset.reset" to "latest"
            )
        )
    }

    @GetMapping("/mailpiece", params = ["id"])
    fun pull(@RequestParam("id") id: String): Flux<String> {
        return service.queryStream(
            "SELECT * FROM MAILPIECE_TABLE WHERE KEY = '\${id}'",
            mapOf(),
            mapOf("id" to id),
            mapOf(
                "ksql.streams.auto.offset.reset" to "earliest"
            )
        )
    }
}