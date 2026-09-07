package com.example.imed

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class CadastroPacienteActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var fotoBase64: String? = null
    private lateinit var ivProfile: ImageView

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            // Remove definitivamente o filtro de cor (tint) do XML
            ivProfile.imageTintList = null
            ivProfile.clearColorFilter()

            // Carrega a foto redondinha
            ivProfile.load(uri) {
                transformations(CircleCropTransformation())
            }

            // Converte a imagem para Base64 para salvar no Firestore
            fotoBase64 = uriToBase64(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_paciente)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        ivProfile = findViewById(R.id.ivProfilePhoto)
        val cardPhoto = findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardProfilePhoto)

        cardPhoto.setOnClickListener {
            pickImage.launch("image/*")
        }

        val etNome = findViewById<TextInputEditText>(R.id.etNomeCadastro)
        val etCpf = findViewById<TextInputEditText>(R.id.etCpfCadastro)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmailCadastro)
        val etSenha = findViewById<TextInputEditText>(R.id.etSenhaCadastro)
        val btnFinalizar = findViewById<MaterialButton>(R.id.btnFinalizarCadastro)
        val btnCancelar = findViewById<MaterialButton>(R.id.btnCancelarCadastro)
        val tvJaTemConta = findViewById<TextView>(R.id.tvAlreadyHaveAccount)

        MaskHelper.mask(etCpf, "###.###.###-##")

        btnFinalizar.setOnClickListener {
            val nome = etNome.text.toString().trim()
            val cpf = etCpf.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val senha = etSenha.text.toString().trim()

            if (nome.isNotEmpty() && cpf.isNotEmpty() && email.isNotEmpty() && senha.isNotEmpty()) {
                cadastrarPaciente(nome, cpf, email, senha)
            } else {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancelar.setOnClickListener { finish() }
        tvJaTemConta.setOnClickListener { finish() }
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

    private fun cadastrarPaciente(nome: String, cpf: String, email: String, senha: String) {
        auth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                    savePatientData(uid, fotoBase64, nome, cpf, email)
                } else {
                    Toast.makeText(this, "Erro no Auth: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun savePatientData(uid: String, photoBase64Str: String?, nome: String, cpf: String, email: String) {
        val usuarioMap = hashMapOf<String, Any>(
            "nome" to nome,
            "cpf" to cpf,
            "email" to email,
            "role" to "paciente"
        )

        if (photoBase64Str != null) {
            usuarioMap["photoUrl"] = photoBase64Str
        }

        db.collection("usuarios").document(uid).set(usuarioMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Paciente cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MenuPacienteActivity::class.java)
                startActivity(intent)
                finishAffinity()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao salvar no banco: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}