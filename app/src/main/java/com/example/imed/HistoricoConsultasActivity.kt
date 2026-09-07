package com.example.imed

import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import coil.load
import coil.transform.CircleCropTransformation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HistoricoConsultasActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historico_consultas)

        carregarHistorico()

        findViewById<Button>(R.id.btnVoltar).setOnClickListener { finish() }
    }

    private fun carregarHistorico() {
        val container = findViewById<LinearLayout>(R.id.containerHistorico)
        container.removeAllViews()

        val uid = auth.currentUser?.uid ?: return

        db.collection("agendamentos")
            .whereEqualTo("idPaciente", uid)
            .get()
            .addOnSuccessListener { documentos ->
                for (doc in documentos) {
                    val view = LayoutInflater.from(this).inflate(R.layout.card_historico, container, false)

                    val tvNomeMedico = view.findViewById<TextView>(R.id.tvNomeMedicoHist)
                    val tvDataHora = view.findViewById<TextView>(R.id.tvDataHoraHist)
                    val tvStatus = view.findViewById<TextView>(R.id.tvStatusHist)
                    val ivFotoMedico = view.findViewById<ImageView>(R.id.ivFotoMedicoHist)

                    val idMedico = doc.getString("idMedico") ?: ""
                    val data = doc.getString("data") ?: ""
                    val horario = doc.getString("horario") ?: ""
                    val status = doc.getString("status")?.uppercase() ?: "DESCONHECIDO"

                    tvDataHora.text = "Data: $data - $horario"
                    tvStatus.text = "STATUS: $status"

                    // Define as cores baseadas no status
                    when (status) {
                        "PENDENTE", "AGENDADA" -> tvStatus.setTextColor(Color.parseColor("#2E7D32"))
                        "CANCELADO", "CANCELADA" -> tvStatus.setTextColor(Color.parseColor("#B71C1C"))
                        "CONCLUIDO", "REALIZADA" -> tvStatus.setTextColor(Color.parseColor("#06152D"))
                        "AUSENTE" -> tvStatus.setTextColor(Color.parseColor("#F57C00"))
                        else -> tvStatus.setTextColor(Color.GRAY)
                    }

                    // Busca o nome e a foto do médico
                    if (idMedico.isNotEmpty()) {
                        db.collection("usuarios").document(idMedico).get()
                            .addOnSuccessListener { userDoc ->
                                tvNomeMedico.text = userDoc.getString("nome") ?: "Médico Desconhecido"

                                val photoUrl = userDoc.getString("photoUrl")
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
                                    ivFotoMedico.setImageResource(android.R.drawable.ic_menu_agenda)
                                }
                            }
                    }

                    container.addView(view)
                }
            }
    }
}