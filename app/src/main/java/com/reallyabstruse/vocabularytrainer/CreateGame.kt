package com.reallyabstruse.vocabularytrainer

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowInsets
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.reallyabstruse.vocabularytrainer.databinding.ActivityCreateGameBinding
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.coroutines.*
import org.json.JSONArray
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URL
import java.net.URLEncoder
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class CreateGame : ActivityAbstract() {
    private lateinit var gameTitle: String
    private lateinit var gameLang: String
    private lateinit var dir: File

    private lateinit var binding: ActivityCreateGameBinding

    private var wordList = mutableListOf<String>()
    
    private var curWord :Int = 0
    private var hasSaved: Boolean = false

    companion object {
        fun getDir(gameLang: String, gameTitle: String) :String {
            return URLEncoder.encode("$gameLang-$gameTitle", "UTF-8")
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gameTitle = intent.getStringExtra(TITLE) ?: return
        gameLang = intent.getStringExtra(LANG) ?: return
        dir = File(filesDir, getDir(gameLang, gameTitle))
        dir.mkdirs()

        loadWordsJson(gameLang, gameTitle) {
            wordList = it
            curWord = wordList.size
            updateScreen()
            showLoadingOverlay(false)
        }

        binding.buttonBrowseImages.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }
            browseResult.launch(intent)
        }

        binding.buttonSearchPixabay.setOnClickListener {
            val word = getCurrentWord()
            if (word.isNotBlank()) {
                val intent = Intent(this, PixabayImageSearcher::class.java).apply {
                    putExtra(WORD, word)
                }
                browseResult.launch(intent)
            }
        }

        binding.buttonSave.setOnClickListener {
            if (saveGame(true)) {
                finish()
            }
        }

        binding.buttonDelete.setOnClickListener {
            deleteCurrentWord()
        }

        binding.buttonNextWord.setOnClickListener {
            if (getCurrentWord().isBlank()) {
                showError(R.string.enter_word_first)
                return@setOnClickListener
            }

            addCurrentWord()

            curWord++
            updateScreen()
        }

        binding.buttonPrevWord.setOnClickListener {
            addCurrentWord()

            curWord--
            updateScreen()
        }

        binding.editWord.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s:CharSequence, start:Int, before:Int, count:Int) {
                setNextButtonVisibility()
            }
            override fun beforeTextChanged(s:CharSequence, start:Int, count:Int,
                                           after:Int) {
            }
            override fun afterTextChanged(s: Editable) {
            }
        })

        binding.overlayLoading.setOnTouchListener { _, _ -> return@setOnTouchListener true }
    }

    override fun onStop() {
        super.onStop()
        if (!hasSaved) {
            saveGame()
        }
    }

    private fun getCurrentWord(): String {
        return binding.editWord.text.toString()
    }

    private fun setCurrentWord(str: String) {
        binding.editWord.setText(str)
    }

    private fun getCurrentImageUri(): Uri {
        return getImageUri(dir, curWord)
    }

    private fun showLoadingOverlay(show: Boolean) {
        binding.overlayLoading.visibility = if(show) View.VISIBLE else View.GONE
    }

    private fun setNextButtonVisibility() {
        binding.buttonNextWord.visibility = if(getCurrentWord().isNotBlank()) View.VISIBLE else View.GONE
    }

    private fun updateScreen(forceImageReload: Boolean = false) {
        if (wordList.size > curWord) {
            setCurrentWord(wordList[curWord])
            loadImageToImageView(getCurrentImageUri(), binding.imageView, forceImageReload)
        } else {
            setCurrentWord("")
            clearImageView(binding.imageView)
        }

        binding.buttonPrevWord.visibility= if (curWord > 0) View.VISIBLE else View.GONE
        setNextButtonVisibility()
    }

    private fun getDesiredDimension(): Int {
        return (0.5 * if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = this.windowManager.currentWindowMetrics
            val insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            windowMetrics.bounds.width() - insets.left - insets.right
        } else {
            val displayMetrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            this.windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.widthPixels
        }).toInt()
    }

    private fun addCurrentWord() {
        val word = binding.editWord.text.toString()
        if (word.isBlank()) {
            return
        }
        if (wordList.size == curWord) {
            wordList.add(word)
        } else {
            wordList[curWord] = word
        }
    }

    private fun moveLastWord(toIdx: Int) {
        val imageFile = getImageFile(dir, wordList.size - 1)
        if (imageFile.exists()) {
            if (!imageFile.renameTo(getImageFile(dir, toIdx))) {
                showError(R.string.could_not_reorder_question_images)
                return
            }
        }

        wordList[toIdx] = wordList.removeLast()
    }

    private fun deleteCurrentWord() {
        dialogBox(this, getString(R.string.are_you_sure_delete_word)) {
            getImageFile(dir, curWord).let {
                if (it.exists() && !it.delete()) {
                    showError(getString(R.string.could_not_delete_word_image))
                    return@dialogBox
                }
            }

            if (curWord < wordList.size - 1) {
                moveLastWord(curWord)
            } else if (curWord == wordList.size - 1) {
                wordList.removeLast()
            }
            updateScreen(true)
        }
    }

    private fun saveGame(fromButton: Boolean = false): Boolean {
        val jsonWords = JSONArray()

        addCurrentWord()

        for ((index, word) in wordList.withIndex()) {
            if (fromButton) {
                if (word.isBlank() || !getImageFile(dir, index).exists()) {
                    curWord = index
                    updateScreen()
                    showError(R.string.add_text_and_image_first)
                    return false
                }
            }

            jsonWords.put(word)
        }

        try {
            File(dir, GAMEINFO).writeText(jsonWords.toString())
            hasSaved = true
            return true
        } catch(e: Exception) {
            when(e) {
                is FileNotFoundException, is SecurityException -> {
                    showError(getString(R.string.could_not_save_game_info))
                }
                else -> throw e
            }
        }
        return false
    }

    private val browseResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {

            var uri: Uri = result.data?.data ?: return@registerForActivityResult

            lifecycleScope.launch(Dispatchers.Main) {
                var error: String? = null

                if (uri.scheme == "https" || uri.scheme == "http") {
                    showLoadingOverlay(true)

                    withContext(Dispatchers.IO) {
                        try {
                            URL(uri.toString()).openStream().use {
                                val cacheFile = File(cacheDir, "image")

                                Files.copy(
                                    it,
                                    cacheFile.toPath(),
                                    StandardCopyOption.REPLACE_EXISTING
                                )
                                uri = Uri.fromFile(cacheFile)
                            }
                        } catch(e: IOException) {
                            error = e.toString()
                        }
                    }

                    showLoadingOverlay(false)
                    error?.let {
                        showError(it)
                        return@launch
                    }
                }

                startCropper(uri)
            }
        }
    }

    private fun startCropper(uri: Uri) {
        val desiredDimension = getDesiredDimension()

        cropResult.launch(CropImage.activity(uri)
            .setAspectRatio(1, 1)
            .setRequestedSize(desiredDimension, desiredDimension, CropImageView.RequestSizeOptions.RESIZE_INSIDE)
            .setInitialCropWindowPaddingRatio(0F)
            .setOutputUri(getCurrentImageUri())
            .setAllowFlipping(false)
            .setAllowCounterRotation(true)
            .setAutoZoomEnabled(false)
            .setCropMenuCropButtonTitle(getString(R.string.ok))
            .getIntent(this))
    }

    private val cropResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            activityResult: ActivityResult ->
                val result = CropImage.getActivityResult(activityResult.data)
                if (activityResult.resultCode == RESULT_OK) {
                    if (wordList.size <= curWord) {
                        wordList.add(getCurrentWord())
                    }

                    updateScreen(true)
                } else if (activityResult.resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    showError(result.error.toString())
                }
    }

}