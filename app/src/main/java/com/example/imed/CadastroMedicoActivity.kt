package com.example.imed

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class CadastroMedicoActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var fotoBase64: String? = null
    private lateinit var ivProfile: ImageView
    private lateinit var etEspecialidade: AutoCompleteTextView
    private val especialidadesList = mutableListOf<String>()

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            // Remove o filtro cinza original
            ivProfile.imageTintList = null
            ivProfile.clearColorFilter()

            ivProfile.load(uri) {
                transformations(CircleCropTransformation())
            }

            // Converte a imagem para Base64 para salvar no Firestore
            fotoBase64 = uriToBase64(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_medico)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        ivProfile = findViewById(R.id.ivProfilePhoto)
        val cardPhoto = findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardProfilePhoto)

        cardPhoto.setOnClickListener {
            pickImage.launch("image/*")
        }

        val etNome = findViewById<TextInputEditText>(R.id.etNome)
        val etCpf = findViewById<TextInputEditText>(R.id.etCpf)
        val etDataNasc = findViewById<TextInputEditText>(R.id.etDataNasc)
        val etTelefone = findViewById<TextInputEditText>(R.id.etTelefone)
        etEspecialidade = findViewById(R.id.etEspecialidade)
        val tvAddSpecialty = findViewById<TextView>(R.id.tvAddSpecialty)
        val etCrm = findViewById<TextInputEditText>(R.id.etCrm)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etSenha = findViewById<TextInputEditText>(R.id.etSenha)
        val btnCadastrar = findViewById<MaterialButton>(R.id.btnCadastrar)
        val btnVoltar = findViewById<MaterialButton>(R.id.btnVoltar)

        MaskHelper.mask(etCpf, "###.###.###-##")
        MaskHelper.mask(etTelefone, "(##) #####-####")
        MaskHelper.mask(etDataNasc, "##/##/####")

        loadEspecialidades()

        tvAddSpecialty.setOnClickListener {
            showAddSpecialtyDialog()
        }

        etEspecialidade.setOnClickListener {
            etEspecialidade.showDropDown()
        }

        btnCadastrar.setOnClickListener {
            val nome = etNome.text.toString().trim()
            val cpf = etCpf.text.toString().trim()
            val dataNasc = etDataNasc.text.toString().trim()
            val telefone = etTelefone.text.toString().trim()
            val especialidade = etEspecialidade.text.toString().trim()
            val crm = etCrm.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val senha = etSenha.text.toString().trim()

            if (nome.isEmpty() || cpf.isEmpty() || dataNasc.isEmpty() ||
                telefone.isEmpty() || especialidade.isEmpty() || crm.isEmpty() ||
                email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos obrigatórios!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, senha)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid ?: return@addOnSuccessListener
                    // Passa a foto convertida (Base64) direto para o Firestore
                    saveDoctorData(uid, fotoBase64, nome, cpf, dataNasc, telefone, especialidade, crm, email)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro no cadastro: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        btnVoltar.setOnClickListener { finish() }
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

    private fun loadEspecialidades() {
        db.collection("especialidades")
            .orderBy("nome")
            .get()
            .addOnSuccessListener { documents ->
                especialidadesList.clear()
                for (doc in documents) {
                    val nome = doc.getString("nome")
                    if (nome != null) especialidadesList.add(nome)
                }
                val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, especialidadesList)
                etEspecialidade.setAdapter(adapter)
            }
            .addOnFailureListener {
                val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, emptyList<String>())
                etEspecialidade.setAdapter(adapter)
            }
    }

    private fun showAddSpecialtyDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Nova Especialidade")

        val input = EditText(this)
        input.hint = "Nome da especialidade"
        val paddingPx = (24 * resources.displayMetrics.density).toInt()
        input.setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
        builder.setView(input)

        builder.setPositiveButton("Adicionar") { dialog, _ ->
            val novaEsp = input.text.toString().trim()
            if (novaEsp.isNotEmpty()) {
                saveNewSpecialty(novaEsp)
            } else {
                Toast.makeText(this, "Digite o nome da especialidade", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun saveNewSpecialty(nome: String) {
        val data = hashMapOf("nome" to nome)
        db.collection("especialidades").add(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Especialidade adicionada!", Toast.LENGTH_SHORT).show()
                loadEspecialidades()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao adicionar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveDoctorData(uid: String, photoBase64Str: String?, vararg data: String) {
        val medicoData = hashMapOf<String, Any>(
            "nome" to data[0],
            "cpf" to data[1],
            "dataNascimento" to data[2],
            "telefone" to data[3],
            "especialidade" to data[4],
            "crm" to data[5],
            "email" to data[6],
            "role" to "medico",
            "status" to "ativo"
        )

        if (photoBase64Str != null) {
            medicoData["photoUrl"] = photoBase64Str
        }

        db.collection("usuarios").document(uid).set(medicoData)
            .addOnSuccessListener {
                Toast.makeText(this, "Médico cadastrado com sucesso!", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao salvar dados no banco.", Toast.LENGTH_SHORT).show()
            }
    }
}