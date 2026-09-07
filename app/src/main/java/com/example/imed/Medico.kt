package com.example.imed

data class Medico(
    val id: String,
    val nome: String,
    val especialidade: String,
    val crm: String,
    val photoUrl: String? = null // Novo campo adicionado
)