package dev.sayaya.handbook.domain

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.Instant
import java.util.*

class UserTest : DescribeSpec({
    describe("User") {
        it("toToken으로 JWT 클레임 객체를 생성할 수 있다") {
            val id = UUID.randomUUID()
            val user = User(id, "google", "12345", "Sangjay", mutableListOf(SystemRole.USER))
            val nbf = Instant.now()
            val exp = nbf.plusSeconds(3600)
            val iat = nbf
            val jti = UUID.randomUUID()
            val iss = "handbook"

            val token = user.toToken(nbf, exp, iss, iat, jti)

            token.sub shouldBe id.toString()
            token.id shouldBe jti.toString()
            token.name shouldBe "Sangjay"
            token.authorities shouldBe listOf("USER")
            token.iss shouldBe iss
            token.nbf shouldBe nbf
            token.exp shouldBe exp
            token.iat shouldBe iat
        }
    }
})
