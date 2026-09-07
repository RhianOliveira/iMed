package com.example.imed

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PacienteConsultaActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paciente_consulta)

        carregarConsultas()
        findViewById<Button>(R.id.btnVoltar).setOnClickListener { finish() }
    }

    private fun carregarConsultas() {
        val container = findViewById<LinearLayout>(R.id.containerConsultas)
        container.removeAllViews()

        val uid = auth.currentUser?.uid ?: return

        db.collection("agendamentos")
            .whereEqualTo("idPaciente", uid)
            .whereEqualTo("status", "pendente")
            .get()
            .addOnSuccessListener { documentos ->
                for (doc in documentos) {
                    val view = LayoutInflater.from(this).inflate(R.layout.card_consulta, container, false)

                    val tvNomeMedico = view.findViewById<TextView>(R.id.tvNomeMedico)
                    val tvDataHora = view.findViewById<TextView>(R.id.tvDataHora)
                    val ivFotoMedico = view.findViewById<ImageView>(R.id.ivFotoMedico)
                    val btnCancelar = view.findViewById<Button>(R.id.btnCancelar)
                    val btnRemarcar = view.findViewById<Button>(R.id.btnRemarcar)

                    val idMedico = doc.getString("idMedico") ?: ""
                    val data = doc.getString("data") ?: ""
                    val horario = doc.getString("horario") ?: ""

                    tvDataHora.text = "Data: $data - $horario"

                    if (idMedico.isNotEmpty()) {
                        db.collection("usuarios").document(idMedico).get()
                            .addOnSuccessListener { userDoc ->
                                tvNomeMedico.text = userDoc.getString("nome") ?: "Médico Desconhecido"

                                val photoUrl = userDoc.getString("photoUrl")
                                if (!photoUrl.isNullOrEmpty()) {
                                    try {
                                        ivFotoMedico.imageTintList = null
                                        ivFotoMedico.clearColorFilter()

                                        if (photoUrl.startsWith("http")) {
                                            ivFotoMedico.load(photoUrl) {
                                                crossfade(true)
                                                transformations(CircleCropTransformation())
                                            }
                                        } else {
                                            val imageBytes = Base64.decode(photoUrl, Base64.DEFAULT)
                                            val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                            ivFotoMedico.load(decodedImage) {
                                                crossfade(true)
                                                transformations(CircleCropTransformation())
                                            }
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                } else {
                                    ivFotoMedico.setImageResource(android.R.drawable.ic_menu_agenda)
                                }
                            }
                    }

                    btnCancelar.setOnClickListener {
                        db.collection("agendamentos").document(doc.id)
                            .update("status", "cancelado")
                            .addOnSuccessListener {
                                Toast.makeText(this, "Consulta cancelada!", Toast.LENGTH_SHORT).show()
                                carregarConsultas()
                            }
                    }

                    btnRemarcar.setOnClickListener {
                        abrirDialogRemarcar(doc.id, idMedico)
                    }

                    container.addView(view)
                }
            }
    }

    private fun abrirDialogRemarcar(agendamentoId: String, idMedico: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_remarcar_consulta, null)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val etEspecialidade = dialogView.findViewById<TextInputEditText>(R.id.etEspecialidadeRemarcar)
        val etMedico = dialogView.findViewById<TextInputEditText>(R.id.etMedicoRemarcar)
        val calendarView = dialogView.findViewById<CalendarView>(R.id.calendarRemarcar)

        val amanha = java.util.Calendar.getInstance()
        amanha.add(java.util.Calendar.DAY_OF_YEAR, 1)
        calendarView.minDate = amanha.timeInMillis

        val spinner = dialogView.findViewById<Spinner>(R.id.spinnerHorariosRemarcar)
        val btnConfirmar = dialogView.findViewById<Button>(R.id.btnConfirmarRemarcacao)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelarRemarcacao)
        val btnFechar = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnFecharDialog)

        var novaData = ""

        btnFechar.setOnClickListener { dialog.dismiss() }
        btnCancelar.setOnClickListener { dialog.dismiss() }

        db.collection("usuarios").document(idMedico).get().addOnSuccessListener { doc ->
            etMedico.setText(doc.getString("nome") ?: "")
            etEspecialidade.setText(doc.getString("especialidade") ?: "")
        }

        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("Selecione uma data..."))

        calendarView.setOnDateChangeListener { _, year, month, day ->
            novaData = "$day/${month + 1}/$year"

            db.collection("agendamentos")
                .whereEqualTo("idMedico", idMedico)
                .whereEqualTo("data", novaData)
                .get()
                .addOnSuccessListener { docs ->
                    val horariosOcupados = docs.mapNotNull { doc ->
                        if (doc.id == agendamentoId) return@mapNotNull null

                        val status = doc.getString("status")?.lowercase()
                        if (status != "cancelado" && status != "cancelada") {
                            doc.getString("horario")
                        } else null
                    }

                    val todosHorarios = mutableListOf<String>()
                    for (h in 9 until 18) {
                        todosHorarios.add("$h:00")
                        todosHorarios.add("$h:30")
                    }

                    val horariosDisponiveis = todosHorarios.filter { it !in horariosOcupados }

                    if (horariosDisponiveis.isEmpty()) {
                        Toast.makeText(this, "Nenhum horário disponível.", Toast.LENGTH_SHORT).show()
                        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("Sem horários"))
                    } else {
                        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, horariosDisponiveis)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinner.adapter = adapter
                    }
                }
        }

        btnConfirmar.setOnClickListener {
            val selecionado = spinner.selectedItem?.toString()
            if (selecionado == null || selecionado == "Selecione uma data..." || selecionado == "Sem horários") {
                Toast.makeText(this, "Escolha uma data com horário válido!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val novoHorario = selecionado

            val dadosAtualizados = mapOf(
                "data" to novaData,
                "horario" to novoHorario
            )

            db.collection("agendamentos").document(agendamentoId)
                .update(dadosAtualizados)
                .addOnSuccessListener {
                    Toast.makeText(this, "Consulta reagendada!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    carregarConsultas()
                }
        }

        dialog.show()
    }
}