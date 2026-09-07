package com.example.imed

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class ProntuarioParte1Activity : AppCompatActivity() {

    private var idPaciente: String = ""
    private var idAgendamento: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prontuario_parte1)

        // Captura os IDs passados pela tela anterior (Perfil ou Agenda)
        idPaciente = intent.getStringExtra("PACIENTE_ID") ?: ""
        idAgendamento = intent.getStringExtra("AGENDAMENTO_ID") ?: ""

        if (idPaciente.isEmpty() || idAgendamento.isEmpty()) {
            Toast.makeText(this, "Erro ao carregar dados da consulta.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val etQueixas = findViewById<TextInputEditText>(R.id.etQueixas)
        val etAlergias = findViewById<TextInputEditText>(R.id.etAlergias)
        val etMedicamentos = findViewById<TextInputEditText>(R.id.etMedicamentos)
        val etDoencas = findViewById<TextInputEditText>(R.id.etDoencas)

        val btnVoltar = findViewById<MaterialButton>(R.id.btnVoltar)
        val btnProximo = findViewById<MaterialButton>(R.id.btnProximo)

        btnVoltar.setOnClickListener {
            finish()
        }

        btnProximo.setOnClickListener {
            val queixas = etQueixas.text.toString().trim()
            val alergias = etAlergias.text.toString().trim()
            val medicamentos = etMedicamentos.text.toString().trim()
            val doencas = etDoencas.text.toString().trim()

            // Validação de segurança
            if (queixas.isEmpty() || alergias.isEmpty() || medicamentos.isEmpty() || doencas.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Cria a intenção para a Parte 2
            val intent = Intent(this, ProntuarioParte2Activity::class.java)

            // Passa todos os dados coletados
            intent.putExtra("PACIENTE_ID", idPaciente)
            intent.putExtra("AGENDAMENTO_ID", idAgendamento)
            intent.putExtra("QUEIXAS", queixas)
            intent.putExtra("ALERGIAS", alergias)
            intent.putExtra("MEDICAMENTOS", medicamentos)
            intent.putExtra("DOENCAS", doencas)

            startActivity(intent)

            // Fecha a Parte 1 para que, ao salvar na Parte 2, o médico retorne ao Perfil
            finish()
        }
    }
}