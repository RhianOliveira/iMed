package com.example.imed

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore

class ConsultaMedicosActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var etSearch: com.google.android.material.textfield.TextInputEditText
    private lateinit var db: FirebaseFirestore
    private var fullList = mutableListOf<Medico>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consulta_medicos)

        recyclerView = findViewById(R.id.recyclerViewMedicos)
        etSearch = findViewById(R.id.etSearch)
        recyclerView.layoutManager = LinearLayoutManager(this)
        db = FirebaseFirestore.getInstance()

        findViewById<MaterialButton>(R.id.btnVoltarConsulta).setOnClickListener { finish() }

        // Filtro de Busca em tempo real
        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterList(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun filterList(query: String) {
        val filtered = fullList.filter {
            it.nome.contains(query, ignoreCase = true) || it.especialidade.contains(query, ignoreCase = true)
        }
        recyclerView.adapter = MedicoAdapter(filtered) { medico ->
            val intent = Intent(this, DetalhesMedicosActivity::class.java)
            intent.putExtra("ID_MEDICO", medico.id)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        fullList.clear()

        db.collection("usuarios")
            .whereEqualTo("role", "medico")
            .whereEqualTo("status", "ativo")
            .get()
            .addOnSuccessListener { documentos ->
                for (doc in documentos) {
                    // Adicionamos a busca do photoUrl aqui
                    fullList.add(
                        Medico(
                            id = doc.id,
                            nome = doc.getString("nome") ?: "",
                            especialidade = doc.getString("especialidade") ?: "",
                            crm = doc.getString("crm") ?: "",
                            photoUrl = doc.getString("photoUrl")
                        )
                    )
                }
                filterList(etSearch.text.toString())
            }
    }
}