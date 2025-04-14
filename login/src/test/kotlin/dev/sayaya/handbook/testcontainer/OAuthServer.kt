package dev.sayaya.handbook.testcontainer

import org.springframework.test.context.DynamicPropertyRegistry
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName

internal class OAuthServer {
    companion object {
        const val PROVIDER = "test-provider"
        const val USER = "test-user"
        const val TOKEN = "SUCCESS_TOKEN"
    }
    private val oauthServer = GenericContainer(DockerImageName.parse("ghcr.io/navikt/mock-oauth2-server:2.1.10")).withExposedPorts(8080)
        .waitingFor(Wait.forHttp("/$USER/.well-known/openid-configuration").forStatusCode(200))
        .waitingFor(Wait.forHttp("/test-admin/.well-known/openid-configuration").forStatusCode(200))
    init {
        oauthServer.start()
    }
    @Suppress("HttpUrlsUsage")
    fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
        val baseUrl = "${oauthServer.host}:${oauthServer.getMappedPort(8080)}"
        registry.add("spring.security.oauth2.client.registration.$PROVIDER.client-id") { "test-client" }
        registry.add("spring.security.oauth2.client.registration.$PROVIDER.client-secret") { "test-secret" }
        registry.add("spring.security.oauth2.client.registration.$PROVIDER.scope") { "openid" }
        registry.add("spring.security.oauth2.client.registration.$PROVIDER.authorization-grant-type") { "authorization_code" }
        registry.add("spring.security.oauth2.client.registration.$PROVIDER.redirect-uri") { "{baseUrl}/login/oauth2/code/{registrationId}" }
        registry.add("spring.security.oauth2.client.provider.$PROVIDER.authorization-uri") { "http://$baseUrl/$USER/authorize" }
        registry.add("spring.security.oauth2.client.provider.$PROVIDER.token-uri") { "http://$baseUrl/$USER/token" }
        registry.add("spring.security.oauth2.client.provider.$PROVIDER.jwk-set-uri") { "http://$baseUrl/$USER/jwks" }
    }
}