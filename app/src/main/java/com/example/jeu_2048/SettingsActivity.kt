package com.example.jeu_2048

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import android.widget.RadioButton
import android.widget.RadioGroup

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = getSharedPreferences("GameSettings", Context.MODE_PRIVATE)

        val switchMusic = findViewById<Switch>(R.id.switch_music)
        val switchSounds = findViewById<Switch>(R.id.switch_sounds)
        val btnBack = findViewById<Button>(R.id.btn_back)
        val switchTheme = findViewById<Switch>(R.id.switch_theme)

        // --- MUSIQUE ET SONS ---
        switchMusic.isChecked = prefs.getBoolean("music", true)
        switchSounds.isChecked = prefs.getBoolean("sounds", true)

        switchMusic.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("music", isChecked).apply()
        }
        switchSounds.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("sounds", isChecked).apply()
        }

        val isDarkMode = prefs.getBoolean("dark_mode", false)
        switchTheme.isChecked = isDarkMode

        switchTheme.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_mode", isChecked).apply()

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            recreate() // Rafraîchit l'écran immédiatement
        }
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup_size)
        val db = com.example.jeu_2048.database.MatchDatabase.getDatabase(this)

        // Afficher la taille actuellement sauvegardée
        when (prefs.getInt("grid_size", 4)) {
            3 -> findViewById<RadioButton>(R.id.rb_3).isChecked = true
            4 -> findViewById<RadioButton>(R.id.rb_4).isChecked = true
            5 -> findViewById<RadioButton>(R.id.rb_5).isChecked = true
        }

        // Sauvegarder si l'utilisateur change la taille
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val newSize = when (checkedId) {
                R.id.rb_3 -> 3
                R.id.rb_5 -> 5
                else -> 4
            }
            prefs.edit().putInt("grid_size", newSize).apply()

            // On termine la partie en cours pour éviter un crash au retour
            db.matchDao().cancelRunningMatches()
        }

        // --- Ajoutez ceci dans le onCreate ---
        val switchAnimations = findViewById<Switch>(R.id.switch_animations)
        switchAnimations.isChecked = prefs.getBoolean("animations", true)

        switchAnimations.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("animations", isChecked).apply()
        }

        // --- Modifiez la vérification de la taille pour inclure le 6x6 ---
        when (prefs.getInt("grid_size", 4)) {
            3 -> findViewById<RadioButton>(R.id.rb_3).isChecked = true
            4 -> findViewById<RadioButton>(R.id.rb_4).isChecked = true
            5 -> findViewById<RadioButton>(R.id.rb_5).isChecked = true
            6 -> findViewById<RadioButton>(R.id.rb_6).isChecked = true // NOUVEAU
        }

        // --- Modifiez la sauvegarde du RadioGroup pour inclure le 6x6 ---
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val newSize = when (checkedId) {
                R.id.rb_3 -> 3
                R.id.rb_5 -> 5
                R.id.rb_6 -> 6 // NOUVEAU
                else -> 4
            }
            prefs.edit().putInt("grid_size", newSize).apply()
            db.matchDao().cancelRunningMatches()
        }

        // --- RETOUR ---
        btnBack.setOnClickListener {
            finish()
        }
    }
}