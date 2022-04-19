package com.reallyabstruse.vocabularytrainer

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.reallyabstruse.vocabularytrainer.databinding.ActivityGameScreenFindWordBinding
import com.reallyabstruse.vocabularytrainer.databinding.ActivityMainBinding

const val SCORE = "com.reallyabstruse.vocabularytrainer.score"
const val ERRORS = "com.reallyabstruse.vocabularytrainer.errors"
const val TITLE = "com.reallyabstruse.vocabularytrainer.title"
const val LANG = "com.reallyabstruse.vocabularytrainer.lang"
const val WORD = "com.reallyabstruse.vocabularytrainer.word"

const val GAMEINFO = "gameinfo"

class MainActivity : ActivityAbstract() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()

        val grid = GridMaker(this@MainActivity, binding.gamesLayout, 2)
        filesDir.listFiles()?.forEach {
            val dirName = it.path.substring(it.path.lastIndexOf('/') + 1)
            val (lang, title) = parseDirectoryName(dirName)

            val layout = LinearLayout(this@MainActivity)
            layout.orientation = LinearLayout.VERTICAL
            layout.gravity = Gravity.CENTER

            val image = ImageButton(this@MainActivity)
            image.scaleType = ImageView.ScaleType.FIT_XY
            image.adjustViewBounds = true
            loadImageToImageView(getImageUri(it, 0), image)

            val textView = TextView(this@MainActivity)
            textView.setText(title)
            textView.gravity = Gravity.CENTER

            layout.addView(textView)
            layout.addView(image)

            image.setOnClickListener {
                val intent = Intent(this, ChooseGameType::class.java).apply {
                    putExtra(TITLE, title)
                    putExtra(LANG, lang)
                }
                startActivity(intent)
            }

            grid.addItem(layout)
        }
        grid.finalize()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_action_bar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        when (item.itemId) {
            R.id.action_create_game -> {
                val inputTitle = EditText(this)
                inputTitle.inputType = InputType.TYPE_CLASS_TEXT
                inputTitle.hint = getString(R.string.game_name)

                dialogBox(this,getString(R.string.create_new_game), inputTitle, LanguagePicker()) {
                    val intent = Intent(this, CreateGame::class.java).apply {
                        putExtra(TITLE, it[0])
                        putExtra(LANG, it[1])
                    }
                    startActivity(intent)
                }
            }
        }

        return true
    }
}