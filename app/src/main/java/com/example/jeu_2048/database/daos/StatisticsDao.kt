package com.example.jeu_2048.database.daos

import androidx.room.Dao
import androidx.room.Query

@Dao
interface StatisticsDao {

    @Query("SELECT COUNT(*) from matches")
    fun totalGames(): Int

    @Query("SELECT MAX(score) FROM matches")
    fun getMaxScore(): Int

    @Query("SELECT COUNT(score) FROM matches WHERE score >= 2048")
    fun getGamesWon(): Int

}