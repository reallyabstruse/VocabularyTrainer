package com.reallyabstruse.vocabularytrainer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.reallyabstruse.vocabularytrainer.databinding.ActivityCreateGameBinding
import com.reallyabstruse.vocabularytrainer.databinding.ActivityShowScoreBinding

class ShowScore : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityShowScoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val score = intent.getIntExtra(SCORE, 0)
        val errors = intent.getIntExtra(ERRORS, 0)

        binding.textScore.setText("\uD83D\uDC4D $score")
        binding.textErrors.setText("\uD83D\uDC4E $errors")

        binding.textRating.setText(getRating(score, errors))

        binding.buttonFinish.setOnClickListener {
            finish()
        }
    }

    private fun getRating(score: Int, errors: Int): String{
        val ratings = arrayOf("😭", "🥺", "\uD83D\uDE10", "\uD83D\uDE42", "\uD83D\uDE01", "\uD83E\uDD29\uD83E\uDD73")
        val fraction = score.toFloat() / (score + errors)

        return ratings[(fraction * (ratings.size-1)).toInt()]
    }
}