package com.example.jeu_2048

import android.app.Activity
import android.view.View
import android.content.Context
import android.widget.TextView
import kotlin.random.Random

class Game(private val activity: Activity) {
    private var gridMatrix = Array(4) { IntArray(4) { 0 } }
    private val gridCells = Array(4) { arrayOfNulls<TextView>(4) }

    private val gameId = 0
    private val db = Database.getInstance(activity)
    public var score = 0
    private lateinit var tvScore: TextView

    init {
        tvScore = activity.findViewById(R.id.tv_score)
        for (i in 0..3) {
            for (j in 0..3) {
                val cellID = activity.resources.getIdentifier("cell_${i}_${j}", "id", activity.packageName)
                gridCells[i][j] = activity.findViewById(cellID)
            }
        }
    }

    fun start() {
        placeRandomInitialCells(2);
        db.insertGameEntry(this);


        val mainLayout = activity.findViewById<View>(android.R.id.content)
        mainLayout.setOnTouchListener(object : OnSwipeTouchListener(activity) {
            override fun onSwipeTop() { move("UP") }
            override fun onSwipeBottom() { move("DOWN") }
            override fun onSwipeLeft() { move("LEFT") }
            override fun onSwipeRight() { move("RIGHT") }
        })
    }

    private fun placeRandomInitialCells(count : Int) {
        for (a in 1..count) {
            val row = Random.nextInt(4)
            val column = Random.nextInt(4)
            val value = if (Random.nextDouble() < 0.9) 2 else 4;
            score += value;
            gridMatrix[row][column] = value;
        }
        updateUI()
    }

    private fun isOver() : Boolean {
        // all blocks full and cannot move in any direction
        for (i in 0..3) {
            for (j in 0..3) {
                if (gridMatrix[i][j] != 0) return false
            }
        }
        // move in any direction produces a change
        val allMoves = listOf("LEFT", "RIGHT", "UP", "DOWN");
        for (direction in allMoves) {
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
                    val newValue = newLine[j]
                    if (oldValue != newValue) return true;
                }
            }
        }
        return false;
    }

    private fun move(direction: String) {
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
                val newValue = newLine[j]

                if (oldValue != newValue) hasMoved = true

                when (direction) {
                    "LEFT", "RIGHT" -> gridMatrix[i][j] = newValue
                    "UP", "DOWN" -> gridMatrix[j][i] = newValue
                }
            }
        }

        if (hasMoved) {
            spawnRandomTile()
            updateUI()
        } else {
            if (isOver()) {
                db.updateGameStateEnd(this);
            }
        }
    }
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

        while (merged.size < 4) {
            merged.add(0)
        }
        return merged
    }
    private fun spawnRandomTile() {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (i in 0..3) {
            for (j in 0..3) {
                if (gridMatrix[i][j] == 0) emptyCells.add(Pair(i, j))
            }
        }
        if (emptyCells.isNotEmpty()) {
            val randomCell = emptyCells.random()
            val value = if (Random.nextDouble() < 0.9) 2 else 4
            score += value;
            gridMatrix[randomCell.first][randomCell.second] = value
        }
    }
    private fun updateUI() {
        tvScore.text = score.toString()

        for (i in 0..3) {
            for (j in 0..3) {
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
        db.updateCurrentPoints(this);
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
}