package com.example.jeu_2048

import android.os.CountDownTimer
import android.view.Gravity
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import android.graphics.Typeface
import com.example.jeu_2048.database.MatchDatabase
import com.example.jeu_2048.database.entities.Match
import kotlin.random.Random

class Game(private val activity: MainActivity, private val db: MatchDatabase, private val id : Int? = null) {

    private val prefs = activity.getSharedPreferences("GameSettings", android.content.Context.MODE_PRIVATE)
    private val gridSize = prefs.getInt("grid_size", 4)

    private val isChallengeMode = activity.intent.getBooleanExtra("MODE_DEFI", false)
    private var challengeTimer: CountDownTimer? = null
    private var gameActive = true

    private var gridMatrix = Array(gridSize) { IntArray(gridSize) { 0 } }
    private val gridCells = Array(gridSize) { arrayOfNulls<TextView>(gridSize) }

    public var score = 0
    var currentMatch : Match = Match(score = score, matchStart = System.currentTimeMillis(), isRunning = true)
    private lateinit var tvScore: TextView

    init {
        tvScore = activity.findViewById(R.id.tv_score)
        val gridLayout = activity.findViewById<GridLayout>(R.id.game_grid)

        gridLayout.removeAllViews()
        gridLayout.rowCount = gridSize
        gridLayout.columnCount = gridSize

        val displayMetrics = activity.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val paddingAndMargins = (32 + (gridSize * 16)) * displayMetrics.density
        val cellSize = ((screenWidth - paddingAndMargins) / gridSize).toInt()

        for (i in 0 until gridSize) {
            for (j in 0 until gridSize) {
                val cell = TextView(activity).apply {
                    val params = GridLayout.LayoutParams().apply {
                        rowSpec = GridLayout.spec(i)
                        columnSpec = GridLayout.spec(j)
                        width = cellSize
                        height = cellSize
                        setMargins(8, 8, 8, 8)
                    }
                    layoutParams = params
                    gravity = Gravity.CENTER
                    textSize = when (gridSize) {
                        3 -> 36f
                        4 -> 32f
                        5 -> 24f
                        else -> 20f // Pour le 6x6
                    }
                    setTypeface(null, Typeface.BOLD)
                    setBackgroundColor(activity.getColor(R.color.cell_empty))
                }
                gridCells[i][j] = cell
                gridLayout.addView(cell)
            }
        }
    }

    private fun setupTouchListeners() {
        val mainLayout = activity.findViewById<View>(android.R.id.content)
        mainLayout.setOnTouchListener(object : OnSwipeTouchListener(activity) {
            override fun onSwipeTop() { if(gameActive) move("UP") }
            override fun onSwipeBottom() { if(gameActive) move("DOWN") }
            override fun onSwipeLeft() { if(gameActive) move("LEFT") }
            override fun onSwipeRight() { if(gameActive) move("RIGHT") }
        })
    }

    fun start() {
        if (id == null) {
            val newId = db.matchDao().insertGame(currentMatch)
            currentMatch.id = newId.toInt()
            placeRandomInitialCells(2)
            currentMatch.gridState = getGridAsString()
            db.matchDao().updatePoints(currentMatch)
        } else {
            currentMatch = db.matchDao().selectGame(id)
            score = currentMatch.score
            loadGridFromString(currentMatch.gridState)
            updateUI()
        }
        setupTouchListeners()

        if (isChallengeMode) {
            val tvTimer = activity.findViewById<TextView>(R.id.tv_timer)
            tvTimer.visibility = View.VISIBLE
            startChallengeTimer(tvTimer)
        }
    }

