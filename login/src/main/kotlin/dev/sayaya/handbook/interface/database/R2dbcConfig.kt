package dev.sayaya.handbook.`interface`.database

import org.springframework.context.annotation.Configuration

import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@Configuration
@EnableR2dbcRepositories
class R2dbcConfig