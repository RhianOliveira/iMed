package com.example.imed

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore

class PerfilPacienteActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var idPaciente: String = ""
    private var idAgendamento: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil_paciente)

        idPaciente = intent.getStringExtra("PACIENTE_ID") ?: ""
        idAgendamento = intent.getStringExtra("AGENDAMENTO_ID") ?: ""

        if (idPaciente.isEmpty()) {
            Toast.makeText(this, "Erro ao identificar paciente.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val tvNome = findViewById<TextView>(R.id.tvNome)
        val tvDataNasc = findViewById<TextView>(R.id.tvDataNasc)
        val tvSexo = findViewById<TextView>(R.id.tvSexo)
        val tvTipoSanguineo = findViewById<TextView>(R.id.tvTipoSanguineo)
        val tvAlergias = findViewById<TextView>(R.id.tvAlergias)
        val btnIniciarConsulta = findViewById<MaterialButton>(R.id.btnIniciarConsulta)

        // 1. Busca dados do Paciente
        db.collection("usuarios").document(idPaciente).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                tvNome.text = "Paciente: ${doc.getString("nome") ?: "Não informado"}"
                tvDataNasc.text = "Data de nascimento: ${doc.getString("dataNascimento") ?: "Não informada"}"
                tvSexo.text = "Sexo: ${doc.getString("genero") ?: "Não informado"}"
                tvTipoSanguineo.text = "Tipo sanguíneo: ${doc.getString("tipoSanguineo") ?: "Não informado"}"

                val alergias = doc.getString("alergias") ?: "Nenhuma"
                tvAlergias.text = "Alergias: $alergias"
                tvAlergias.setBackgroundColor(if (alergias.equals("Nenhuma", true) || alergias.isBlank()) Color.parseColor("#388E3C") else Color.parseColor("#B71C1C"))
            }
        }

        // 2. Ações dos botões
        btnIniciarConsulta.setOnClickListener {
            if (idAgendamento.isEmpty()) {
                Toast.makeText(this, "Esta tela não está vinculada a uma consulta agendada.", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, ProntuarioParte1Activity::class.java)
                intent.putExtra("PACIENTE_ID", idPaciente)
                intent.putExtra("AGENDAMENTO_ID", idAgendamento)
                startActivity(intent)
            }
        }

        findViewById<MaterialButton>(R.id.btnHistoricoConsultas).setOnClickListener {
            val intent = Intent(this, HistoricoConsultasPacienteActivity::class.java)
            intent.putExtra("PACIENTE_ID", idPaciente)
            startActivity(intent)
        }

        findViewById<MaterialButton>(R.id.btnVoltarPerfil).setOnClickListener { finish() }
    }

    // O onResume roda SEMPRE que a tela volta a ficar visível
    override fun onResume() {
        super.onResume()
        verificarStatusConsulta()
    }

    // Função que vai no banco confirmar se a consulta mudou para "concluido"
    private fun verificarStatusConsulta() {
        if (idAgendamento.isNotEmpty()) {
            val btnIniciarConsulta = findViewById<MaterialButton>(R.id.btnIniciarConsulta)
            db.collection("agendamentos").document(idAgendamento).get()
                .addOnSuccessListener { doc ->
                    val status = doc.getString("status")
                    if (status == "concluido") {
                        btnIniciarConsulta.text = "CONSULTA FINALIZADA"
                        btnIniciarConsulta.isEnabled = false // Desativa totalmente o clique
                        btnIniciarConsulta.setBackgroundColor(Color.GRAY) // Deixa cinza para ficar evidente
                    }
                }
        }
    }
}