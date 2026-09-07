package br.ufpr.sept.so2.modules.iam.application

interface IamSettings {
    val accessTtlSeconds: Long

    val refreshTtlSeconds: Long

    val resetTtlSeconds: Long

    val maxFalhasConsecutivas: Int

    val minutosBloqueio: Int

    val frontendBaseUrl: String
}
