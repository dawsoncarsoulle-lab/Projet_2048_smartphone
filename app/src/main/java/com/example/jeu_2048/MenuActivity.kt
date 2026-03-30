package com.example.jeu_2048

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.example.jeu_2048.database.MatchDatabase

class MenuActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val db = MatchDatabase.getDatabase(this)
        val btnPlay = findViewById<LinearLayout>(R.id.btnPlay)
        val btnBestScore = findViewById<LinearLayout>(R.id.btnBestScore)
        val btnLeaderboard = findViewById<LinearLayout>(R.id.btnLeaderboard)
        val btnSettings = findViewById<LinearLayout>(R.id.btnSettings)
        val btnQuit = findViewById<LinearLayout>(R.id.btnQuit)

        addClickAnimation(btnPlay)
        addClickAnimation(btnBestScore)
        addClickAnimation(btnLeaderboard)
        addClickAnimation(btnSettings)
        addClickAnimation(btnQuit)

        btnPlay.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        btnBestScore.setOnClickListener {
            val bestScore = db.matchDao().getBestScore()
            Toast.makeText(this, "Meilleur score : $bestScore", Toast.LENGTH_SHORT).show()
        }

        btnLeaderboard.setOnClickListener {
            Toast.makeText(this, "Classement bientôt disponible", Toast.LENGTH_SHORT).show()
        }

        btnSettings.setOnClickListener {
            Toast.makeText(this, "Paramètres bientôt disponibles", Toast.LENGTH_SHORT).show()
        }

        btnQuit.setOnClickListener {
            finish()
        }
    }

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
            false
        }
    }
}