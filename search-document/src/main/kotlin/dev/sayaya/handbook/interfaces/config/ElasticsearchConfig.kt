package dev.sayaya.handbook.interfaces.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchConfiguration
import org.springframework.data.elasticsearch.repository.config.EnableReactiveElasticsearchRepositories

/**
 * Elasticsearch 클라이언트 및 리액티브 리포지토리 설정.
 */
@Configuration
@EnableReactiveElasticsearchRepositories(basePackages = ["dev.sayaya.handbook.interfaces.database"])
class ElasticsearchConfig : ReactiveElasticsearchConfiguration() {

    @Value("\${spring.elasticsearch.uris:localhost:9200}")
    private lateinit var esUris: String

    override fun clientConfiguration(): ClientConfiguration {
        return ClientConfiguration.builder()
            .connectedTo(esUris)
            .build()
    }
}
