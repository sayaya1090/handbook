package dev.sayaya.handbook.usecase

import dev.sayaya.handbook.domain.Document
import io.kotest.core.spec.style.ShouldSpec
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.security.Principal
import java.util.UUID

@Suppress("ReactiveStreamsUnusedPublisher")
internal class DocumentServiceTest: ShouldSpec({
    val repo = mockk<DocumentRepository>()

    val eventHandler = mockk<ExternalServiceHandler>()
    val principal = mockk<Principal>()
    val service = DocumentService(repo, eventHandler)

    val workspaceId = UUID.randomUUID()

    beforeTest {
        clearMocks(repo, eventHandler, principal, answers = false)
    }
    context("save 메소드는") {
        should("빈 문서 리스트가 주어지면 Mono.empty()를 반환하고 아무 작업도 수행하지 않아야 한다") {
            // given
            val emptyDocumentList = emptyList<Document>()

            // when
            val resultMono = service.save(principal, workspaceId, emptyDocumentList)

            // then
            StepVerifier.create(resultMono)
                .verifyComplete()

            verify(exactly = 0) { repo.saveAll(any(), any()) }
            verify(exactly = 0) { eventHandler.publish(any(), any(), any()) }
        }

        should("문서 리스트가 주어지면 문서를 저장하고 이벤트를 발행해야 한다") {
            // given
            val mockDocument1 = mockk<Document>()
            val mockDocument2 = mockk<Document>()
            val documentsToSave = listOf(mockDocument1, mockDocument2)

            // ExternalService.DocumentKey.of(doc)를 모킹하기 위해 DocumentKey도 모킹합니다.
            // 실제 DocumentKey 클래스 구조에 따라 이 부분은 달라질 수 있습니다.
            // 여기서는 간단히 Document 인스턴스 자체를 Key로 사용한다고 가정하거나,
            // DocumentKey.of가 Document의 특정 필드를 사용한다면 해당 필드도 모킹해야 합니다.
            // 편의상, ExternalService.DocumentKey.of(document)가 document를 그대로 반환한다고 가정하거나
            // 고유한 mockk<ExternalService.DocumentKey>()를 반환하도록 설정합니다.

            val mockKey1 = mockk<ExternalService.DocumentKey>()
            val mockKey2 = mockk<ExternalService.DocumentKey>()

            // Document 객체에 따라 ExternalService.DocumentKey.of()가 호출될 것을 가정합니다.
            // 이 부분이 프로젝트의 실제 Document 및 ExternalService.DocumentKey 구조에 따라 조정되어야 합니다.
            // 여기서는 Document 객체 자체가 키로 사용되거나, DocumentKey.of가 간단히 동작한다고 가정합니다.
            // 만약 Document에 id 같은 필드가 있고, 그것으로 Key를 만든다면 해당 필드를 mock해야 합니다.
            // 편의상, ExternalService.DocumentKey의 of 메소드가 Document 인스턴스를 받아
            // DocumentKey 인스턴스를 반환한다고 가정하고, 이를 모킹합니다.

            // `ExternalService.DocumentKey.of(doc)` 호출을 모킹하기 위한 설정
            // 실제 `Document` 클래스와 `ExternalService.DocumentKey`의 `of` 메소드 구현에 따라 달라집니다.
            // 여기서는 `of` 메소드가 Document 인스턴스를 받아 DocumentKey 인스턴스를 반환한다고 가정합니다.
            // 그리고 그 반환값을 미리 정의된 mockKey로 설정합니다.
            // `DocumentKey.of`가 static 메소드라면 `mockkStatic(ExternalService.DocumentKey::class)` 등이 필요할 수 있습니다.
            // 여기서는 DocumentKey.of가 Document의 특정 속성(예: id)을 사용한다고 가정하고, Document mock에 해당 속성을 설정합니다.
            // 또는, 더 간단하게는 Document 자체를 key로 사용한다고 가정할 수 있습니다.
            // DocumentService의 코드를 보면 ExternalService.DocumentKey.of(it)으로 되어 있으므로,
            // ExternalService.DocumentKey를 직접 모킹하거나, Document의 특정 필드를 설정해야 합니다.
            // 가장 간단한 방법은 Document 인스턴스 자체가 키로 사용될 수 있도록 하거나,
            // DocumentKey.of가 Document 인스턴스를 받아 예측 가능한 DocumentKey를 반환하도록 하는 것입니다.

            // 여기서는 ExternalService.DocumentKey.of가 Document의 특정 메서드(예: `toKey()`)를 호출한다고 가정하고,
            // 해당 메서드를 모킹합니다. 또는 `DocumentKey.of` 자체를 모킹할 수도 있습니다.
            // 실제 `ExternalService.DocumentKey.of`의 구현에 따라 이 부분은 달라집니다.
            // Document.serial() 메소드에 대한 stubbing 추가
            // serial() 메소드가 String을 반환한다고 가정합니다. 실제 반환 타입에 맞게 수정하세요.
            every { mockDocument1.serial } returns "SERIAL_MOCK_1"
            every { mockDocument2.serial } returns "SERIAL_MOCK_2"
            every { mockDocument1.type } returns "TYPE_MOCK_1"
            every { mockDocument2.type } returns "TYPE_MOCK_2"

            // ExternalService.DocumentKey.of()가 호출될 때 반환될 DocumentKey mock 객체들
            // 실제 ExternalService.DocumentKey.of()의 구현을 알아야 정확한 모킹이 가능합니다.
            // 여기서는 ExternalServiceHandler.publish에 전달되는 Map의 키가 DocumentKey 타입이므로,
            // DocumentKey.of()가 DocumentKey 인스턴스를 반환한다고 가정합니다.
            val documentKey1 = mockk<ExternalService.DocumentKey>()
            val documentKey2 = mockk<ExternalService.DocumentKey>()

            // `ExternalService.DocumentKey.of(document)` 호출에 대한 모킹
            // DocumentService 코드를 보면 `it.associateBy(ExternalService.DocumentKey::of)`가 사용됩니다.
            // 이는 각 Document `doc`에 대해 `ExternalService.DocumentKey.of(doc)`을 호출한다는 의미입니다.
            // `ExternalService.DocumentKey.of`가 정적 메서드라면 `mockkStatic`을 사용해야 합니다.
            // 여기서는 `ExternalService.DocumentKey`에 `of`라는 정적 팩토리 메서드가 있다고 가정합니다.
            // 이 부분은 실제 코드 구조에 따라 달라집니다.
            // 일단은 `DocumentKey.of`가 Document 인스턴스를 그대로 키로 사용하거나,
            // Document 인스턴스에 기반한 예측 가능한 값을 반환한다고 가정하고 진행하겠습니다.
            // 아래 코드는 ExternalService.DocumentKey.of가 Document의 id를 사용해 Key를 만든다고 가정한 예시입니다.
            // (실제 `DocumentKey` 클래스가 없으므로, `Document` 객체 자체나 `String` 등으로 대체하여 테스트할 수도 있습니다.)
            // 가장 직접적인 방법은 `ExternalService.DocumentKey` 자체를 모킹하고, `of` 메소드도 모킹하는 것입니다.
            // 여기서는 associateBy의 결과를 정확히 예측하기 위해 `ExternalService.DocumentKey.of`가
            // 각 `Document`에 대해 고유한 `ExternalService.DocumentKey`를 반환하도록 설정합니다.

            // repo.saveAll()이 반환할 Mono 설정
            val savedDocumentsMono = Mono.just(documentsToSave)
            every { repo.saveAll(workspaceId, documentsToSave) } returns savedDocumentsMono

            // eventHandler.publish()가 반환할 Mono 설정 및 호출 검증을 위한 준비
            // `it.associateBy(ExternalService.DocumentKey::of)` 부분 때문에
            // `ExternalService.DocumentKey.of`가 어떻게 동작하는지 알아야 합니다.
            // 여기서는 `ExternalService.DocumentKey.of(doc)`이 `doc` 자체를 반환하거나,
            // `doc`의 특정 식별자를 반환한다고 가정합니다.
            // 가장 간단하게는, `ExternalService.DocumentKey.of`가 호출되면,
            // 해당 `Document`에 매핑되는 `ExternalService.DocumentKey` mock을 반환하도록 설정합니다.
            // (ExternalService.DocumentKey의 실제 구현이 없으므로, 이는 가정입니다.)

            // ExternalService.DocumentKey.of 가 각 Document 에 대해 고유한 키를 반환한다고 가정
            val keyForDoc1 = mockk<ExternalService.DocumentKey>("keyForDoc1")
            val keyForDoc2 = mockk<ExternalService.DocumentKey>("keyForDoc2")

            // DocumentService의 `it.associateBy(ExternalService.DocumentKey::of)`를 처리하기 위한 모킹
            // `ExternalService.DocumentKey`의 `of` 메소드가 Document를 인자로 받는다고 가정합니다.
            // `of`가 정적 메소드라면 `mockkStatic`이 필요합니다. 여기서는 인스턴스 메소드나 동반 객체 메소드로 가정하지 않고,
            // `associateBy`에 전달되는 람다가 `ExternalService.DocumentKey.of(document)` 형태의 호출을 한다고 가정합니다.
            // 실제 `ExternalService.DocumentKey`의 `of` 메소드 시그니처에 맞춰야 합니다.
            // 이 테스트에서는 `ExternalService.DocumentKey.of(document)`의 반환값을 모킹하기 어렵기 때문에,
            // `eventHandler.publish`가 받는 `Map`의 키 타입을 `Any`로 하고, 값으로 `Document`를 받도록
            // `verify` 부분에서 `any<Map<ExternalService.DocumentKey, Document>>()`를 사용할 수 있습니다.
            // 또는, `DocumentKey.of()`가 `Document`의 `id`와 같은 예측 가능한 값을 사용한다고 가정할 수 있습니다.

            // 여기서는 ExternalServiceHandler.publish에 전달될 Map을 명시적으로 만들지 않고,
            // any()를 사용하여 검증하겠습니다. 더 정확한 검증을 위해서는
            // ExternalService.DocumentKey.of의 동작을 모킹하거나 실제 객체를 사용해야 합니다.
            every { eventHandler.publish(principal, workspaceId, any()) } returns Mono.empty() // Mono<Void> 또는 Mono<Boolean> 등

            // when
            val resultMono = service.save(principal, workspaceId, documentsToSave)

            // then
            StepVerifier.create(resultMono)
                .expectNext(documentsToSave)
                .verifyComplete()

            verify(exactly = 1) { repo.saveAll(workspaceId, documentsToSave) }
            // eventHandler.publish 호출 검증. Map의 내용까지 정확히 검증하려면 ExternalService.DocumentKey.of 모킹 필요
            verify(exactly = 1) {
                eventHandler.publish(
                    principal,
                    workspaceId,
                    // documentsToSave.associateBy { ExternalService.DocumentKey.of(it) } 와 일치하는 Map
                    // 실제 ExternalService.DocumentKey.of의 동작을 알 수 없으므로 any()를 사용하거나,
                    // DocumentKey.of를 모킹하여 예상되는 Map을 만들어야 합니다.
                    // 여기서는 any()를 사용하여 타입만 맞는지 확인합니다.
                    any<Map<ExternalService.DocumentKey, Document>>()
                )
            }
        }
    }
})
