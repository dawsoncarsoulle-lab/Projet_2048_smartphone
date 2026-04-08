package com.example.jeu_2048.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class Match(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    @ColumnInfo(name = "score")
    var score: Int = 0,
    @ColumnInfo(name = "match_start")
    var matchStart: Long = 0,
    @ColumnInfo(name = "match_end")
    var matchEnd: Long? = null,
    @ColumnInfo(name = "is_running")
    var isRunning: Boolean = false,
    @ColumnInfo(name = "grid_state")
    var gridState: String = "" // NOUVELLE COLONNE POUR LA GRILLE
)