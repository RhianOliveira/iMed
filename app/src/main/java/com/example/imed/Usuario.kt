package com.example.imed

data class Usuario(
    val uid: String = "",
    val email: String = "",
    val tipoAcesso: String = "paciente", // Valores esperados: "paciente", "doutor", "admin"
    val status: String = "ativo"        // Útil para doutores (ativo/inativo)
)