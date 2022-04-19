package com.reallyabstruse.vocabularytrainer

import android.os.Bundle
import android.util.TypedValue
import android.widget.*
import com.reallyabstruse.vocabularytrainer.databinding.ActivityGameScreenFindPicBinding
import com.reallyabstruse.vocabularytrainer.databinding.ActivityGameScreenFindWordBinding

class GameScreenFindWord : GameAbstract() {
    private lateinit var binding: ActivityGameScreenFindWordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityGameScreenFindWordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
    }

    override fun onWordsReady(words: MutableList<String>) {
        super.onWordsReady(words)
        showNextQuestion()
    }

    override fun showNextQuestion() {
        val options = getNextQuestionOptions() ?: return

        val rowHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60f, resources.displayMetrics)
        val grid = GridMaker(this, binding.answerTable, 2, rowHeight = rowHeight.toInt())

        val solutionNum = questionIndices[curQuestion]

        for (option in options) {
            val speakButton = Button(this)
            speakButton.text = getString(R.string.speak)
            speakButton.textSize = 20f
            speakButton.setOnClickListener {
                doSpeak(gameWords[option])
            }
            val wordButton = Button(this)
            wordButton.text = gameWords[option]
            wordButton.textSize = 20f
            if (solutionNum == option) {
                wordButton.id = R.id.correctOption
            }
            wordButton.setOnClickListener(chooseGameOption)

            grid.addItem(wordButton)
            grid.addItem(speakButton, stretch = false)
        }
        grid.finalize()

        loadImageToImageView(getImageUri(solutionNum), binding.imageFindWord)
    }
}