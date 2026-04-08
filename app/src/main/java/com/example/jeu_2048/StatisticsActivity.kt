package com.example.jeu_2048

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.jeu_2048.database.MatchDatabase

class StatisticsActivity : AppCompatActivity() { // Héritage de AppCompatActivity corrigé
    override fun onCreate(savedInstanceState: Bundle?) {
        // Applique le thème sombre/clair
        val prefs = getSharedPreferences("GameSettings", android.content.Context.MODE_PRIVATE)
        if (prefs.getBoolean("dark_mode", false)) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        val db = MatchDatabase.getDatabase(this)
        val statsDao = db.statisticsDao()

        val tvTotalGames = findViewById<TextView>(R.id.tv_total_games)
        val tvBestScore = findViewById<TextView>(R.id.tv_best_score)
        val tvGamesWon = findViewById<TextView>(R.id.tv_games_won)
        val btnBack = findViewById<Button>(R.id.btn_back_stats)
        val btnShare = findViewById<Button>(R.id.btn_share_score)

        // Récupération des données
        val totalGames = statsDao.totalGames()
        val bestScore = statsDao.getMaxScore() // Retourne 0 si aucune partie
        val gamesWon = statsDao.getGamesWon()

        // Mise à jour du texte
        tvTotalGames.text = "Parties jouées : $totalGames"
        tvBestScore.text = "Meilleur score : $bestScore"
        tvGamesWon.text = "Victoires (2048) : $gamesWon"

        btnBack.setOnClickListener {
            finish()
        }

        // Nouveau partage avec capture d'écran
        btnShare.setOnClickListener {
            // 1. Prendre une capture de l'écran des statistiques
            val view = window.decorView.rootView
            val bitmap = android.graphics.Bitmap.createBitmap(view.width, view.height, android.graphics.Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            view.draw(canvas)

            try {
                // 2. Sauvegarder l'image dans le cache
                val cachePath = java.io.File(cacheDir, "images")
                cachePath.mkdirs()
                val file = java.io.File(cachePath, "score.png")
                val stream = java.io.FileOutputStream(file)
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
                stream.close()

                // 3. Partager l'image
                val uri = androidx.core.content.FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                    putExtra(android.content.Intent.EXTRA_TEXT, "Je viens de marquer $bestScore points sur 2048 ! Peux-tu me battre ?")
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(android.content.Intent.createChooser(shareIntent, "Partager mon record via..."))
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Erreur lors de la création de l'image", Toast.LENGTH_SHORT).show()
            }
        }
    }
}