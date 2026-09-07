package com.example.imed

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation

class MedicoAdapter(
    private val medicos: List<Medico>,
    private val onItemClick: (Medico) -> Unit
) : RecyclerView.Adapter<MedicoAdapter.MedicoViewHolder>() {

    class MedicoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNome: TextView = view.findViewById(R.id.tvNomeMedico)
        val tvEspecialidade: TextView = view.findViewById(R.id.tvEspecialidadeMedico)
        val tvCrm: TextView = view.findViewById(R.id.tvCrmMedico)
        val ivFoto: ImageView = view.findViewById(R.id.ivFotoMedico)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_medico, parent, false)
        return MedicoViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicoViewHolder, position: Int) {
        val medico = medicos[position]
        holder.tvNome.text = medico.nome
        holder.tvEspecialidade.text = medico.especialidade
        holder.tvCrm.text = "CRM: ${medico.crm}"

        // Lógica para carregar a foto do médico (Base64 ou Link HTTP)
        if (!medico.photoUrl.isNullOrEmpty()) {
            try {
                holder.ivFoto.imageTintList = null
                holder.ivFoto.clearColorFilter()

                if (medico.photoUrl.startsWith("http")) {
                    holder.ivFoto.load(medico.photoUrl) {
                        crossfade(true)
                        transformations(CircleCropTransformation())
                    }
                } else {
                    val imageBytes = Base64.decode(medico.photoUrl, Base64.DEFAULT)
                    val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    holder.ivFoto.load(decodedImage) {
                        crossfade(true)
                        transformations(CircleCropTransformation())
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            // Caso não tenha foto cadastrada, exibe o ícone padrão
            holder.ivFoto.setImageResource(android.R.drawable.ic_menu_myplaces)
        }

        holder.itemView.setOnClickListener { onItemClick(medico) }
    }

    override fun getItemCount() = medicos.size
}