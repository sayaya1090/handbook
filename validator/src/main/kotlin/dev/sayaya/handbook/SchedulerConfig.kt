package dev.sayaya.handbook

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Configuration
class SchedulerConfig {
    @Bean(name = [VIRTUAL_THREAD_SCHEDULER_BEAN_NAME])
    fun virtualThreadScheduler(): Scheduler {
        val virtualThreadExecutorService: ExecutorService = Executors.newVirtualThreadPerTaskExecutor()
        return Schedulers.fromExecutorService(virtualThreadExecutorService, "reactor-virtual-threads") // 스케줄러 이름 지정
    }
    companion object {
        const val VIRTUAL_THREAD_SCHEDULER_BEAN_NAME: String = "virtualThreadScheduler"
    }
}

