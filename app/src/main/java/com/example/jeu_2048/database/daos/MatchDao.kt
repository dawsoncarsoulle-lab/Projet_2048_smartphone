package com.example.jeu_2048.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.jeu_2048.database.entities.Match

@Dao
interface MatchDao {
    @Insert
    fun insertGame(match: Match): Long

    @Query("SELECT * from matches WHERE id = :matchId")
    fun selectGame(matchId: Int): Match

    @Query("SELECT MAX(score) FROM matches")
    fun getBestScore(): Int

    @Update
    fun updatePoints(match: Match)

    @Update
    fun updateGameEnd(match: Match)
}