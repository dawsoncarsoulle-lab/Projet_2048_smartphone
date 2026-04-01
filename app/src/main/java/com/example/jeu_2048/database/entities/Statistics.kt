package com.example.jeu_2048.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "matches")
data class Statistics(
    @ColumnInfo(name = "max_score")
    var maxScore: Int = 0,
    @ColumnInfo(name = "games_played")
    var gamesPlayed: Int = 0,
    @ColumnInfo(name = "games_won")
    var gamesWon: Int = 0,
)