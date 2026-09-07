package com.example.imed

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.firestore.FirebaseFirestore

class RelatoriosActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    private lateinit var tvTotal: TextView
    private lateinit var tvRealizadas: TextView
    private lateinit var tvCanceladas: TextView
    private lateinit var tvMedicos: TextView
    private lateinit var tvPacientes: TextView
    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_relatorios)

        initViews()
        loadMetrics()
        loadAgendamentosData()

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnVoltar).setOnClickListener {
            finish()
        }
    }

    private fun initViews() {
        tvTotal = findViewById(R.id.tvTotalConsultas)
        tvRealizadas = findViewById(R.id.tvRealizadas)
        tvCanceladas = findViewById(R.id.tvCanceladas)
        tvMedicos = findViewById(R.id.tvMedicosAtivos)
        tvPacientes = findViewById(R.id.tvTotalPacientes)
        pieChart = findViewById(R.id.pieChartSpecialties)
        barChart = findViewById(R.id.barChartMonthly)
    }

    private fun loadMetrics() {
        // Médicos Ativos
        db.collection("usuarios")
            .whereEqualTo("role", "medico")
            .whereEqualTo("status", "ativo")
            .get()
            .addOnSuccessListener { tvMedicos.text = it.size().toString() }

        // Total Pacientes
        db.collection("usuarios")
            .whereEqualTo("role", "paciente")
            .get()
            .addOnSuccessListener { tvPacientes.text = it.size().toString() }
    }

    private fun loadAgendamentosData() {
        // 1. Primeiro buscamos as especialidades dos médicos para poder cruzar os dados
        db.collection("usuarios").whereEqualTo("role", "medico").get().addOnSuccessListener { userSnapshot ->
            val doctorSpecialties = mutableMapOf<String, String>()
            for (userDoc in userSnapshot) {
                val id = userDoc.id
                val esp = userDoc.getString("especialidade") ?: "Outros"
                doctorSpecialties[id] = esp
            }

            // 2. Agora buscamos os agendamentos com os nomes de status corretos do seu banco
            db.collection("agendamentos").get().addOnSuccessListener { querySnapshot ->
                val total = querySnapshot.size()
                var realizadas = 0
                var canceladas = 0

                val specialtiesCount = mutableMapOf<String, Int>()
                val monthlyCount = IntArray(12) { 0 }

                for (doc in querySnapshot) {
                    val status = doc.getString("status") ?: ""
                    
                    // Mapeamento conforme seu banco de dados
                    if (status.equals("concluido", true)) {
                        realizadas++
                    } else if (status.equals("cancelado", true) || status.equals("ausente", true)) {
                        canceladas++
                    }

                    // Especialidades (buscando pelo idMedico)
                    val idMedico = doc.getString("idMedico") ?: ""
                    val esp = doctorSpecialties[idMedico] ?: "Indefinida"
                    specialtiesCount[esp] = (specialtiesCount[esp] ?: 0) + 1

                    // Mensal (apenas concluídas)
                    if (status.equals("concluido", true)) {
                        val dataStr = doc.getString("data") ?: "" // Formato "5/7/2026"
                        if (dataStr.isNotEmpty()) {
                            try {
                                val parts = dataStr.split("/")
                                if (parts.size >= 2) {
                                    val month = parts[1].toInt() - 1
                                    if (month in 0..11) {
                                        monthlyCount[month]++
                                    }
                                }
                            } catch (e: Exception) {}
                        }
                    }
                }

                tvTotal.text = total.toString()
                tvRealizadas.text = realizadas.toString()
                tvCanceladas.text = canceladas.toString()

                setupPieChart(specialtiesCount)
                setupBarChart(monthlyCount)
            }
        }
    }

    private fun setupPieChart(data: Map<String, Int>) {
        val entries = mutableListOf<PieEntry>()
        val sortedList = data.entries.sortedByDescending { it.value }
        
        var otherSum = 0
        sortedList.forEachIndexed { index, entry ->
            if (index < 9) {
                entries.add(PieEntry(entry.value.toFloat(), entry.key))
            } else {
                otherSum += entry.value
            }
        }

        if (otherSum > 0) {
            entries.add(PieEntry(otherSum.toFloat(), "Outros"))
        }

        val dataSet = PieDataSet(entries, "")
        val colors = mutableListOf<Int>()
        for (c in ColorTemplate.MATERIAL_COLORS) colors.add(c)
        for (c in ColorTemplate.JOYFUL_COLORS) colors.add(c)
        for (c in ColorTemplate.VORDIPLOM_COLORS) colors.add(c)
        
        dataSet.colors = colors
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 14f

        val pieData = PieData(dataSet)
        pieChart.data = pieData
        pieChart.description.isEnabled = false
        pieChart.centerText = "Especialidades"
        pieChart.setHoleColor(Color.TRANSPARENT)
        pieChart.legend.isWordWrapEnabled = true
        pieChart.animateY(1000)
        pieChart.invalidate()
    }

    private fun setupBarChart(monthlyData: IntArray) {
        val entries = mutableListOf<BarEntry>()
        val labels = arrayOf("Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez")

        for (i in 0..11) {
            entries.add(BarEntry(i.toFloat(), monthlyData[i].toFloat()))
        }

        val dataSet = BarDataSet(entries, "Consultas Realizadas")
        dataSet.color = Color.parseColor("#06152D")
        dataSet.valueTextSize = 10f
        
        val barData = BarData(dataSet)
        barChart.data = barData
        
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        barChart.xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        barChart.xAxis.setDrawGridLines(false)
        barChart.xAxis.granularity = 1f
        barChart.xAxis.labelCount = 12
        
        barChart.axisLeft.setDrawGridLines(false)
        barChart.axisRight.isEnabled = false
        barChart.description.isEnabled = false
        barChart.animateY(1000)
        barChart.invalidate()
    }
}