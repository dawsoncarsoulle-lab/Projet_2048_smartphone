package com.example.jeu_2048

import android.app.Activity
import android.database.sqlite.SQLiteOpenHelper
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.example.jeu_2048.database.MatchDatabase
import kotlin.random.Random

class MainActivity : Activity() {
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mediaPlayer = MediaPlayer.create(this, R.raw.nutus)

        val db = MatchDatabase.getDatabase(this)
        mediaPlayer.start()
        val match = Game(this, db);
        match.start();
    }


}