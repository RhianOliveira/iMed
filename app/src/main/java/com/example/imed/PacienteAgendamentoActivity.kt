package com.example.imed

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PacienteAgendamentoActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var medicoSelecionadoId: String = ""
    private var dataSelecionada: String = ""
    private var horarioSelecionado: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paciente_agendamento)

        val autoEsp = findViewById<AutoCompleteTextView>(R.id.autoCompleteEspecialidade)
        val autoMed = findViewById<AutoCompleteTextView>(R.id.autoCompleteMedico)
        val calendar = findViewById<CalendarView>(R.id.calendarView)
        val spinnerHorarios = findViewById<Spinner>(R.id.spinnerHorarios)

        // Bloqueia datas passadas e o dia atual (permite apenas de amanhã em diante)
        val amanha = java.util.Calendar.getInstance()
        amanha.add(java.util.Calendar.DAY_OF_YEAR, 1)
        calendar.minDate = amanha.timeInMillis

        val btnConfirmar = findViewById<MaterialButton>(R.id.btnConfirmar)
        val btnVoltarTopo = findViewById<MaterialButton>(R.id.btnVoltarTopo)

        btnVoltarTopo.setOnClickListener { finish() }

        // 1. Carregar Especialidades
        db.collection("usuarios")
            .whereEqualTo("role", "medico")
            .get()
            .addOnSuccessListener { docs ->
                val especialidades = docs.map { it.getString("especialidade") ?: "" }.distinct()
                val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, especialidades)
                autoEsp.setAdapter(adapter)
            }

        // 2. Filtrar Médicos
        autoEsp.setOnItemClickListener { _, _, _, _ ->
            val esp = autoEsp.text.toString()
            autoMed.setText("")
            medicoSelecionadoId = "" // Reseta o médico ao trocar especialidade
            limparSpinner(spinnerHorarios)

            db.collection("usuarios")
                .whereEqualTo("especialidade", esp)
                .whereEqualTo("role", "medico")
                .whereEqualTo("status", "ativo")
                .get()
                .addOnSuccessListener { docs ->
                    val medicos = docs.map { it.getString("nome") ?: "" }
                    val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, medicos)
                    autoMed.setAdapter(adapter)

                    autoMed.setOnItemClickListener { _, _, position, _ ->
                        medicoSelecionadoId = docs.documents[position].id
                        // Se já houver uma data, recarrega os horários para o novo médico
                        if (dataSelecionada.isNotEmpty()) {
                            carregarHorariosDisponiveis(spinnerHorarios)
                        }
                    }
                }
        }

        // 3. Gerar Horários ao Clicar no Calendário
        calendar.setOnDateChangeListener { _, year, month, day ->
            dataSelecionada = "$day/${month + 1}/$year"

            if (medicoSelecionadoId.isEmpty()) {
                Toast.makeText(this, "Selecione um médico primeiro!", Toast.LENGTH_SHORT).show()
                return@setOnDateChangeListener
            }

            carregarHorariosDisponiveis(spinnerHorarios)
        }

        spinnerHorarios.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                horarioSelecionado = parent?.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                horarioSelecionado = ""
            }
        }

        // 4. Salvar Agendamento
        btnConfirmar.setOnClickListener {
            if (medicoSelecionadoId.isEmpty() || dataSelecionada.isEmpty() || horarioSelecionado.isEmpty() || horarioSelecionado == "Sem horários") {
                Toast.makeText(this, "Selecione médico, data e horário disponível!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val agendamento = hashMapOf(
                "idPaciente" to auth.currentUser?.uid,
                "idMedico" to medicoSelecionadoId,
                "data" to dataSelecionada,
                "horario" to horarioSelecionado,
                "status" to "pendente"
            )

            db.collection("agendamentos").add(agendamento)
                .addOnSuccessListener {
                    Toast.makeText(this, "Agendamento confirmado!", Toast.LENGTH_SHORT).show()
                    finish()
                }
        }
    }

    private fun limparSpinner(spinner: Spinner) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("Selecione um horário"))
        spinner.adapter = adapter
        horarioSelecionado = ""
    }

    // Função que checa o banco e filtra os horários
    private fun carregarHorariosDisponiveis(spinner: Spinner) {
        db.collection("agendamentos")
            .whereEqualTo("idMedico", medicoSelecionadoId)
            .whereEqualTo("data", dataSelecionada)
            .get()
            .addOnSuccessListener { docs ->
                // Lista de horários já marcados (ignorando os cancelados)
                val horariosOcupados = docs.mapNotNull {
                    val status = it.getString("status")?.lowercase()
                    if (status != "cancelado" && status != "cancelada") {
                        it.getString("horario")
                    } else null
                }

                val todosHorarios = mutableListOf<String>()
                for (h in 9 until 18) {
                    todosHorarios.add("$h:00")
                    todosHorarios.add("$h:30")
                }

                // Filtra: só mantém o que NÃO está na lista de ocupados
                val horariosDisponiveis = todosHorarios.filter { it !in horariosOcupados }

                if (horariosDisponiveis.isEmpty()) {
                    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("Sem horários"))
                    spinner.adapter = adapter
                    horarioSelecionado = ""
                    Toast.makeText(this, "Sem horários disponíveis nesta data.", Toast.LENGTH_SHORT).show()
                } else {
                    val listaComPrompt = mutableListOf("Selecione um horário")
                    listaComPrompt.addAll(horariosDisponiveis)
                    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listaComPrompt)
                    spinner.adapter = adapter
                }
            }
    }
}