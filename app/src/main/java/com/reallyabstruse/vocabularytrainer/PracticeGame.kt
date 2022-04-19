package com.reallyabstruse.vocabularytrainer

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import com.reallyabstruse.vocabularytrainer.databinding.ActivityPixabayImageSearcherBinding
import com.reallyabstruse.vocabularytrainer.databinding.ActivityPracticeGameBinding

class PracticeGame : GameAbstract() {
    private lateinit var binding: ActivityPracticeGameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityPracticeGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)

        binding.buttonSpeak.setOnClickListener {
            doSpeak(gameWords[questionIndices[curQuestion]])
        }

        binding.buttonClose.setOnClickListener {
            finish()
        }
    }

    override fun onWordsReady(words: MutableList<String>) {
        super.onWordsReady(words)
        showNextQuestion()
    }

    override fun showNextQuestion() {
        loadImageToImageView(getImageUri(questionIndices[curQuestion]), binding.imagePractice)
        binding.buttonPrev.visibility = if (curQuestion > 0) View.VISIBLE else View.GONE
        binding.buttonNext.visibility = if (curQuestion < questionIndices.size - 1) View.VISIBLE else View.GONE
    }


}