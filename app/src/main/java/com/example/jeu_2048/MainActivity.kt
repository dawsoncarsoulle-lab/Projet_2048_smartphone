package com.example.jeu_2048

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import kotlin.random.Random

class MainActivity : Activity() {

    private var gridMatrix = Array(4) { IntArray(4) { 0 } }
    private val gridCells = Array(4) { arrayOfNulls<TextView>(4) }

    private var score = 0
    private lateinit var tvScore: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvScore = findViewById(R.id.tv_score)

        for (i in 0..3) {
            for (j in 0..3) {
                val cellID = resources.getIdentifier("cell_${i}_${j}", "id", packageName)
                gridCells[i][j] = findViewById(cellID)
            }
        }

        spawnRandomTile()
        spawnRandomTile()
        updateUI()
        val mainLayout = findViewById<View>(android.R.id.content)
        mainLayout.setOnTouchListener(object : OnSwipeTouchListener(this@MainActivity) {
            override fun onSwipeTop() { move("UP") }
            override fun onSwipeBottom() { move("DOWN") }
            override fun onSwipeLeft() { move("LEFT") }
            override fun onSwipeRight() { move("RIGHT") }
        })
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
    }

    private fun getTileColor(value: Int): Int {
        return when (value) {
            0 -> getColor(R.color.cell_empty)
            2 -> getColor(R.color.tile_2)
            4 -> getColor(R.color.tile_4)
            8 -> getColor(R.color.tile_8)
            16 -> getColor(R.color.tile_16)
            32 -> getColor(R.color.tile_32)
            64 -> getColor(R.color.tile_64)
            128 -> getColor(R.color.tile_128)
            256 -> getColor(R.color.tile_256)
            512 -> getColor(R.color.tile_512)
            1024 -> getColor(R.color.tile_1024)
            2048 -> getColor(R.color.tile_2048)
            else -> getColor(R.color.tile_2048)
        }
    }

    private fun getTileTextColor(value: Int): Int {
        return if (value <= 4) getColor(R.color.text_dark) else getColor(R.color.text_light)
    }
}