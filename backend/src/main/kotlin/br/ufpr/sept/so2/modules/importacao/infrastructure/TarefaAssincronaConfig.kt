package br.ufpr.sept.so2.modules.importacao.infrastructure

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@Configuration
@EnableAsync
class TarefaAssincronaConfig {
    @Bean(name = ["so2TaskExecutor"])
    fun so2TaskExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 2
        executor.maxPoolSize = 4
        executor.setThreadNamePrefix("so2-job-")
        executor.initialize()
        return executor
    }
}
