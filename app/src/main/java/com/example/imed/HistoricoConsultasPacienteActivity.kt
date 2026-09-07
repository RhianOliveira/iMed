package com.example.imed

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class HistoricoConsultasPacienteActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var idPaciente: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historico_consultas_paciente)

        idPaciente = intent.getStringExtra("PACIENTE_ID") ?: ""

        if (idPaciente.isEmpty()) {
            Toast.makeText(this, "Erro: Paciente não identificado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        findViewById<MaterialButton>(R.id.btnVoltar).setOnClickListener {
            finish()
        }

        carregarHistorico()
    }

    private fun carregarHistorico() {
        val container = findViewById<LinearLayout>(R.id.containerHistoricoPaciente)
        container.removeAllViews()

        db.collection("prontuarios")
            .whereEqualTo("paciente", idPaciente)
            .get()
            .addOnSuccessListener { documentos ->
                if (documentos.isEmpty) {
                    val tvVazio = TextView(this)
                    tvVazio.text = "Nenhum histórico de consulta encontrado."
                    tvVazio.setPadding(16, 16, 16, 16)
                    container.addView(tvVazio)
                    return@addOnSuccessListener
                }

                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val prontuariosOrdenados = documentos.sortedByDescending {
                    try {
                        dateFormat.parse(it.getString("record_date") ?: "")
                    } catch (e: Exception) {
                        Date(0)
                    }
                }

                for (doc in prontuariosOrdenados) {
                    val view = LayoutInflater.from(this).inflate(R.layout.item_historico_consulta, container, false)

                    val tvData = view.findViewById<TextView>(R.id.tvDataHistorico)
                    val tvMedico = view.findViewById<TextView>(R.id.tvMedicoHistorico)
                    val tvResumo = view.findViewById<TextView>(R.id.tvResumoHistorico)
                    val ivFotoMedico = view.findViewById<ImageView>(R.id.ivFotoMedicoHistorico) // Certifique-se de ter este ID no XML do item
                    val btnVisualizar = view.findViewById<MaterialButton>(R.id.btnVisualizarProntuario)

                    tvData.text = doc.getString("record_date") ?: "Data não informada"

                    val queixa = doc.getString("anamnesis_complaints") ?: ""
                    tvResumo.text = if (queixa.length > 40) "Resumo: ${queixa.substring(0, 40)}..." else "Resumo: $queixa"

                    val idMedico = doc.getString("medico") ?: ""
                    val prontuarioId = doc.id

                    if (idMedico.isNotEmpty()) {
                        db.collection("usuarios").document(idMedico).get().addOnSuccessListener { medicoDoc ->
                            tvMedico.text = "Dr(a). ${medicoDoc.getString("nome") ?: "Não identificado"}"

                            // Carregamento da foto do médico (Base64 ou URL)
                            val photoUrl = medicoDoc.getString("photoUrl")
                            if (!photoUrl.isNullOrEmpty() && ivFotoMedico != null) {
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
                            } else if (ivFotoMedico != null) {
                                ivFotoMedico.setImageResource(android.R.drawable.ic_menu_myplaces)
                            }
                        }
                    } else {
                        tvMedico.text = "Médico não identificado"
                        ivFotoMedico?.setImageResource(android.R.drawable.ic_menu_myplaces)
                    }

                    btnVisualizar.setOnClickListener {
                        val intent = Intent(this, VisualizarProntuarioActivity::class.java)
                        intent.putExtra("PRONTUARIO_ID", prontuarioId)
                        startActivity(intent)
                    }

                    container.addView(view)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar histórico.", Toast.LENGTH_SHORT).show()
            }
    }
}