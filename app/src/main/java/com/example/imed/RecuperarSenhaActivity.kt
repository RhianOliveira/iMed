package com.example.imed

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class RecuperarSenhaActivity : AppCompatActivity() {

    // Declarando os componentes do XML com base nos IDs que você mandou
    private lateinit var etEmailRecuperar: TextInputEditText
    private lateinit var btnEnviarRecuperacao: MaterialButton
    private lateinit var btnVoltarLogin: MaterialButton

    // Instância do Firebase Auth
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recuperar_senha) // Substitua pelo nome exato do seu xml se for diferente

        // Inicializando o Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Vinculando os componentes do Kotlin com os IDs do XML
        etEmailRecuperar = findViewById(R.id.etEmailRecuperar)
        btnEnviarRecuperacao = findViewById(R.id.btnEnviarRecuperacao)
        btnVoltarLogin = findViewById(R.id.btnVoltarLogin)

        // Ação do botão de enviar o e-mail de recuperação
        btnEnviarRecuperacao.setOnClickListener {
            val email = etEmailRecuperar.text.toString().trim()

            if (email.isNotEmpty()) {
                // Função nativa do Firebase para enviar o e-mail de redefinição
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                this,
                                "E-mail de recuperação enviado com sucesso! Verifique sua caixa de entrada.",
                                Toast.LENGTH_LONG
                            ).show()
                            finish() // Fecha a tela e volta para o login
                        } else {
                            Toast.makeText(
                                this,
                                "Erro ao enviar: ${task.exception?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            } else {
                etEmailRecuperar.error = "Por favor, digite seu e-mail"
            }
        }

        // Ação do botão de voltar (setinha no topo esquerdo)
        btnVoltarLogin.setOnClickListener {
            finish() // Apenas fecha a tela atual e retorna para a anterior (Login)
        }
    }
}