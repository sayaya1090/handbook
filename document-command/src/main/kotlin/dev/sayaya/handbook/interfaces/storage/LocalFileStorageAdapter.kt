package dev.sayaya.handbook.interfaces.storage

import dev.sayaya.handbook.usecase.FileStorageService
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.*

/**
 * 로컬 파일시스템 기반 파일 저장소 어댑터.
 *
 * **책임:** 업로드된 파일을 `{baseDir}/{workspace}/{uuid}.{ext}` 경로에 저장하고,
 * 클라이언트가 접근 가능한 상대 URL을 반환한다.
 *
 * **의존관계:**
 * - 없음 (순수 인프라 어댑터, java.nio.file 사용)
 *
 * **주의:** 파일 I/O는 블로킹이므로 boundedElastic 스케줄러에서 실행한다.
 * 프로덕션 환경에서는 S3 등 외부 스토리지 어댑터로 교체할 수 있다.
 */
class LocalFileStorageAdapter(
    private val baseDir: Path = Path.of("./uploads"),
) : FileStorageService {

    override fun upload(workspace: UUID, filename: String, content: ByteArray): Mono<String> {
        return Mono.fromCallable {
            val ext = extractExtension(filename)
            val fileId = UUID.randomUUID()
            val dir = baseDir.resolve(workspace.toString())
            Files.createDirectories(dir)
            val targetFile = dir.resolve("$fileId.$ext")
            Files.write(targetFile, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
            "/workspace/$workspace/files/$fileId.$ext"
        }.subscribeOn(Schedulers.boundedElastic())
    }

    companion object {
        private fun extractExtension(filename: String): String {
            val dotIndex = filename.lastIndexOf('.')
            if (dotIndex < 0 || dotIndex == filename.length - 1) {
                throw IllegalArgumentException("File must have an extension: $filename")
            }
            return filename.substring(dotIndex + 1).lowercase()
        }
    }
}
