package com.example.imed

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class HistoricoConsultasMedicoActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var adapter: HistoricoMedicoAdapter
    private val listaConsultas = mutableListOf<ConsultaHistorico>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historico_consultas_medico)

        findViewById<MaterialButton>(R.id.btnVoltar).setOnClickListener { finish() }

        val rv = findViewById<RecyclerView>(R.id.rvHistoricoMedico)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = HistoricoMedicoAdapter(listaConsultas) { consulta ->
            val intent = Intent(this, VisualizarProntuarioActivity::class.java)
            intent.putExtra("PRONTUARIO_ID", consulta.id)
            startActivity(intent)
        }
        rv.adapter = adapter

        loadHistory()

        val etSearch = findViewById<EditText>(R.id.etSearchPatient)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadHistory() {
        val uidMedico = auth.currentUser?.uid ?: return

        db.collection("agendamentos")
            .whereEqualTo("idMedico", uidMedico)
            .whereEqualTo("status", "concluido")
            .get()
            .addOnSuccessListener { querySnapshot ->
                listaConsultas.clear()

                if (querySnapshot.isEmpty) {
                    adapter.updateList(emptyList())
                    return@addOnSuccessListener
                }

                val totalDocs = querySnapshot.size()
                var processedCount = 0
                val temporaryList = mutableListOf<ConsultaHistorico>()

                for (doc in querySnapshot) {
                    val id = doc.id
                    val idPac = doc.getString("idPaciente") ?: ""
                    val data = doc.getString("data") ?: ""
                    val hora = doc.getString("horario") ?: ""

                    db.collection("usuarios").document(idPac).get().addOnSuccessListener { userDoc ->
                        val nomePac = userDoc.getString("nome") ?: "Paciente"
                        val photoUrl = userDoc.getString("photoUrl")

                        temporaryList.add(ConsultaHistorico(id, nomePac, "$data $hora", photoUrl))

                        processedCount++
                        if (processedCount == totalDocs) {
                            finalizeList(temporaryList)
                        }
                    }.addOnFailureListener {
                        processedCount++
                        if (processedCount == totalDocs) {
                            finalizeList(temporaryList)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao carregar histórico: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("HISTORICO_MEDICO", "Erro Firestore", e)
            }
    }

    private fun finalizeList(list: List<ConsultaHistorico>) {
        val sdf = SimpleDateFormat("d/M/yyyy HH:mm", Locale.getDefault())
        val sortedList = list.sortedByDescending {
            try {
                sdf.parse(it.dataHoraCheia)
            } catch (e: Exception) {
                Date(0)
            }
        }
        listaConsultas.clear()
        listaConsultas.addAll(sortedList)
        adapter.updateList(listaConsultas)
    }

    data class ConsultaHistorico(
        val id: String,
        val nomePaciente: String,
        val dataHoraCheia: String,
        val photoUrl: String?
    )

    class HistoricoMedicoAdapter(
        private var items: List<ConsultaHistorico>,
        private val onClick: (ConsultaHistorico) -> Unit
    ) : RecyclerView.Adapter<HistoricoMedicoAdapter.VH>() {

        private var fullList = items

        fun updateList(newList: List<ConsultaHistorico>) {
            this.fullList = newList
            this.items = newList
            notifyDataSetChanged()
        }

        fun filter(query: String) {
            items = if (query.isEmpty()) {
                fullList
            } else {
                fullList.filter { it.nomePaciente.contains(query, ignoreCase = true) }
            }
            notifyDataSetChanged()
        }

        class VH(view: View) : RecyclerView.ViewHolder(view) {
            val tvData: TextView = view.findViewById(R.id.tvDataAtendimento)
            val tvNome: TextView = view.findViewById(R.id.tvNomePacienteAtendido)
            val ivPhoto: ImageView = view.findViewById(R.id.ivPatientPhoto)
            val btnVer: MaterialButton = view.findViewById(R.id.btnVerProntuarioMedico)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_historico_medico, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = items[position]
            holder.tvData.text = item.dataHoraCheia
            holder.tvNome.text = item.nomePaciente

            if (!item.photoUrl.isNullOrEmpty()) {
                try {
                    holder.ivPhoto.imageTintList = null
                    holder.ivPhoto.clearColorFilter()

                    if (item.photoUrl.startsWith("http")) {
                        holder.ivPhoto.load(item.photoUrl) {
                            crossfade(true)
                            transformations(CircleCropTransformation())
                        }
                    } else {
                        val imageBytes = Base64.decode(item.photoUrl, Base64.DEFAULT)
                        val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        holder.ivPhoto.load(decodedImage) {
                            crossfade(true)
                            transformations(CircleCropTransformation())
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                holder.ivPhoto.setImageResource(android.R.drawable.ic_menu_myplaces)
            }

            holder.btnVer.setOnClickListener { onClick(item) }
            holder.itemView.setOnClickListener { onClick(item) }
        }

        override fun getItemCount() = items.size
    }
}