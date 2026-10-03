package br.ufpr.sept.so2.modules.iam.application

data class CampoPatch<T>(val presente: Boolean, val valor: T?) {
    companion object {
        fun <T> ausente(): CampoPatch<T> = CampoPatch(false, null)

        fun <T> de(valor: T?): CampoPatch<T> = CampoPatch(true, valor)
    }
}

data class PerfilPatch(
    val nomeSocial: CampoPatch<String?> = CampoPatch.ausente(),
    val telefone: CampoPatch<String?> = CampoPatch.ausente(),
    val emailPessoal: CampoPatch<String?> = CampoPatch.ausente(),
    val identidadeGenero: CampoPatch<String?> = CampoPatch.ausente(),
)
