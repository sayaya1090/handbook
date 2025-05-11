package dev.sayaya.handbook.client.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import java.util.Date

class TypeTest : StringSpec({
    "유효한 Type 객체 생성" {
        val attributes = listOf(Attribute.builder().id("test").name("test").build()) // 유효한 Attribute 리스트
        val effectDate = Date(1672531200000L) // 2023-01-01 00:00:00 GMT
        val expireDate = Date(1672534800000L) // 2023-01-01 01:00:00 GMT

        val type = Type.builder()
            .id("test-id")
            .version("1.0")
            .effectDateTime(effectDate)
            .expireDateTime(expireDate)
            .description("A valid Type object")
            .primitive(true)
            .attributes(attributes) // Lombok @Singular 사용 시 attribute(Attribute()) 로도 가능
            .parent("parent-id")
            .x(0).y(0).width(100).height(100)
            .build()

        type.id() shouldBe "test-id"
        type.version() shouldBe "1.0"
        type.effectDateTime() shouldBe effectDate
        type.expireDateTime() shouldBe expireDate
        type.description() shouldBe "A valid Type object"
        type.primitive() shouldBe true
        type.parent() shouldBe "parent-id"
        type.attributes().shouldNotBeEmpty()
        type.x() shouldBe 0
        type.y() shouldBe 0
        type.width() shouldBe 100
        type.height() shouldBe 100
    }

    "id가 null일 경우 빌드 시 예외를 던진다" {
        val exception = shouldThrow<IllegalArgumentException> {
            Type.builder()
                // .id(null)
                .version("1.0")
                .effectDateTime(Date(1672531200000L))
                .expireDateTime(Date(1672534800000L))
                .description("Type with null id")
                .primitive(false)
                .attribute(Attribute.builder().id("test").name("test").build()) // @Singular 사용 예시
                .parent(null)
                .x(0).y(0).width(100).height(100)
                .build() // id() 메서드에서 예외 발생
        }
        // Type.java의 id() 메서드 내 validateNonNullOrEmpty 확인
        exception.shouldHaveMessage("id must not be null")
    }

    "version이 null일 경우 빌드 시 예외를 던진다" {
        val exception = shouldThrow<IllegalArgumentException> {
            Type.builder()
                .id("test-id")
                // .version(null)
                .effectDateTime(Date(1672531200000L))
                .expireDateTime(Date(1672534800000L))
                .description("Type with null version")
                .primitive(false)
                .attributes(listOf(Attribute.builder().id("test").name("test").build()))
                .parent(null)
                .x(0).y(0).width(100).height(100)
                .build() // version() 메서드에서 예외 발생
        }
        // Type.java의 version() 메서드 내 validateNonNullOrEmpty 확인
        exception.shouldHaveMessage("version must not be null")
    }

    "attributes가 null일 경우 빌드 시 예외를 던진다" {
         // Lombok의 @Builder 와 @Singular 는 기본적으로 null 리스트를 허용하지 않고 빈 리스트로 처리하거나,
         // 명시적으로 attributes(null)을 호출하면 NullPointerException을 발생시킬 수 있습니다.
         // Type 클래스에서 명시적으로 attributes() 메서드를 오버라이드하여 null 검사를 추가했으므로,
         // 해당 검사를 테스트합니다.
        val exception = shouldThrow<IllegalArgumentException> {
             // Type 생성자 또는 attributes() 메서드에서 직접 null 검사를 하는 경우 테스트
             val builder = Type.builder()
                .id("test-id")
                .version("1.0")
                .effectDateTime(Date(1672531200000L))
                .expireDateTime(Date(1672534800000L))
                .description("Type with null attributes")
                .primitive(false)
                // .attributes(null) // 빌더 패턴에서 직접 null 설정 시도
                .parent(null)
                .x(0).y(0).width(100).height(100)

             // Type 클래스 내부의 attributes(List<Attribute>) 메서드가 호출될 때 null 검증
             // Lombok이 생성한 빌더의 build() 메서드는 내부적으로 setter를 호출합니다.
             // 명시적으로 재정의된 attributes() 메서드가 없다면 Lombok 기본 동작을 따릅니다.
             // 여기서는 Type.java 에 attributes(List<Attribute>) 메서드가 있으므로 해당 메서드가 호출됩니다.
             val type = builder.build() // 이 시점에서 attributes(null)이 호출될 수 있다면 예외 발생
             type.attributes(null) // 또는 생성된 객체에 직접 null 할당 시도 시 예외 발생 확인
        }
        // Type.java의 attributes() 메서드 내 validateNonNull 확인
        exception.shouldHaveMessage("attributes must not be null")

        // Lombok @Singular 사용 시 null 컬렉션 제공 테스트 (보통 NPE 발생 또는 빈 리스트로 처리됨)
        // 참고: 이 테스트는 Type.java의 attributes(List<>) 메서드 구현에 따라 달라질 수 있음
        // 만약 attributes() 메서드가 없다면 Lombok의 기본 동작 테스트가 됨
         val exceptionSingular = shouldThrow<NullPointerException> {
             Type.builder()
                 .id("test-id")
                 .version("1.0")
                 .effectDateTime(Date(1672531200000L))
                 .expireDateTime(Date(1672534800000L))
                 .description("Type with null attributes via singular adder")
                 .primitive(false)
                 .attributes(null) // @Singular가 있어도 전체 리스트를 null로 설정 시도
                 .parent(null)
                 .x(0).y(0).width(100).height(100)
                 .build()
         }
         // Lombok @Singular의 기본 동작은 null 컬렉션 제공 시 NPE를 던질 수 있습니다.
         // 메시지는 구현에 따라 다를 수 있습니다.
         // exceptionSingular.message shouldContain "Cannot invoke" // 예시 메시지 부분
    }


    "종료 시간이 시작 시간보다 빠를 경우 빌드 시 예외를 던진다" {
        val startDate = Date(1672534800000L) // 2023-01-01 01:00:00 GMT
        val endDate = Date(1672531200000L)   // 2023-01-01 00:00:00 GMT (잘못된 시간)

        val exception = shouldThrow<IllegalArgumentException> {
            Type.builder()
                .id("test-id")
                .version("1.0")
                .effectDateTime(startDate)
                .expireDateTime(endDate) // 잘못된 시간 설정
                .description("Invalid date range")
                .primitive(false)
                .attributes(listOf(Attribute.builder().build()))
                .parent(null)
                .x(0).y(0).width(100).height(100)
                .build() // 생성자에서 날짜 비교 시 예외 발생
        }

        exception.shouldHaveMessage("Expire date time must be after effect date time")
    }

    "Type의 부모-자식 관계 설정" {
        val parentAttributes = listOf(Attribute.builder().id("parent-attr").build())
        val childAttributes = listOf(Attribute.builder().id("child-attr").build())
        val parentEffectDate = Date(1672531200000L)
        val parentExpireDate = Date(1672534800000L)
        val childEffectDate = Date(1672534800000L) // 부모 만료 시간과 동일하게 시작 가능
        val childExpireDate = Date(1672538400000L) // 부모 만료 시간 이후

        val parentType = Type.builder()
            .id("parent-id")
            .version("1.0")
            .effectDateTime(parentEffectDate)
            .expireDateTime(parentExpireDate)
            .description("Parent Type")
            .primitive(true)
            .attributes(parentAttributes)
            .parent(null) // 부모는 parent가 없음
            .x(0).y(0).width(100).height(100)
            .build()

        val childType = Type.builder()
            .id("child-id")
            .version("1.0")
            .effectDateTime(childEffectDate)
            .expireDateTime(childExpireDate)
            .description("Child Type")
            .primitive(false)
            .attributes(childAttributes)
            .parent(parentType.id()) // 부모 id 사용
            .x(10).y(10).width(50).height(50) // 다른 위치/크기 가질 수 있음
            .build()

        parentType.id() shouldBe "parent-id"
        childType.parent() shouldBe parentType.id()
        childType.parent() shouldBe "parent-id" // 명시적으로 값 확인
    }

    // 추가: 위치 및 크기 관련 유효성 검사 테스트
    "x 좌표가 음수일 경우 예외를 던진다" {
        val exception = shouldThrow<IllegalArgumentException> {
            Type.builder()
                .id("test-id").version("1.0")
                .effectDateTime(Date()).expireDateTime(Date(System.currentTimeMillis() + 10000))
                .description("Negative X")
                .attributes(listOf(Attribute.builder().build()))
                .width(100).height(100).y(0)
                .x(-1) // 음수 X 좌표
                .build()
        }
        exception.shouldHaveMessage("X must be greater than or equal 0")
    }

     "width가 0 이하일 경우 예외를 던진다" {
        val exception = shouldThrow<IllegalArgumentException> {
            Type.builder()
                .id("test-id").version("1.0")
                .effectDateTime(Date()).expireDateTime(Date(System.currentTimeMillis() + 10000))
                .description("Zero width")
                .attributes(listOf(Attribute.builder().build()))
                .x(0).y(0).height(100)
                .width(0) // 0 너비
                .build()
        }
        exception.shouldHaveMessage("Width must be greater than 0")
    }
})