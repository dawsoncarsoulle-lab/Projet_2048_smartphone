package com.example.jeu_2048

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Application du thème au démarrage
        val prefs = getSharedPreferences("GameSettings", android.content.Context.MODE_PRIVATE)
        if (prefs.getBoolean("dark_mode", false)) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        // Récupération des vues
        val btnPlay = findViewById<LinearLayout>(R.id.btnPlay)
        val btnChallenge = findViewById<Button>(R.id.btn_challenge)
        val btnMulti = findViewById<Button>(R.id.btn_multiplayer)
        addClickAnimation(btnMulti)

        btnMulti.setOnClickListener {
            startActivity(android.content.Intent(this, MultiplayerActivity::class.java))
        }
        val btnBestScore = findViewById<LinearLayout>(R.id.btnBestScore)
        val btnLeaderboard = findViewById<LinearLayout>(R.id.btnLeaderboard)
        val btnSettings = findViewById<LinearLayout>(R.id.btnSettings)
        val btnQuit = findViewById<LinearLayout>(R.id.btnQuit)

        // Ajout des animations de clic
        addClickAnimation(btnPlay)
        addClickAnimation(btnChallenge) // Animation pour le bouton défi
        addClickAnimation(btnBestScore)
        addClickAnimation(btnLeaderboard)
        addClickAnimation(btnSettings)
        addClickAnimation(btnQuit)

        // Bouton JOUER (Mode Normal)
        btnPlay.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("MODE_DEFI", false) // Mode normal
            startActivity(intent)
        }

        // Bouton MODE DÉFI (Chronomètre)
        btnChallenge.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("MODE_DEFI", true) // Mode contre-la-montre activé
            startActivity(intent)
        }

        btnBestScore.setOnClickListener {
            startActivity(Intent(this, StatisticsActivity::class.java))
        }

        btnLeaderboard.setOnClickListener {
            startActivity(Intent(this, LeaderboardActivity::class.java))
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        btnQuit.setOnClickListener {
            finish()
        }

        val btnTuto = findViewById<Button>(R.id.btn_tutorial)
        addClickAnimation(btnTuto)
        btnTuto.setOnClickListener {
            startActivity(android.content.Intent(this, TutorialActivity::class.java))
        }
    }

    // Fonction pour l'animation des boutons
    private fun addClickAnimation(view: View) {
        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                }
            }
            false // Retourne false pour que le OnClickListener soit quand même déclenché
        }
    }
}