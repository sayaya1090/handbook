package dev.sayaya.handbook

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * search-workspace 서비스 엔트리포인트.
 *
 * **역할**: 워크스페이스 도메인의 read-side. `/menus` 메뉴 공급과
 * `/workspaces` 조회 GET API 를 제공한다. DB 연결은 **PostgreSQL 읽기 전용
 * 트랜잭션** 으로 고정되어 (application.yml `options=-c default_transaction_read_only=on`)
 * 실수로 write 가 수행되지 않도록 안전장치가 걸려 있다.
 */
@SpringBootApplication
class SearchWorkspaceApplication

fun main(args: Array<String>) {
    runApplication<SearchWorkspaceApplication>(*args)
}
