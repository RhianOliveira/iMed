package com.example.imed

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class AgendaDoutorActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Variáveis para guardar o estado do pop-up quando a tela for para o fundo
    private var dataDialogAtual: String = ""
    private var containerPacientesAtual: LinearLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agenda_doutor)

        findViewById<MaterialButton>(R.id.btnVoltarAgenda).setOnClickListener { finish() }

        findViewById<CalendarView>(R.id.calendarDoutor).setOnDateChangeListener { _, year, month, day ->
            val dataSelecionada = "$day/${month + 1}/$year"
            abrirDialogAgendaDia(dataSelecionada)
        }
    }

    private fun abrirDialogAgendaDia(data: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_agenda_dia, null)
        val container = dialogView.findViewById<LinearLayout>(R.id.containerPacientes)

        // Salvamos as referências do pop-up que acabou de ser aberto
        dataDialogAtual = data
        containerPacientesAtual = container

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setNegativeButton("Fechar", null)
            .show()

        // Quando o médico fechar o pop-up manualmente, nós limpamos as referências
        dialog.setOnDismissListener {
            dataDialogAtual = ""
            containerPacientesAtual = null
        }

        carregarConsultasDoDia(data, container)
    }

    // Assim como no Perfil, o onResume roda sempre que a tela volta a ficar visível.
    // Ele vai recarregar a lista do pop-up instantaneamente.
    override fun onResume() {
        super.onResume()
        if (dataDialogAtual.isNotEmpty() && containerPacientesAtual != null) {
            carregarConsultasDoDia(dataDialogAtual, containerPacientesAtual!!)
        }
    }

    private fun carregarConsultasDoDia(data: String, container: LinearLayout) {
        val uidMedico = auth.currentUser?.uid ?: return
        container.removeAllViews()

        db.collection("agendamentos")
            .whereEqualTo("idMedico", uidMedico)
            .whereEqualTo("data", data)
            .whereEqualTo("status", "pendente")
            .get()
            .addOnSuccessListener { docs ->
                if (docs.isEmpty) {
                    val tvVazio = TextView(this)
                    tvVazio.text = "Nenhuma consulta pendente."
                    tvVazio.setPadding(16, 16, 16, 16)
                    container.addView(tvVazio)
                    return@addOnSuccessListener
                }

                for (doc in docs.sortedBy { it.getString("horario") }) {
                    val view = LayoutInflater.from(this).inflate(R.layout.card_agenda_doutor, container, false)
                    val idPaciente = doc.getString("idPaciente") ?: ""
                    val agendamentoId = doc.id
                    val dataAgendamento = doc.getString("data") ?: ""
                    val horarioAgendamento = doc.getString("horario") ?: ""

                    db.collection("usuarios").document(idPaciente).get().addOnSuccessListener {
                        view.findViewById<TextView>(R.id.tvInfoPacienteDoutor).text = it.getString("nome") ?: "Paciente"
                        view.findViewById<TextView>(R.id.tvHorarioConsulta).text = horarioAgendamento
                    }

                    val btnCancelar = view.findViewById<MaterialButton>(R.id.btnCancelarAgenda)

                    // Validação de 24 horas antes de permitir o cancelamento
                    if (!podeCancelar(dataAgendamento, horarioAgendamento)) {
                        btnCancelar.isEnabled = false
                        btnCancelar.alpha = 0.5f // Deixa o botão visualmente "desabilitado"
                    }

                    btnCancelar.setOnClickListener {
                        atualizarStatus(agendamentoId, "cancelado", data, container)
                    }
                    view.findViewById<MaterialButton>(R.id.btnAusenteAgenda).setOnClickListener {
                        atualizarStatus(agendamentoId, "ausente", data, container)
                    }

                    view.findViewById<MaterialButton>(R.id.btnVerPerfilAgenda).setOnClickListener {
                        startActivity(Intent(this, PerfilPacienteActivity::class.java).apply {
                            putExtra("PACIENTE_ID", idPaciente)
                            putExtra("AGENDAMENTO_ID", agendamentoId)
                        })
                    }
                    container.addView(view)
                }
            }
    }

    private fun podeCancelar(dataStr: String, horarioStr: String): Boolean {
        return try {
            // Define o formato baseado em como você salva no Firestore (ex: "5/9/2026 14:00")
            val sdf = SimpleDateFormat("d/M/yyyy H:mm", Locale.getDefault())
            val dataConsulta = sdf.parse("$dataStr $horarioStr") ?: return false

            val agora = Calendar.getInstance().time
            val diffMilissegundos = dataConsulta.time - agora.time
            val diffHoras = diffMilissegundos / (1000 * 60 * 60)

            // Retorna true se faltarem 24 horas ou mais para a consulta
            diffHoras >= 24
        } catch (e: Exception) {
            // Em caso de erro na data, por segurança não permite cancelar
            false
        }
    }

    private fun atualizarStatus(id: String, status: String, data: String, container: LinearLayout) {
        db.collection("agendamentos").document(id).update("status", status).addOnSuccessListener {
            Toast.makeText(this, "Status alterado para $status", Toast.LENGTH_SHORT).show()
            carregarConsultasDoDia(data, container)
        }
    }
}