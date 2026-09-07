package com.example.imed

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MenuPacienteActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_paciente)

        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)
        val btnLogout = findViewById<MaterialButton>(R.id.btnLogoutPaciente)

        // Buscar nome do Paciente no Firestore
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

        // Inicializando os botões
        val btnMeusDados = findViewById<MaterialButton>(R.id.btnMeusDados)
        val btnNovoAgendamento = findViewById<MaterialButton>(R.id.btnNovoAgendamento)
        val btnVisualizarAgenda = findViewById<MaterialButton>(R.id.btnVisualizarAgenda)
        val btnHistorico = findViewById<MaterialButton>(R.id.btnHistorico)

        // Novos Cards de UI
        val cardMeusDados = findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardMeusDados)
        val cardNovoAgendamento = findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardNovoAgendamento)
        val cardVisualizarAgenda = findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardVisualizarAgenda)
        val cardHistorico = findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardHistorico)

        // Configurando a navegação
        val actionMeusDados = { startActivity(Intent(this, DadosPacienteActivity::class.java)) }
        val actionNovoAgendamento = { startActivity(Intent(this, PacienteAgendamentoActivity::class.java)) }
        val actionVisualizarAgenda = { startActivity(Intent(this, PacienteConsultaActivity::class.java)) }
        val actionHistorico = { startActivity(Intent(this, HistoricoConsultasActivity::class.java)) }

        btnMeusDados.setOnClickListener { actionMeusDados() }
        cardMeusDados.setOnClickListener { actionMeusDados() }

        btnNovoAgendamento.setOnClickListener { actionNovoAgendamento() }
        cardNovoAgendamento.setOnClickListener { actionNovoAgendamento() }

        btnVisualizarAgenda.setOnClickListener { actionVisualizarAgenda() }
        cardVisualizarAgenda.setOnClickListener { actionVisualizarAgenda() }

        btnHistorico.setOnClickListener { actionHistorico() }
        cardHistorico.setOnClickListener { actionHistorico() }

        // Configurando o Logout
        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}