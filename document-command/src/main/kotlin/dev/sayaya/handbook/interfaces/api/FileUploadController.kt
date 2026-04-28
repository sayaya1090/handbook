package dev.sayaya.handbook.interfaces.api

import dev.sayaya.handbook.usecase.FileStorageService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import java.util.*

/**
 * 파일 업로드 REST 컨트롤러.
 *
 * **책임:** 워크스페이스 단위로 파일 업로드를 처리한다.
 * multipart/form-data 요청에서 파일을 받아 확장자를 검증한 뒤 저장소에 저장하고 URL을 반환한다.
 *
 * **의존관계:**
 * - [FileStorageService] — 파일 저장 포트
 *
 * **주의:** 허용 확장자 목록(allowedExtensions)은 FileConfig에서 주입받는다.
 * 허용되지 않은 확장자의 파일은 400 Bad Request로 거부한다.
 * 파일 크기가 maxFileSize를 초과하면 413 Payload Too Large로 거부한다.
 */
@RestController
@RequestMapping("/workspaces/{workspace}/files")
class FileUploadController(
    private val fileStorageService: FileStorageService,
    private val allowedExtensions: Set<String>,
    @Value("\${file.max-size:52428800}") private val maxFileSize: Long,
) {

    @PostMapping(
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @ResponseStatus(HttpStatus.CREATED)
    fun upload(
        @PathVariable workspace: UUID,
        @RequestPart("file") filePart: FilePart,
    ): Mono<FileUploadResponse> {
        val filename = filePart.filename()
        val ext = extractExtension(filename)
        if (ext !in allowedExtensions) {
            return Mono.error(
                ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File extension '$ext' is not allowed. Allowed: $allowedExtensions"
                )
            )
        }
        return filePart.content()
            .reduce { buf1, buf2 ->
                buf1.write(buf2)
                buf1
            }
            .flatMap { dataBuffer ->
                val bytes = ByteArray(dataBuffer.readableByteCount())
                dataBuffer.read(bytes)
                if (bytes.size > maxFileSize) {
                    Mono.error(ResponseStatusException(
                        HttpStatus.PAYLOAD_TOO_LARGE,
                        "File size ${bytes.size} bytes exceeds maximum allowed size of $maxFileSize bytes"
                    ))
                } else {
                    fileStorageService.upload(workspace, filename, bytes)
                }
            }
            .map { url -> FileUploadResponse(url) }
    }

    /**
     * 파일 업로드 응답 DTO.
     *
     * @property url 저장된 파일의 접근 URL
     */
    data class FileUploadResponse(val url: String)

    companion object {
        private fun extractExtension(filename: String): String {
            val dotIndex = filename.lastIndexOf('.')
            if (dotIndex < 0 || dotIndex == filename.length - 1) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "File must have an extension: $filename")
            }
            return filename.substring(dotIndex + 1).lowercase()
        }
    }
}
