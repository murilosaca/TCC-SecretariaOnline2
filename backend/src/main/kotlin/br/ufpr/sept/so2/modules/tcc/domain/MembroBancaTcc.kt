package br.ufpr.sept.so2.modules.tcc.domain

import java.util.UUID

class MembroBancaTcc(
    val id: UUID,
    val idUsuario: UUID,
    val papel: PapelBancaTcc,
)