    private fun startChallengeTimer(tvTimer: TextView) {
        challengeTimer?.cancel()
        gameActive = true
        challengeTimer = object : CountDownTimer(120000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                val minutes = secondsLeft / 60
                val seconds = secondsLeft % 60
                tvTimer.text = String.format("Temps : %02d:%02d", minutes, seconds)
            }
            override fun onFinish() {
                tvTimer.text = "Temps écoulé !"
                gameActive = false
                currentMatch.isRunning = false
                db.matchDao().updateGameEnd(currentMatch)
                Toast.makeText(activity, "DÉFI PERDU ! Temps écoulé.", Toast.LENGTH_LONG).show()
            }
        }.start()
    }

    private fun placeRandomInitialCells(count : Int) {
        val spawned = mutableListOf<Pair<Int, Int>>()
        for (a in 1..count) {
            val newCell = spawnRandomTile()
            if (newCell != null) spawned.add(newCell)
        }
        updateUI()

        // Animation d'apparition des premières tuiles
        if (prefs.getBoolean("animations", true)) {
            spawned.forEach { applySpawnAnimation(it) }
        }
    }

    private fun isOver() : Boolean {
        if (score >= 2048) return true

        for (i in 0 until gridSize) {
            for (j in 0 until gridSize) {
                if (gridMatrix[i][j] == 0) return false
            }
        }

        for (i in 0 until gridSize) {
            for (j in 0 until gridSize) {
                val current = gridMatrix[i][j]
                if (i < gridSize - 1 && current == gridMatrix[i + 1][j]) return false
                if (j < gridSize - 1 && current == gridMatrix[i][j + 1]) return false
            }
        }
        return true
    }

    private fun move(direction: String) {
        var hasMoved = false

        for (i in 0 until gridSize) {
            val line = mutableListOf<Int>()
            for (j in 0 until gridSize) {
                when (direction) {
                    "LEFT", "RIGHT" -> line.add(gridMatrix[i][j])
                    "UP", "DOWN" -> line.add(gridMatrix[j][i])
                }
            }

            if (direction == "RIGHT" || direction == "DOWN") line.reverse()
            val newLine = slideAndMerge(line)
            if (direction == "RIGHT" || direction == "DOWN") newLine.reverse()

            for (j in 0 until gridSize) {
                val oldValue = when (direction) {
                    "LEFT", "RIGHT" -> gridMatrix[i][j]
                    else -> gridMatrix[j][i]
                }
                val newValue = newLine[j]

                if (oldValue != newValue) hasMoved = true

                when (direction) {
                    "LEFT", "RIGHT" -> gridMatrix[i][j] = newValue
                    "UP", "DOWN" -> gridMatrix[j][i] = newValue
                }
            }
        }

        if (hasMoved) {
            val newCell = spawnRandomTile()
            if (prefs.getBoolean("sounds", true)) {
                android.media.MediaPlayer.create(activity, R.raw.sound_effect).start()
            }

            updateUI() // Met à jour les valeurs d'abord

            // --- GESTION DES ANIMATIONS ---
            if (prefs.getBoolean("animations", true)) {
                applySlideAnimation(direction)
                if (newCell != null) {
                    applySpawnAnimation(newCell)
                }
            }

            if (isChallengeMode && score >= 1000 && gameActive) {
                gameActive = false
                challengeTimer?.cancel()
                currentMatch.isRunning = false
                db.matchDao().updateGameEnd(currentMatch)
                Toast.makeText(activity, "🏆 DÉFI RÉUSSI ! Bravo !", Toast.LENGTH_LONG).show()
            }

        } else {
            if (isOver()) {
                gameActive = false
                challengeTimer?.cancel()
                currentMatch.isRunning = false
                currentMatch.matchEnd = System.currentTimeMillis()
                currentMatch.score = score
                db.matchDao().updateGameEnd(currentMatch)
            }
        }
    }

    // --- FONCTIONS D'ANIMATIONS ---
    private fun applySlideAnimation(direction: String) {
        val offset = 150f // Puissance du glissement
        val translationX = when (direction) {
            "LEFT" -> offset
            "RIGHT" -> -offset
            else -> 0f
        }
        val translationY = when (direction) {
            "UP" -> offset
            "DOWN" -> -offset
            else -> 0f
        }

        for (i in 0 until gridSize) {
            for (j in 0 until gridSize) {
                val view = gridCells[i][j] ?: continue
                if (gridMatrix[i][j] != 0) {
                    // On décale la tuile dans la direction opposée...
                    view.translationX = translationX
                    view.translationY = translationY
                    // ... et on l'anime vers sa position normale
                    view.animate()
                        .translationX(0f)
                        .translationY(0f)
                        .setDuration(150)
                        .start()
                }
            }
        }
    }

    private fun applySpawnAnimation(cell: Pair<Int, Int>) {
        val view = gridCells[cell.first][cell.second] ?: return
        view.scaleX = 0.2f
        view.scaleY = 0.2f
        view.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(250)
            .setInterpolator(OvershootInterpolator()) // Donne un effet "rebond"
            .start()
    }
    // ---------------------------------

    private fun slideAndMerge(line: List<Int>): MutableList<Int> {
        val nonZeros = line.filter { it != 0 }.toMutableList()
        val merged = mutableListOf<Int>()
        var i = 0

        while (i < nonZeros.size) {
            if (i + 1 < nonZeros.size && nonZeros[i] == nonZeros[i + 1]) {
                val sum = nonZeros[i] * 2
                merged.add(sum)
                score += sum
                i += 2
            } else {
                merged.add(nonZeros[i])
                i++
            }
        }

        while (merged.size < gridSize) {
            merged.add(0)
        }
        return merged
    }

    // Modifié pour retourner les coordonnées de la nouvelle tuile
    private fun spawnRandomTile(): Pair<Int, Int>? {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (i in 0 until gridSize) {
            for (j in 0 until gridSize) {
                if (gridMatrix[i][j] == 0) emptyCells.add(Pair(i, j))
            }
        }
        if (emptyCells.isNotEmpty()) {
            val randomCell = emptyCells.random()
            val value = if (Random.nextDouble() < 0.9) 2 else 4
            gridMatrix[randomCell.first][randomCell.second] = value
            return randomCell
        }
        return null
    }

    private fun updateUI() {
        tvScore.text = score.toString()

        for (i in 0 until gridSize) {
            for (j in 0 until gridSize) {
                val value = gridMatrix[i][j]
                val textView = gridCells[i][j]
                if (value == 0) {
                    textView?.text = ""
                } else {
                    textView?.text = value.toString()
                }
                textView?.setBackgroundColor(getTileColor(value))
                textView?.setTextColor(getTileTextColor(value))
            }
        }

        currentMatch.score = score
        currentMatch.gridState = getGridAsString()
        if (currentMatch.id != 0 && gameActive) {
            db.matchDao().updatePoints(currentMatch)
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

    private fun getTileTextColor(value: Int): Int {
        return if (value <= 4) activity.getColor(R.color.text_dark) else activity.getColor(R.color.text_light)
    }

    private fun getGridAsString(): String {
        return gridMatrix.joinToString(",") { row -> row.joinToString(",") }
    }

    private fun loadGridFromString(state: String) {
        if (state.isEmpty()) return
        val values = state.split(",").map { it.toInt() }
        var index = 0
        for (i in 0 until gridSize) {
            for (j in 0 until gridSize) {
                if (index < values.size) {
                    gridMatrix[i][j] = values[index++]
                }
            }
        }
    }

    fun restart() {
        challengeTimer?.cancel()
        currentMatch.isRunning = false
        currentMatch.matchEnd = System.currentTimeMillis()
        db.matchDao().updateGameEnd(currentMatch)
        score = 0
        gameActive = true
        for (i in 0 until gridSize) {
            for (j in 0 until gridSize) {
                gridMatrix[i][j] = 0
            }
        }
        currentMatch = Match(
            score = 0,
            matchStart = System.currentTimeMillis(),
            isRunning = true,
            gridState = getGridAsString()
        )
        val newId = db.matchDao().insertGame(currentMatch)
        currentMatch.id = newId.toInt()
        placeRandomInitialCells(2)
        currentMatch.gridState = getGridAsString()
        db.matchDao().updatePoints(currentMatch)

        if (isChallengeMode) {
            val tvTimer = activity.findViewById<TextView>(R.id.tv_timer)
            startChallengeTimer(tvTimer)
        }
    }
}