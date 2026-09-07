package com.example.imed

import android.content.Intent
import android.os.Bundle
import android.util.Log // Adicionado para logs
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class InserirSenhaActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var emailDigitado: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inserir_senha)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        emailDigitado = intent.getStringExtra("EMAIL_DIGITADO")

        val etSenha = findViewById<TextInputEditText>(R.id.etSenha)
        val btnLogar = findViewById<MaterialButton>(R.id.btnLogar)
        val btnCancelar = findViewById<MaterialButton>(R.id.btnCancelar)
        val tvRecuperar = findViewById<android.widget.TextView>(R.id.tvRecuperarSenha)

        btnLogar.setOnClickListener {
            val senha = etSenha.text.toString().trim()

            if (emailDigitado != null && senha.isNotEmpty()) {
                fazerLogin(emailDigitado!!, senha)
            } else {
                Toast.makeText(this, "Preencha a senha", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancelar.setOnClickListener {
            finish()
        }

        tvRecuperar.setOnClickListener {
            val intent = Intent(this, RecuperarSenhaActivity::class.java)
            startActivity(intent)
        }
    }

    private fun fazerLogin(email: String, senha: String) {
        auth.signInWithEmailAndPassword(email, senha)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        verificarTipoAcesso(uid)
                    }
                } else {
                    Toast.makeText(this, "Erro no login: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun verificarTipoAcesso(uid: String) {
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role")

                    // Log para ver no Android Studio (Logcat)
                    Log.d("LOGIN_DEBUG", "UID buscado: $uid | Role encontrado: $role")

                    if (role == null) {
                        Toast.makeText(this, "Erro: Campo 'role' não existe no banco!", Toast.LENGTH_LONG).show()
                    } else {
                        when (role) {
                            "admin" -> startActivity(Intent(this, MenuAdminActivity::class.java))
                            "medico" -> startActivity(Intent(this, MenuDoutorActivity::class.java))
                            "paciente" -> startActivity(Intent(this, MenuPacienteActivity::class.java))
                            else -> {
                                Toast.makeText(this, "Cargo '$role' não reconhecido!", Toast.LENGTH_LONG).show()
                            }
                        }
                        finish()
                    }
                } else {
                    Toast.makeText(this, "Usuário não encontrado na coleção 'usuarios'!", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Falha na conexão: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}