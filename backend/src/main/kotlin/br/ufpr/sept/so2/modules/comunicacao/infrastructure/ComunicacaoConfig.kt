package br.ufpr.sept.so2.modules.comunicacao.infrastructure

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
@EnableConfigurationProperties(ComunicacaoProperties::class)
class ComunicacaoConfig
