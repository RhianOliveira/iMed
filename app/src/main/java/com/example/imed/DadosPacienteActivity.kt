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
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
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

class DadosPacienteActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var fotoBase64: String? = null
    private lateinit var ivProfile: ImageView

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            // Remove o filtro cinza do ícone de câmera original
            ivProfile.imageTintList = null
            ivProfile.clearColorFilter()

            ivProfile.load(uri) {
                transformations(CircleCropTransformation())
            }
            // Converte a nova foto para salvar sem usar o Storage
            fotoBase64 = uriToBase64(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dados_paciente)

        val uid = auth.currentUser?.uid ?: return

        ivProfile = findViewById(R.id.ivProfilePhoto)
        val cardPhoto = findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardProfilePhoto)

        cardPhoto.setOnClickListener {
            pickImage.launch("image/*")
        }

        // Mapeamento dos campos
        val etNome = findViewById<TextInputEditText>(R.id.etNomePaciente)
        val etCpf = findViewById<TextInputEditText>(R.id.etCpf)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etGenero = findViewById<AutoCompleteTextView>(R.id.etGenero)
        val etAlergias = findViewById<TextInputEditText>(R.id.etAlergias)
        val etTipoSanguineo = findViewById<AutoCompleteTextView>(R.id.etTipoSanguineo)
        val etRg = findViewById<TextInputEditText>(R.id.etRg)
        val etCelular = findViewById<TextInputEditText>(R.id.etCelular)
        val etDataNasc = findViewById<TextInputEditText>(R.id.etDataNasc)
        val etCep = findViewById<TextInputEditText>(R.id.etCep)
        val etUf = findViewById<AutoCompleteTextView>(R.id.etUf)
        val etCidade = findViewById<TextInputEditText>(R.id.etCidade)
        val etEndereco = findViewById<TextInputEditText>(R.id.etEndereco)
        val etNumero = findViewById<TextInputEditText>(R.id.etNumero)

        // Configuração dos Dropdowns
        setupDropdowns(etGenero, etTipoSanguineo, etUf)

        // Forçar a exibição da lista ao clicar
        etGenero.setOnClickListener { etGenero.showDropDown() }
        etTipoSanguineo.setOnClickListener { etTipoSanguineo.showDropDown() }
        etUf.setOnClickListener { etUf.showDropDown() }

        // Aplicando as Máscaras
        MaskHelper.mask(etCpf, "###.###.###-##")
        MaskHelper.mask(etCelular, "(##) #####-####")
        MaskHelper.mask(etDataNasc, "##/##/####")
        MaskHelper.mask(etCep, "#####-###")
        MaskHelper.mask(etRg, "##.###.###-#")

        // Carregar informações iniciais
        db.collection("usuarios").document(uid).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                etNome.setText(doc.getString("nome"))
                etCpf.setText(doc.getString("cpf"))
                etEmail.setText(doc.getString("email"))

                doc.getString("genero")?.let { etGenero.setText(it, false) }
                etAlergias.setText(doc.getString("alergias"))
                doc.getString("tipoSanguineo")?.let { etTipoSanguineo.setText(it, false) }
                etRg.setText(doc.getString("rg"))
                etCelular.setText(doc.getString("celular"))
                etDataNasc.setText(doc.getString("dataNascimento"))
                etCep.setText(doc.getString("cep"))
                doc.getString("uf")?.let { etUf.setText(it, false) }
                etCidade.setText(doc.getString("cidade"))
                etEndereco.setText(doc.getString("endereco"))
                etNumero.setText(doc.getString("numero"))

                val photoUrl = doc.getString("photoUrl")
                if (!photoUrl.isNullOrEmpty()) {
                    try {
                        // Limpa o fundo cinza
                        ivProfile.imageTintList = null
                        ivProfile.clearColorFilter()

                        // Checa se é um link antigo do Storage ou o nosso novo texto Base64
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

        // Salvar novas informações
        findViewById<MaterialButton>(R.id.btnSalvar).setOnClickListener {
            val edits: Array<View> = arrayOf(etNome, etCpf, etEmail, etGenero, etAlergias, etTipoSanguineo, etRg, etCelular, etDataNasc, etCep, etUf, etCidade, etEndereco, etNumero)
            saveData(uid, fotoBase64, edits)
        }

        findViewById<MaterialButton>(R.id.btnVoltar).setOnClickListener { finish() }

        // Excluir Conta
        findViewById<MaterialButton>(R.id.btnExcluir).setOnClickListener {
            db.collection("usuarios").document(uid).update("status", "inativo")
                .addOnSuccessListener {
                    Toast.makeText(this, "Conta desativada.", Toast.LENGTH_SHORT).show()
                    auth.signOut()
                    finish()
                }
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

    private fun setupDropdowns(etGen: AutoCompleteTextView, etTip: AutoCompleteTextView, etEst: AutoCompleteTextView) {
        val generos = arrayOf("Masculino", "Feminino", "Não-Binário", "Agênero", "Prefiro Não Dizer")
        val tipos = arrayOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
        val ufs = arrayOf("AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO")

        etGen.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, generos))
        etTip.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, tipos))
        etEst.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, ufs))
    }

    private fun saveData(uid: String, photoBase64Str: String?, edits: Array<View>) {
        val names = listOf("nome", "cpf", "email", "genero", "alergias", "tipoSanguineo", "rg", "celular", "dataNascimento", "cep", "uf", "cidade", "endereco", "numero")
        val novosDados = mutableMapOf<String, Any>()

        edits.forEachIndexed { i, view ->
            val text = when(view) {
                is TextInputEditText -> view.text.toString()
                is AutoCompleteTextView -> view.text.toString()
                else -> ""
            }
            novosDados[names[i]] = text
        }

        // Se a foto tiver sido alterada, atualizamos a String no banco
        if (photoBase64Str != null) {
            novosDados["photoUrl"] = photoBase64Str
        }

        db.collection("usuarios").document(uid).update(novosDados)
            .addOnSuccessListener {
                Toast.makeText(this, "Dados atualizados com sucesso!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao salvar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}