package com.example.jeu_2048

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class MultiplayerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Applique le thème
        val prefs = getSharedPreferences("GameSettings", android.content.Context.MODE_PRIVATE)
        if (prefs.getBoolean("dark_mode", false)) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiplayer)

        // Initialisation des deux jeux (fixé en 4x4 pour que ça rentre à l'écran)
        val p1Game = PlayerGame(this, findViewById(R.id.grid_p1), findViewById(R.id.tv_score_p1), "Joueur 1")
        val p2Game = PlayerGame(this, findViewById(R.id.grid_p2), findViewById(R.id.tv_score_p2), "Joueur 2")

        p1Game.start()
        p2Game.start()

        // Zone tactile indépendante pour le Joueur 1 (Moitié basse)
        val zoneP1 = findViewById<LinearLayout>(R.id.zone_p1)
        zoneP1.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeTop() { p1Game.move("UP") }
            override fun onSwipeBottom() { p1Game.move("DOWN") }
            override fun onSwipeLeft() { p1Game.move("LEFT") }
            override fun onSwipeRight() { p1Game.move("RIGHT") }
        })

        // Zone tactile indépendante pour le Joueur 2 (Moitié haute)
        val zoneP2 = findViewById<LinearLayout>(R.id.zone_p2)
        zoneP2.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeTop() { p2Game.move("UP") }
            override fun onSwipeBottom() { p2Game.move("DOWN") }
            override fun onSwipeLeft() { p2Game.move("LEFT") }
            override fun onSwipeRight() { p2Game.move("RIGHT") }
        })
    }

    fun checkOverallGameOver(p1Game: PlayerGame, p2Game: PlayerGame) {
        if (!p1Game.isAlive && !p2Game.isAlive) {
            val msg = when {
                p1Game.score > p2Game.score -> "Le Joueur 1 gagne avec ${p1Game.score} pts !"
                p2Game.score > p1Game.score -> "Le Joueur 2 gagne avec ${p2Game.score} pts !"
                else -> "Égalité parfaite (${p1Game.score} pts) !"
            }
            android.app.AlertDialog.Builder(this)
                .setTitle("Fin de la partie !")
                .setMessage(msg)
                .setPositiveButton("Quitter") { _, _ -> finish() }
                .setCancelable(false)
                .show()
        }
    }
    // Mini-moteur de jeu pour gérer une grille
    inner class PlayerGame(
        private val activity: AppCompatActivity,
        private val gridLayout: GridLayout,
        private val tvScore: TextView,
        private val playerName: String
    ) {
        private var gridMatrix = Array(4) { IntArray(4) { 0 } }
        private val gridCells = Array(4) { arrayOfNulls<TextView>(4) }
        var score = 0
        var isAlive = true

        init {
            gridLayout.rowCount = 4
            gridLayout.columnCount = 4
            // Calcul d'une taille de case plus petite pour l'écran partagé
            val displayMetrics = activity.resources.displayMetrics
            val width = displayMetrics.widthPixels
            val height = displayMetrics.heightPixels
            val cellSize = (Math.min(width, height / 2) - (120 * displayMetrics.density)) / 4

            for (i in 0..3) {
                for (j in 0..3) {
                    val cell = TextView(activity).apply {
                        val params = GridLayout.LayoutParams().apply {
                            rowSpec = GridLayout.spec(i)
                            columnSpec = GridLayout.spec(j)
                            this.width = cellSize.toInt()
                            this.height = cellSize.toInt()
                            setMargins(4, 4, 4, 4)
                        }
                        layoutParams = params
                        gravity = Gravity.CENTER
                        textSize = 20f
                        setTypeface(null, Typeface.BOLD)
                    }
                    gridCells[i][j] = cell
                    gridLayout.addView(cell)
                }
            }
        }

        fun start() {
            spawnTile()
            spawnTile()
            updateUI()
        }

        fun move(direction: String) {
            if (!isAlive) return
            var hasMoved = false

            for (i in 0..3) {
                val line = mutableListOf<Int>()
                for (j in 0..3) {
                    when (direction) {
                        "LEFT", "RIGHT" -> line.add(gridMatrix[i][j])
                        "UP", "DOWN" -> line.add(gridMatrix[j][i])
                    }
                }

                if (direction == "RIGHT" || direction == "DOWN") line.reverse()
                val newLine = slideAndMerge(line)
                if (direction == "RIGHT" || direction == "DOWN") newLine.reverse()

                for (j in 0..3) {
                    val oldValue = when (direction) {
                        "LEFT", "RIGHT" -> gridMatrix[i][j]
                        else -> gridMatrix[j][i]
                    }
                    if (oldValue != newLine[j]) hasMoved = true
                    when (direction) {
                        "LEFT", "RIGHT" -> gridMatrix[i][j] = newLine[j]
                        "UP", "DOWN" -> gridMatrix[j][i] = newLine[j]
                    }
                }
            }

            if (hasMoved) {
                spawnTile()
                updateUI()
            } else if (isGameOver()) {
                isAlive = false
                Toast.makeText(activity, "$playerName est bloqué !", Toast.LENGTH_SHORT).show()
                (activity as MultiplayerActivity).checkOverallGameOver(
                    (activity).findViewById<View>(R.id.grid_p1).tag as? PlayerGame ?: this,
                    (activity).findViewById<View>(R.id.grid_p2).tag as? PlayerGame ?: this
                )
            }
        }

        private fun slideAndMerge(line: List<Int>): MutableList<Int> {
            val nonZeros = line.filter { it != 0 }.toMutableList()
            val merged = mutableListOf<Int>()
            var i = 0
            while (i < nonZeros.size) {
                if (i + 1 < nonZeros.size && nonZeros[i] == nonZeros[i + 1]) {
                    merged.add(nonZeros[i] * 2)
                    score += nonZeros[i] * 2
                    i += 2
                } else {
                    merged.add(nonZeros[i])
                    i++
                }
            }
            while (merged.size < 4) merged.add(0)
            return merged
        }

        private fun spawnTile() {
            val empty = mutableListOf<Pair<Int, Int>>()
            for (i in 0..3) for (j in 0..3) if (gridMatrix[i][j] == 0) empty.add(Pair(i, j))
            if (empty.isNotEmpty()) {
                val pos = empty.random()
                gridMatrix[pos.first][pos.second] = if (Random.nextDouble() < 0.9) 2 else 4
            }
        }

        private fun isGameOver(): Boolean {
            for (i in 0..3) for (j in 0..3) if (gridMatrix[i][j] == 0) return false
            for (i in 0..3) for (j in 0..3) {
                val c = gridMatrix[i][j]
                if (i < 3 && c == gridMatrix[i + 1][j]) return false
                if (j < 3 && c == gridMatrix[i][j + 1]) return false
            }
            return true
        }

        private fun updateUI() {
            tvScore.text = "Score : $score"
            for (i in 0..3) {
                for (j in 0..3) {
                    val value = gridMatrix[i][j]
                    val tv = gridCells[i][j]
                    tv?.text = if (value == 0) "" else value.toString()
                    tv?.setBackgroundColor(getTileColor(value))
                    tv?.setTextColor(if (value <= 4) activity.getColor(R.color.text_dark) else activity.getColor(R.color.text_light))
                }
            }
        }

        private fun getTileColor(value: Int): Int {
            return when (value) {
                0 -> activity.getColor(R.color.cell_empty)
                2 -> activity.getColor(R.color.tile_2)
                4 -> activity.getColor(R.color.tile_4)
                8 -> activity.getColor(R.color.tile_8)
                16 -> activity.getColor(R.color.tile_16)
                32 -> activity.getColor(R.color.tile_32)
                64 -> activity.getColor(R.color.tile_64)
                128 -> activity.getColor(R.color.tile_128)
                256 -> activity.getColor(R.color.tile_256)
                512 -> activity.getColor(R.color.tile_512)
                1024 -> activity.getColor(R.color.tile_1024)
                2048 -> activity.getColor(R.color.tile_2048)
                else -> activity.getColor(R.color.tile_2048)
            }
        }
    }
}