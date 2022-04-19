package com.reallyabstruse.vocabularytrainer

import android.os.Bundle
import android.widget.*
import com.reallyabstruse.vocabularytrainer.databinding.ActivityCreateGameBinding
import com.reallyabstruse.vocabularytrainer.databinding.ActivityGameScreenFindPicBinding

class GameScreenFindPic : GameAbstract() {
    private lateinit var binding: ActivityGameScreenFindPicBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityGameScreenFindPicBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)

        binding.buttonSpeak.setOnClickListener {
            doSpeak(gameWords[questionIndices[curQuestion]])
        }
    }

    override fun onWordsReady(words: MutableList<String>) {
        super.onWordsReady(words)
        showNextQuestion()
    }

    override fun showNextQuestion() {
        val options = getNextQuestionOptions() ?: return

        val grid = GridMaker(this, binding.answerTable, 2)

        val solutionNum = questionIndices[curQuestion]

        for (option in options) {
            val image = ImageButton(this)
            image.scaleType = ImageView.ScaleType.FIT_XY
            image.adjustViewBounds = true
            loadImageToImageView(getImageUri(option), image)
            if (option == solutionNum) {
                image.id = R.id.correctOption
            }
            image.setOnClickListener(chooseGameOption)
            grid.addItem(image)
        }
        grid.finalize()

        binding.titleText.text = gameWords[solutionNum]

        doSpeak(gameWords[solutionNum])
    }
}