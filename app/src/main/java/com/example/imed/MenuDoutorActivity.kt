package com.example.imed

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MenuDoutorActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_doutor)

        val tvGreeting = findViewById<TextView>(R.id.tvGreetingDoctor)
        val btnLogout = findViewById<MaterialButton>(R.id.btnLogoutDoutor)
        
        // Cards
        val cardAgendamentos = findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardAgendamentos)
        val cardHistorico = findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardHistoricoMedico)

        // Buscar nome do Doutor no Firestore
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("usuarios").document(uid).get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val nome = document.getString("nome")
                    if (!nome.isNullOrEmpty()) {
                        tvGreeting.text = "Olá, $nome"
                    }
                }
            }
        }

        cardAgendamentos.setOnClickListener {
            startActivity(Intent(this, AgendaDoutorActivity::class.java))
        }

        cardHistorico.setOnClickListener {
            startActivity(Intent(this, HistoricoConsultasMedicoActivity::class.java))
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}