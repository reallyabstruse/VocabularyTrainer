package com.reallyabstruse.vocabularytrainer

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import com.reallyabstruse.vocabularytrainer.databinding.ActivityChooseGameTypeBinding
import com.reallyabstruse.vocabularytrainer.databinding.ActivityCreateGameBinding
import java.io.FileNotFoundException


class ChooseGameType : ActivityAbstract() {

    private lateinit var gameLang: String
    private lateinit var gameTitle: String

    private lateinit var binding: ActivityChooseGameTypeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChooseGameTypeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gameLang = intent.getStringExtra(LANG)!!
        gameTitle = intent.getStringExtra(TITLE)!!

       binding.buttonFindWord.setOnClickListener {
            val intent = Intent(this, GameScreenFindWord::class.java).apply {
                putExtra(LANG, gameLang)
                putExtra(TITLE, gameTitle)
            }
            startActivity(intent)
        }

        binding.buttonFindImage.setOnClickListener {
            val intent = Intent(this, GameScreenFindPic::class.java).apply {
                putExtra(LANG, gameLang)
                putExtra(TITLE, gameTitle)
            }
            startActivity(intent)
        }

        binding.buttonPractice.setOnClickListener {
            val intent = Intent(this, PracticeGame::class.java).apply {
                putExtra(LANG, gameLang)
                putExtra(TITLE, gameTitle)
            }
            startActivity(intent)
        }

        binding.buttonClose.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        updateScreen()
    }

    private fun updateScreen() {
        actionBar?.title = gameTitle
        supportActionBar?.title = gameTitle

        loadImageToImageView(getImageUri(getGameDirectory(gameLang, gameTitle), 0), binding.imageGame)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.choose_game_action_bar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        when (item.itemId) {
            R.id.action_delete -> {
                dialogBox(this, getString(R.string.are_you_sure_delete_game)) {
                    if (getGameDirectory(gameLang, gameTitle).deleteRecursively()) {
                        finish()
                    } else {
                        showError(getString(R.string.failed_to_delete_game))
                    }
                }
            }

            R.id.action_edit_lang -> {
                dialogBox(this, getString(R.string.rename_game), LanguagePicker(gameLang)) {
                    if (getGameDirectory(gameLang, gameTitle).renameTo(getGameDirectory(it[0], gameTitle))) {
                        gameLang = it[0]
                        updateScreen()
                    } else {
                        showError(getString(R.string.could_not_change_game_lang))
                    }
                }
            }

            R.id.action_edit_name -> {
                val inputTitle = EditText(this)
                inputTitle.inputType = InputType.TYPE_CLASS_TEXT
                inputTitle.hint = getString(R.string.game_name)
                inputTitle.setText(gameTitle)

                dialogBox(this, getString(R.string.rename_game), inputTitle) {
                    if (getGameDirectory(gameLang, gameTitle).renameTo(getGameDirectory(gameLang, it[0]))) {
                        gameTitle = it[0]
                        updateScreen()
                    } else {
                        showError(getString(R.string.could_not_rename_game))
                    }
                }
            }

            R.id.action_edit_words -> {
                val intent = Intent(this, CreateGame::class.java).apply {
                    putExtra(TITLE, gameTitle)
                    putExtra(LANG, gameLang)
                }
                startActivity(intent)
            }
        }

        return true
    }
}