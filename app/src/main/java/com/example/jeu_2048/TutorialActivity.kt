package com.example.jeu_2048

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TutorialActivity : AppCompatActivity() {

    private var step = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("GameSettings", android.content.Context.MODE_PRIVATE)
        if (prefs.getBoolean("dark_mode", false)) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)

        val tvText = findViewById<TextView>(R.id.tv_tuto_text)
        val tvAnim = findViewById<TextView>(R.id.tv_tuto_animation)
        val btnNext = findViewById<Button>(R.id.btn_tuto_next)
        val btnBack = findViewById<Button>(R.id.btn_back_tuto)

        btnBack.setOnClickListener { finish() }

        tvAnim.animate().scaleX(1.1f).scaleY(1.1f).setDuration(500).withEndAction(object : Runnable {
            override fun run() {
                tvAnim.animate().scaleX(1.0f).scaleY(1.0f).setDuration(500).withEndAction(this).start()
            }
        }).start()

        btnNext.setOnClickListener {
            step++
            when (step) {
                2 -> {
                    tvText.text = "Lorsque deux tuiles avec le même numéro se touchent, elles fusionnent en une seule !"
                    tvAnim.text = "4"
                    tvAnim.setBackgroundColor(getColor(R.color.tile_8))
                }
                3 -> {
                    tvText.text = "Continuez à fusionner les tuiles pour atteindre le score ultime de 2048. Attention à ne pas bloquer la grille !"
                    tvAnim.text = "2048"
                    tvAnim.setBackgroundColor(getColor(R.color.tile_2048))
                    tvAnim.setTextColor(getColor(R.color.text_light))
                    btnNext.text = "J'AI COMPRIS !"
                }
                4 -> {
                    finish()
                }
            }
        }
    }
}