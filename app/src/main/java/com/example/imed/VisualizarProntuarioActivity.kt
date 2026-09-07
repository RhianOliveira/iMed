package com.example.imed

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore

class VisualizarProntuarioActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visualizar_prontuario)

        // Recebe o ID. Pode ser o ID do Prontuário ou o ID da Consulta (Agendamento)
        val idRecebido = intent.getStringExtra("PRONTUARIO_ID") ?: intent.getStringExtra("idConsulta") ?: ""

        if (idRecebido.isEmpty()) {
            Toast.makeText(this, "Erro: Identificador não encontrado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        findViewById<MaterialButton>(R.id.btnVoltarVis).setOnClickListener {
            finish()
        }

        // Tenta buscar primeiro como ID direto do documento de prontuário
        db.collection("prontuarios").document(idRecebido).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    exibirDados(doc)
                } else {
                    // Se não encontrou por ID direto, tenta buscar pelo campo "consulta" (link com agendamento)
                    buscarPorIdConsulta(idRecebido)
                }
            }
            .addOnFailureListener {
                buscarPorIdConsulta(idRecebido)
            }
    }

    private fun buscarPorIdConsulta(idConsulta: String) {
        db.collection("prontuarios")
            .whereEqualTo("consulta", idConsulta)
            .get()
            .addOnSuccessListener { snapshots ->
                if (!snapshots.isEmpty) {
                    exibirDados(snapshots.documents[0])
                } else {
                    Toast.makeText(this, "Prontuário ainda não registrado para esta consulta.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar prontuário.", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun exibirDados(doc: com.google.firebase.firestore.DocumentSnapshot) {
        val tvVisQueixas = findViewById<TextView>(R.id.tvVisQueixas)
        val tvVisAlergias = findViewById<TextView>(R.id.tvVisAlergias)
        val tvVisMedicamentos = findViewById<TextView>(R.id.tvVisMedicamentos)
        val tvVisDoencas = findViewById<TextView>(R.id.tvVisDoencas)
        val tvVisDiagnostico = findViewById<TextView>(R.id.tvVisDiagnostico)
        val tvVisPrescricao = findViewById<TextView>(R.id.tvVisPrescricao)

        tvVisQueixas.text = doc.getString("anamnesis_complaints") ?: "Nenhuma queixa registrada"
        tvVisAlergias.text = doc.getString("anamnesis_allergies") ?: "Nenhuma alergia registrada"
        tvVisMedicamentos.text = doc.getString("anamnesis_medications") ?: "Nenhum medicamento registrado"
        tvVisDoencas.text = doc.getString("anamnesis_history") ?: "Nenhum histórico registrado"
        tvVisDiagnostico.text = doc.getString("diagnosis") ?: "Sem diagnóstico"
        tvVisPrescricao.text = doc.getString("prescription") ?: "Sem prescrição"
    }
}