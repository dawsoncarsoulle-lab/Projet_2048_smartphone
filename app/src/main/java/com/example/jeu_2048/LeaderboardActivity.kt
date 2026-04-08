package com.example.jeu_2048

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.example.jeu_2048.database.MatchDatabase
import androidx.appcompat.app.AppCompatActivity

class LeaderboardActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        val db = MatchDatabase.getDatabase(this)
        val container = findViewById<LinearLayout>(R.id.leaderboard_container)
        val btnReset = findViewById<Button>(R.id.btn_reset_leaderboard)
        val btnBack = findViewById<Button>(R.id.btn_back)

        fun loadScores() {
            container.removeAllViews()
            val matches = db.matchDao().getAllMatchesDescending()

            if (matches.isEmpty()) {
                val emptyText = TextView(this)
                emptyText.text = "Aucun score pour le moment."
                emptyText.textSize = 18f
                emptyText.setTextColor(getColor(R.color.text_dark))
                container.addView(emptyText)
                return
            }

            matches.forEachIndexed { index, match ->
                val scoreView = TextView(this)
                scoreView.text = "${index + 1}. Score : ${match.score}"
                scoreView.textSize = 24f
                scoreView.setTextColor(getColor(R.color.text_dark))
                scoreView.setPadding(0, 16, 0, 16)
                container.addView(scoreView)
            }
        }

        loadScores()
        btnReset.setOnClickListener {
            db.matchDao().resetLeaderboard()
            loadScores()
        }
        btnBack.setOnClickListener {
            finish()
        }
    }
}