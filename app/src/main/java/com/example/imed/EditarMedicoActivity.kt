package com.example.imed

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class EditarMedicoActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    private var fotoBase64: String? = null
    private lateinit var ivProfile: ImageView
    private lateinit var medicoId: String

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            // Remove o filtro cinza original
            ivProfile.imageTintList = null
            ivProfile.clearColorFilter()

            ivProfile.load(uri) {
                transformations(CircleCropTransformation())
            }

            // Converte a imagem para salvar no banco de dados sem usar Storage
            fotoBase64 = uriToBase64(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_medico)

        medicoId = intent.getStringExtra("ID_MEDICO") ?: return

        ivProfile = findViewById(R.id.ivProfilePhoto)
        val cardPhoto = findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardProfilePhoto)

        cardPhoto.setOnClickListener {
            pickImage.launch("image/*")
        }

        val etNome = findViewById<TextInputEditText>(R.id.etNome)
        val etCpf = findViewById<TextInputEditText>(R.id.etCpf)
        val etDataNasc = findViewById<TextInputEditText>(R.id.etDataNasc)
        val etTelefone = findViewById<TextInputEditText>(R.id.etTelefone)
        val etCrm = findViewById<TextInputEditText>(R.id.etCrm)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)

        val btnSalvar = findViewById<MaterialButton>(R.id.btnSalvar)
        val btnVoltarTopo = findViewById<MaterialButton>(R.id.btnVoltar)

        btnVoltarTopo.setOnClickListener { finish() }

        // Carregar dados atuais do Firestore
        db.collection("usuarios").document(medicoId).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                etNome.setText(doc.getString("nome"))
                etCpf.setText(doc.getString("cpf"))
                etDataNasc.setText(doc.getString("dataNascimento"))
                etTelefone.setText(doc.getString("telefone"))
                etCrm.setText(doc.getString("crm"))
                etEmail.setText(doc.getString("email"))

                val photoUrl = doc.getString("photoUrl")
                if (!photoUrl.isNullOrEmpty()) {
                    try {
                        // Limpa o fundo cinza antes de exibir
                        ivProfile.imageTintList = null
                        ivProfile.clearColorFilter()

                        // Checa se é um link antigo ou o novo texto Base64
                        if (photoUrl.startsWith("http")) {
                            ivProfile.load(photoUrl) {
                                crossfade(true)
                                transformations(CircleCropTransformation())
                            }
                        } else {
                            // Decodifica o texto Base64 de volta para Imagem
                            val imageBytes = Base64.decode(photoUrl, Base64.DEFAULT)
                            val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            ivProfile.load(decodedImage) {
                                crossfade(true)
                                transformations(CircleCropTransformation())
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        // Salvar Alterações
        btnSalvar.setOnClickListener {
            val edits: Array<View> = arrayOf(etNome, etCpf, etDataNasc, etTelefone, etCrm, etEmail)
            updateDoctorData(fotoBase64, edits)
        }
    }

    private fun uriToBase64(uri: Uri): String? {
        return try {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }

            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, true)
            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val bytes = outputStream.toByteArray()

            Base64.encodeToString(bytes, Base64.DEFAULT)
        } catch (e: Exception) {
            null
        }
    }

    private fun updateDoctorData(photoBase64Str: String?, edits: Array<View>) {
        val names = listOf("nome", "cpf", "dataNascimento", "telefone", "crm", "email")
        val dadosAtualizados = mutableMapOf<String, Any>()

        edits.forEachIndexed { i, view ->
            val text = when(view) {
                is TextInputEditText -> view.text.toString()
                else -> ""
            }
            dadosAtualizados[names[i]] = text
        }

        // Se uma nova foto foi escolhida, atualizamos a String no banco
        if (photoBase64Str != null) {
            dadosAtualizados["photoUrl"] = photoBase64Str
        }

        db.collection("usuarios").document(medicoId).update(dadosAtualizados)
            .addOnSuccessListener {
                Toast.makeText(this, "Dados atualizados com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao atualizar.", Toast.LENGTH_SHORT).show()
            }
    }
}