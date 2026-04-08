package com.example.jeu_2048

import android.app.Activity
import android.database.sqlite.SQLiteOpenHelper
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.example.jeu_2048.database.MatchDatabase
import kotlin.random.Random
import androidx.appcompat.app.AppCompatActivity

class MainActivity : Activity() {
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mediaPlayer = MediaPlayer.create(this, R.raw.nutus)
        val prefs = getSharedPreferences("GameSettings", android.content.Context.MODE_PRIVATE)
        if (prefs.getBoolean("music", true)) {
            mediaPlayer.start()
        }
        val db = MatchDatabase.getDatabase(this)
        val runningMatch = db.matchDao().getRunningMatch()
        val match = if (runningMatch != null) {
            Game(this, db, runningMatch.id)
        } else {
            Game(this, db)
        }
        match.start()
        val btnRestart = findViewById<android.widget.Button>(R.id.btn_restart)
        btnRestart.setOnClickListener {
            match.restart()
        }
    }


}