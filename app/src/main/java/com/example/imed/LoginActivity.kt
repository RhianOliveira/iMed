package com.example.imed // Substitua pelo seu pacote real

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login) // Nome do seu arquivo XML

        // Conecta os elementos do XML
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val btnProximo = findViewById<MaterialButton>(R.id.btnProximo)
        val tvCadastreSe = findViewById<TextView>(R.id.tvCadastreSe)

        // Lógica do botão Próximo
        btnProximo.setOnClickListener {
            val email = etEmail.text.toString().trim()

            if (email.isNotEmpty()) {
                // Aqui você deve criar uma nova Activity chamada LoginSenhaActivity
                val intent = Intent(this, InserirSenhaActivity::class.java)
                // Passamos o email para a próxima tela para o Firebase saber quem está logando
                intent.putExtra("EMAIL_DIGITADO", email)
                startActivity(intent)
            } else {
                etEmail.error = "Por favor, digite seu e-mail"
            }
        }

        // Lógica do link Cadastre-se
        tvCadastreSe.setOnClickListener {
            val intent = Intent(this, CadastroPacienteActivity::class.java) // Crie esta tela se não tiver
            startActivity(intent)
        }
    }
}