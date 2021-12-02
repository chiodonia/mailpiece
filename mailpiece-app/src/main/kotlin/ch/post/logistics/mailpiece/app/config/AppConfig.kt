package ch.post.logistics.mailpiece.app.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.HttpProtocol
import reactor.netty.http.client.HttpClient

@Configuration
class AppConfig {
    @Value("\${ksqldb.url}")
    var ksqldbUrl: String = ""

    @Bean
    fun webClient(): WebClient {
        return WebClient
            .builder()
            .clientConnector(
                ReactorClientHttpConnector(
                    HttpClient.create()
                        .wiretap(true)
                        .protocol(HttpProtocol.H2C)
                )
            )
            .baseUrl(ksqldbUrl)
            .build()
    }
}
