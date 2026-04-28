package dev.sayaya.handbook.interfaces.config

import dev.sayaya.handbook.interfaces.storage.LocalFileStorageAdapter
import dev.sayaya.handbook.usecase.FileStorageService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.nio.file.Path

/**
 * 파일 업로드 관련 Spring Bean 설정.
 *
 * **책임:** FileStorageService 포트의 구현체(LocalFileStorageAdapter)와
 * 허용 확장자 목록을 Bean으로 등록한다.
 *
 * **의존관계:**
 * - [LocalFileStorageAdapter] — 로컬 파일 저장소 어댑터
 *
 * **주의:** `file.upload-dir` 프로퍼티로 저장 경로를 변경할 수 있다.
 * `file.allowed-extensions`로 허용 확장자를 제한한다 (기본: pdf,xlsx,docx,png,jpg,jpeg,csv).
 */
@Configuration
class FileConfig {

    @Bean
    fun fileStorageService(
        @Value("\${file.upload-dir:./uploads}") uploadDir: String,
    ): FileStorageService = LocalFileStorageAdapter(Path.of(uploadDir))

    @Bean
    fun allowedExtensions(
        @Value("\${file.allowed-extensions:pdf,xlsx,docx,png,jpg,jpeg,csv}") extensions: String,
    ): Set<String> = extensions.split(",").map { it.trim().lowercase() }.toSet()
}
