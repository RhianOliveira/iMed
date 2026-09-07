package com.example.imed

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ProntuarioParte2Activity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prontuario_parte2)

        // Captura todos os dados passados pela Parte 1
        val idPaciente = intent.getStringExtra("PACIENTE_ID") ?: ""
        val idAgendamento = intent.getStringExtra("AGENDAMENTO_ID") ?: ""
        val queixas = intent.getStringExtra("QUEIXAS") ?: ""
        val alergias = intent.getStringExtra("ALERGIAS") ?: ""
        val medicamentos = intent.getStringExtra("MEDICAMENTOS") ?: ""
        val doencas = intent.getStringExtra("DOENCAS") ?: ""

        val etDiagnostico = findViewById<TextInputEditText>(R.id.etDiagnostico)
        val etPrescricao = findViewById<TextInputEditText>(R.id.etPrescricao)
        val btnVoltar = findViewById<MaterialButton>(R.id.btnVoltar)
        val btnSalvar = findViewById<MaterialButton>(R.id.btnSalvarProntuario)

        btnVoltar.setOnClickListener {
            finish()
        }

        btnSalvar.setOnClickListener {
            val diagnostico = etDiagnostico.text.toString().trim()
            val prescricao = etPrescricao.text.toString().trim()

            // Validação de campos vazios
            if (diagnostico.isEmpty() || prescricao.isEmpty()) {
                Toast.makeText(this, "Preencha o diagnóstico e a prescrição!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Mapeamento dos campos conforme sua tabela de dados
            val prontuario = hashMapOf(
                "paciente" to idPaciente,
                "medico" to (auth.currentUser?.uid ?: ""),
                "consulta" to idAgendamento,
                "anamnesis_complaints" to queixas,
                "anamnesis_allergies" to alergias,
                "anamnesis_medications" to medicamentos,
                "anamnesis_history" to doencas,
                "diagnosis" to diagnostico,
                "prescription" to prescricao,
                "record_date" to SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()),
                "is_finilazed" to true
            )

            // Salva no Firestore
            db.collection("prontuarios").add(prontuario)
                .addOnSuccessListener {
                    // Atualiza o status da consulta para evitar que ela apareça na agenda novamente
                    db.collection("agendamentos").document(idAgendamento)
                        .update("status", "concluido")
                        .addOnSuccessListener {
                            Toast.makeText(this, "Prontuário salvo com sucesso!", Toast.LENGTH_LONG).show()

                            // Fecha a atividade atual. Como a Parte 1 também será fechada,
                            // o médico retorna diretamente para a tela de Perfil.
                            finish()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Erro ao salvar prontuário.", Toast.LENGTH_SHORT).show()
                }
        }
    }
}