package br.ufpr.sept.so2.modules.coordenacao.api.dto

data class ConfigCursoRequest(
    val horasFormativasMinimas: Int? = null,
    val duracaoCalendario: Int? = null,
    val bancaMembrosExternos: Int? = null,
    val bancaModalidade: String? = null,
    val regimento: String? = null,
)
