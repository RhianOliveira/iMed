package com.example.imed

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class MenuAdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_admin)

        // Aplica o ajuste de margens (evita crash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Referências aos botões originais e novos cards
        val btnCadastroMedico = findViewById<MaterialButton>(R.id.btnCadastroMedico)
        val btnListaMedicos = findViewById<MaterialButton>(R.id.btnListaMedicos)
        val btnRelatorios = findViewById<MaterialButton>(R.id.btnRelatorios)
        val btnLogout = findViewById<MaterialButton>(R.id.btnLogoutAdmin)

        val cardCadastroMedico = findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardCadastroMedico)
        val cardListaMedicos = findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardListaMedicos)
        val cardRelatorios = findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardRelatorios)

        // Funções de Navegação
        val actionCadastro = { startActivity(Intent(this, CadastroMedicoActivity::class.java)) }
        val actionLista = { startActivity(Intent(this, ConsultaMedicosActivity::class.java)) }
        val actionRelatorios = { startActivity(Intent(this, RelatoriosActivity::class.java)) }

        btnCadastroMedico.setOnClickListener { actionCadastro() }
        cardCadastroMedico.setOnClickListener { actionCadastro() }

        btnListaMedicos.setOnClickListener { actionLista() }
        cardListaMedicos.setOnClickListener { actionLista() }

        btnRelatorios.setOnClickListener { actionRelatorios() }
        cardRelatorios.setOnClickListener { actionRelatorios() }

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}