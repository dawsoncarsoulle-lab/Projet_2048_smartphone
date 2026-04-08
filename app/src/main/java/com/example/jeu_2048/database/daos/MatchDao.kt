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

    @Query("SELECT * FROM matches WHERE is_running = 1 ORDER BY id DESC LIMIT 1")
    fun getRunningMatch(): Match?

    @Query("SELECT * FROM matches ORDER BY score DESC")
    fun getAllMatchesDescending(): List<Match>

    @Query("DELETE FROM matches")
    fun resetLeaderboard()

    // Annule la partie en cours si on change la taille de la grille
    @Query("UPDATE matches SET is_running = 0 WHERE is_running = 1")
    fun cancelRunningMatches()
}