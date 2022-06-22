package ch.post.logistics.mailpiece.apps.streaming

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class StreamingConfig {

    @Value("\${apps.ksqldb.url}")
    var ksqldbUrl: String = ""

    @Bean
    fun KsqlService(): KsqlService {
        return KsqlService(ksqldbUrl)
    }

}
