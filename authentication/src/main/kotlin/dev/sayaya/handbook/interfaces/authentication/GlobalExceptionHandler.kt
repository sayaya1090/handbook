package dev.sayaya.handbook.interfaces.authentication

import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import reactor.core.publisher.Mono

/**
 * 전역 예외 처리 핸들러
 *
 * 모든 컨트롤러에서 발생하는 공통 예외를 일관된 형식(RFC 7807 Problem Detail)으로 처리합니다.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgument(ex: IllegalArgumentException): Mono<ProblemDetail> {
        val problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.message ?: "잘못된 요청입니다.")
        problem.title = "Bad Request"
        return Mono.just(problem)
    }

    @ExceptionHandler(DuplicateKeyException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleDuplicateKey(ex: DuplicateKeyException): Mono<ProblemDetail> {
        val problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.message ?: "중복된 데이터가 존재합니다.")
        problem.title = "Conflict"
        return Mono.just(problem)
    }

    @ExceptionHandler(NoSuchElementException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFound(ex: NoSuchElementException): Mono<ProblemDetail> {
        val problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.message ?: "리소스를 찾을 수 없습니다.")
        problem.title = "Not Found"
        return Mono.just(problem)
    }

    @ExceptionHandler(UnsupportedOperationException::class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    fun handleUnsupportedOperation(ex: UnsupportedOperationException): Mono<ProblemDetail> {
        val problem = ProblemDetail.forStatusAndDetail(HttpStatus.METHOD_NOT_ALLOWED, ex.message ?: "지원하지 않는 작업입니다.")
        problem.title = "Method Not Allowed"
        return Mono.just(problem)
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleGeneral(ex: Exception): Mono<ProblemDetail> {
        val problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.")
        problem.title = "Internal Server Error"
        return Mono.just(problem)
    }
}
