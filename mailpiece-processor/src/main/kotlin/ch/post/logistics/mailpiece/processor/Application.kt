package ch.post.logistics.mailpiece.processor

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class Application {
    companion object {
        const val MAILPIECE_TABLE = "mailpiece-table"
        const val QUERY_REMOTE_STATE_STORE_BY_KEY = "query-remote-state-store-by-key"
    }
}

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

