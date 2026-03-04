package com.example.jeu_2048

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import kotlin.random.Random

class MainActivity : Activity() {
    private var gridMatrix = Array(4) { IntArray(4) { 0 } }
    private val gridCells = Array(4) { arrayOfNulls<TextView>(4) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        for (i in 0..3) {
            for (j in 0..3) {
                val cellID = resources.getIdentifier("cell_${i}_${j}", "id", packageName)
                gridCells[i][j] = findViewById(cellID)
            }
        }
        spawnRandomTile()
        spawnRandomTile()
        updateUI()
    }
    private fun spawnRandomTile() {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (i in 0..3) {
            for (j in 0..3) {
                if (gridMatrix[i][j] == 0) {
                    emptyCells.add(Pair(i, j))
                }
            }
        }
        if (emptyCells.isNotEmpty()) {
            val randomCell = emptyCells.random()
            val value = if (Random.nextDouble() < 0.9) 2 else 4
            gridMatrix[randomCell.first][randomCell.second] = value
        }
    }
    private fun updateUI() {
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
            else -> getColor(R.color.tile_2048) // sup a 2048
        }
    }

    private fun getTileTextColor(value: Int): Int {
        return if (value <= 4) {
            getColor(R.color.text_dark)
        } else {
            getColor(R.color.text_light)
        }
    }
}