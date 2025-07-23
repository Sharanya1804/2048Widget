package com.example.awidget2048

import android.content.Context
import android.content.SharedPreferences
import kotlin.random.Random

enum class Direction { UP, DOWN, LEFT, RIGHT }

class GameLogic(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("widget_2048_prefs", Context.MODE_PRIVATE)
    private var grid: Array<IntArray> = Array(4) { IntArray(4) }
    private var score: Int = 0
    private var gameOver: Boolean = false

    init {
        loadGame()
    }

    fun getGrid(): Array<IntArray> = grid
    fun getScore(): Int = score
    fun isGameOver(): Boolean = gameOver

    fun resetGame() {
        grid = Array(4) { IntArray(4) }
        score = 0
        gameOver = false
        addNewTile()
        addNewTile()
        saveGame()
    }

    fun move(direction: Direction): Boolean {
        if (gameOver) return false

        val previousGrid = grid.map { it.clone() }.toTypedArray()
        val previousScore = score

        when (direction) {
            Direction.UP -> moveUp()
            Direction.DOWN -> moveDown()
            Direction.LEFT -> moveLeft()
            Direction.RIGHT -> moveRight()
        }

        // Check if move was valid
        val moved = !arraysEqual(previousGrid, grid) || score != previousScore

        if (moved) {
            addNewTile()
            checkGameOver()
            saveGame()
        }

        return moved
    }

    private fun moveLeft() {
        for (i in 0..3) {
            val row = grid[i]
            val newRow = slideAndMerge(row)
            grid[i] = newRow
        }
    }

    private fun moveRight() {
        for (i in 0..3) {
            val row = grid[i].reversedArray()
            val newRow = slideAndMerge(row).reversedArray()
            grid[i] = newRow
        }
    }

    private fun moveUp() {
        for (j in 0..3) {
            val column = IntArray(4) { grid[it][j] }
            val newColumn = slideAndMerge(column)
            for (i in 0..3) {
                grid[i][j] = newColumn[i]
            }
        }
    }

    private fun moveDown() {
        for (j in 0..3) {
            val column = IntArray(4) { grid[3-it][j] }
            val newColumn = slideAndMerge(column)
            for (i in 0..3) {
                grid[3-i][j] = newColumn[i]
            }
        }
    }

    private fun slideAndMerge(line: IntArray): IntArray {
        val newLine = IntArray(4)
        val nonZero = line.filter { it != 0 }.toMutableList()

        var pos = 0
        var i = 0

        while (i < nonZero.size) {
            if (i + 1 < nonZero.size && nonZero[i] == nonZero[i + 1]) {
                // Merge tiles
                newLine[pos] = nonZero[i] * 2
                score += newLine[pos]
                i += 2
            } else {
                newLine[pos] = nonZero[i]
                i++
            }
            pos++
        }

        return newLine
    }

    private fun addNewTile() {
        val emptyCells = mutableListOf<Pair<Int, Int>>()

        for (i in 0..3) {
            for (j in 0..3) {
                if (grid[i][j] == 0) {
                    emptyCells.add(Pair(i, j))
                }
            }
        }

        if (emptyCells.isNotEmpty()) {
            val randomCell = emptyCells[Random.nextInt(emptyCells.size)]
            grid[randomCell.first][randomCell.second] = if (Random.nextFloat() < 0.9f) 2 else 4
        }
    }

    private fun checkGameOver() {
        // Check for empty cells
        for (i in 0..3) {
            for (j in 0..3) {
                if (grid[i][j] == 0) {
                    gameOver = false
                    return
                }
            }
        }

        // Check for possible merges
        for (i in 0..3) {
            for (j in 0..3) {
                val current = grid[i][j]
                if ((i > 0 && grid[i-1][j] == current) ||
                    (i < 3 && grid[i+1][j] == current) ||
                    (j > 0 && grid[i][j-1] == current) ||
                    (j < 3 && grid[i][j+1] == current)) {
                    gameOver = false
                    return
                }
            }
        }

        gameOver = true
    }

    private fun saveGame() {
        val editor = prefs.edit()

        // Save grid
        for (i in 0..3) {
            for (j in 0..3) {
                editor.putInt("grid_${i}_${j}", grid[i][j])
            }
        }

        editor.putInt("score", score)
        editor.putBoolean("game_over", gameOver)
        editor.apply()
    }

    private fun loadGame() {
        var hasData = false

        // Load grid
        for (i in 0..3) {
            for (j in 0..3) {
                val value = prefs.getInt("grid_${i}_${j}", 0)
                grid[i][j] = value
                if (value != 0) hasData = true
            }
        }

        score = prefs.getInt("score", 0)
        gameOver = prefs.getBoolean("game_over", false)

        // If no saved data, initialize new game
        if (!hasData) {
            resetGame()
        }
    }

    private fun arraysEqual(arr1: Array<IntArray>, arr2: Array<IntArray>): Boolean {
        for (i in arr1.indices) {
            for (j in arr1[i].indices) {
                if (arr1[i][j] != arr2[i][j]) return false
            }
        }
        return true
    }
}
