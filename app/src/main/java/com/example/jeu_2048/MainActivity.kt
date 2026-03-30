package com.example.jeu_2048

import android.app.Activity
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.example.jeu_2048.database.MatchDatabase
import kotlin.random.Random

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val db = MatchDatabase.getDatabase(this)

        val match = Game(this, db);
        match.start();
    }


}