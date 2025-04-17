package dev.sayaya.handbook.`interface`.database

import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface R2dbcUserRepository: R2dbcRepository<R2dbcUserEntity, UUID>