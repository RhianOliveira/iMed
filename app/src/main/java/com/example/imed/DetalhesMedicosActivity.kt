package com.example.imed

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore

class DetalhesMedicosActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var medicoId: String
    private lateinit var ivFotoDetalhe: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhes_medicos)

        db = FirebaseFirestore.getInstance()
        medicoId = intent.getStringExtra("ID_MEDICO") ?: return

        ivFotoDetalhe = findViewById(R.id.ivFotoDetalhe)

        findViewById<MaterialButton>(R.id.btnInativar).setOnClickListener {
            db.collection("usuarios").document(medicoId)
                .update("status", "inativo")
                .addOnSuccessListener {
                    Toast.makeText(this, "Médico inativado!", Toast.LENGTH_SHORT).show()
                    finish()
                }
        }

        findViewById<MaterialButton>(R.id.btnEditar).setOnClickListener {
            val intent = Intent(this, EditarMedicoActivity::class.java)
            intent.putExtra("ID_MEDICO", medicoId)
            startActivity(intent)
        }

        findViewById<MaterialButton>(R.id.btnVoltarDetalhes).setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        db.collection("usuarios").document(medicoId).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                findViewById<TextView>(R.id.tvNomeDetalhe).text = doc.getString("nome") ?: ""
                findViewById<TextView>(R.id.tvCpfDetalhe).text = doc.getString("cpf") ?: ""
                findViewById<TextView>(R.id.tvDataNascDetalhe).text = doc.getString("dataNascimento") ?: ""
                findViewById<TextView>(R.id.tvTelDetalhe).text = doc.getString("telefone") ?: ""
                findViewById<TextView>(R.id.tvEspDetalhe).text = doc.getString("especialidade") ?: ""
                findViewById<TextView>(R.id.tvCrmDetalhe).text = doc.getString("crm") ?: ""

                // Carregamento da foto do médico
                val photoUrl = doc.getString("photoUrl")
                if (!photoUrl.isNullOrEmpty()) {
                    try {
                        ivFotoDetalhe.imageTintList = null
                        ivFotoDetalhe.clearColorFilter()

                        if (photoUrl.startsWith("http")) {
                            ivFotoDetalhe.load(photoUrl) {
                                crossfade(true)
                                transformations(CircleCropTransformation())
                            }
                        } else {
                            val imageBytes = Base64.decode(photoUrl, Base64.DEFAULT)
                            val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            ivFotoDetalhe.load(decodedImage) {
                                crossfade(true)
                                transformations(CircleCropTransformation())
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    ivFotoDetalhe.setImageResource(android.R.drawable.ic_menu_myplaces)
                }
            }
        }
    }
}