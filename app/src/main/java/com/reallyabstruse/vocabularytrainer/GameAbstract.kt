package com.reallyabstruse.vocabularytrainer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

abstract class GameAbstract : ActivityAbstract() {
    lateinit var questionIndices: MutableList<Int>

    var curQuestion: Int = 0

    lateinit var gameWords: List<String>
    private lateinit var gameLang: String
    private lateinit var gameTitle: String

    var currentSolved = false
    var currentError = false
    private var errors = 0

    protected fun getImageUri(num: Int): Uri {
        if (num >= gameWords.size) {
            showError(R.string.trying_to_show_image_out_of_range)
        }

        return getImageUri(getGameDirectory(gameLang, gameTitle), num)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        gameLang = intent.getStringExtra(LANG)!!
        gameTitle = intent.getStringExtra(TITLE)!!

        loadWordsJson(gameLang, gameTitle, ::onWordsReady)

        findViewById<Button>(R.id.buttonNext).setOnClickListener {
            curQuestion++
            showNextQuestion()
        }

        findViewById<Button>(R.id.buttonPrev)?.setOnClickListener {
            curQuestion--
            showNextQuestion()
        }
    }

    open fun onWordsReady(words: MutableList<String>) {
        gameWords = words

        questionIndices = MutableList(gameWords.size) { it }
        questionIndices.shuffle()
    }

    // Returns 4 options as indices, or null if no more questions
    protected fun getNextQuestionOptions() : MutableList<Int>?{
        if (currentError) {
            errors++
        }

        if (curQuestion == questionIndices.size) {
            val intent = Intent(this, ShowScore::class.java).apply {
                putExtra(SCORE, gameWords.size - errors)
                putExtra(ERRORS, errors)
            }
            startActivity(intent)
            finish()
            return null
        }

        findViewById<Button>(R.id.buttonNext).visibility = View.INVISIBLE
        currentSolved = false
        currentError = false

        val options = MutableList(1) { questionIndices[curQuestion] }
        val countOptions: Int = if(questionIndices.size > 4) 4 else questionIndices.size

        for (i in 1 until countOptions) {
            var value: Int
            do {
                value = questionIndices.random()
            } while (options.contains(value))
            options.add(value)
        }

        options.shuffle()
        return options
    }

    protected abstract fun showNextQuestion()

    protected val chooseGameOption = View.OnClickListener {
        if (currentSolved)  {
            return@OnClickListener
        }
        if (it.id != R.id.correctOption) {
            currentError = true
            it.setBackgroundColor(ContextCompat.getColor(this, R.color.red))
        } else {
            currentSolved = true
            findViewById<Button>(R.id.buttonNext).visibility = View.VISIBLE
            it.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
        }
    }
}