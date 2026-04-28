package dev.sayaya.handbook.usecase

import reactor.core.publisher.Mono
import java.util.*

/**
 * 파일 저장소 포트 (헥사고날 아키텍처 출력 포트).
 *
 * **책임:** 업로드된 파일을 저장하고, 접근 가능한 URL/ID를 반환한다.
 *
 * **의존관계:**
 * - [LocalFileStorageAdapter][dev.sayaya.handbook.interfaces.storage.LocalFileStorageAdapter] — 로컬 파일시스템 구현체
 *
 * **주의:** 반환값은 저장된 파일의 상대 경로이며, 클라이언트가 파일에 접근할 수 있는 URL로 사용된다.
 */
interface FileStorageService {
    /**
     * 파일을 저장하고 파일 접근 URL을 반환한다.
     *
     * @param workspace 워크스페이스 ID
     * @param filename 원본 파일명 (확장자 추출용)
     * @param content 파일 바이트 데이터
     * @return 저장된 파일의 접근 URL
     */
    fun upload(workspace: UUID, filename: String, content: ByteArray): Mono<String>
}
