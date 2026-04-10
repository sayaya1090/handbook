package dev.sayaya.handbook.interfaces.authentication

import dev.sayaya.handbook.domain.Pem
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * JWT 인증 관련 빈 설정.
 *
 * **책임:** PEM 키 파서, JWT 인증 컨버터/매니저, Claims 컨버터 빈을 등록한다.
 *
 * **의존관계:**
 * - [AuthenticationConfig] — JWT 시크릿 및 쿠키 헤더 설정
 * - [Pem] — PEM 형식 공개키/비밀키 파싱
 * - [JwtAuthenticationConverter] — HTTP 요청에서 JWT 추출
 * - [JwtAuthenticationManager] — JWT 유효성 검증 및 인증 객체 생성
 *
 * **주의:** [AuthenticationAutoConfig]에서 @Import로 포함된다.
 */
@Configuration
class JwtAuthenticationConfig {
    /**
     * PEM 키 파서 빈을 생성한다.
     *
     * @param config 인증 설정 (jwtSecret 포함)
     * @return PEM 형식 키 파서
     */
    @Bean fun pem(config: AuthenticationConfig) = Pem(config.jwtSecret)

    /**
     * Claims 인증 컨버터 빈을 생성한다.
     *
     * @return 기본 사용자 인증 컨버터
     */
    @Bean fun claimsAuthenticationConverter(): ClaimsAuthenticationConverter = UserAuthenticationConverter()

    /**
     * JWT 인증 컨버터 빈을 생성한다.
     *
     * @param config 인증 설정
     * @return JWT 인증 컨버터
     */
    @Bean fun jwtAuthenticationConverter(config: AuthenticationConfig) = JwtAuthenticationConverter(config)

    /**
     * JWT 인증 매니저 빈을 생성한다.
     *
     * @param pem PEM 키 파서
     * @param converter Claims 인증 컨버터
     * @return JWT 인증 매니저
     */
    @Bean fun jwtAuthenticationManager(pem: Pem, converter: ClaimsAuthenticationConverter) = JwtAuthenticationManager(pem, converter)
}
