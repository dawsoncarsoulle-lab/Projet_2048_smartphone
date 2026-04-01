package com.example.jeu_2048.database.daospackage com.example.jeu_2048.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.jeu_2048.database.entities.Match

@Dao
interface StatisticsDao {

    @Query("SELECT COUNT(*) from matches")
    fun totalGames(): Int

    @Query("SELECT MAX(score) FROM matches")
    fun getMaxScore(): Int

    @Query("SELECT COUNT(score) FROM matches WHERE score >= 2048")
    fun getGamesWon(): Int

}