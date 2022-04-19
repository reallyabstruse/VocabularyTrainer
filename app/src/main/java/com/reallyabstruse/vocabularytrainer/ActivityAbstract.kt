package com.reallyabstruse.vocabularytrainer

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.*
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.*

inline fun <T> T.runIf(condition: Boolean, block: T.() -> T): T = if (condition) block() else this

abstract class ActivityAbstract : AppCompatActivity(), TextToSpeech.OnInitListener{
    private var speakString: String? = null
    private var speakerInitialized = false

    private lateinit var tts: TextToSpeech
    protected val languagesAvailable by lazy {
        tts.availableLanguages.map { n -> n.toString() }
    }

    private val spinnerDrawable by lazy {
        CircularProgressDrawable(this).apply {
            strokeWidth = 5f
            centerRadius = 30f
            start()
        }
    }

    protected fun doSpeak(str: String) {
        if (speakerInitialized) {
            tts.speak(str, TextToSpeech.QUEUE_FLUSH, null, "tts1")
        } else {
            speakString = str
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            intent.getStringExtra(LANG)?.let {
                tts.language = Locale(it)
            }

            speakerInitialized = true
            speakString?.let { doSpeak(it) }
        } else {
            showError(getString(R.string.failed_to_init_tts))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.shutdown()
    }

    protected fun showError(txt: String) {
        Snackbar.make(findViewById(android.R.id.content), txt, Snackbar.LENGTH_LONG).show()
    }

    protected fun showError(@StringRes resId: Int) {
        Snackbar.make(findViewById(android.R.id.content), resId, Snackbar.LENGTH_LONG).show()
    }

    protected fun getImageFile(dir: File, num: Int): File {
        return File(dir, "$num")
    }

    protected fun getImageUri(dir: File, num: Int): Uri {
        return Uri.fromFile(getImageFile(dir, num))
    }

    // returns [lang, name]
    protected fun parseDirectoryName(dir: String) : List<String>{
        return URLDecoder.decode(dir, "UTF-8").split('-', limit = 2)
    }

    protected fun getGameDirectory(lang: String, name: String) : File{
        return File(filesDir, URLEncoder.encode("$lang-$name", "UTF-8"))
    }

    fun loadImageToImageView(uri: Uri, @IdRes resId: Int, forceReload: Boolean = false) {
        loadImageToImageView(uri, findViewById(resId), forceReload)
    }

    fun loadImageToImageView(uri: Uri, imageView: ImageView, forceReload: Boolean = false) {
        Glide
            .with(this)
            .load(uri)
            .placeholder(spinnerDrawable)
            .error(ColorDrawable(Color.TRANSPARENT))
            .runIf(forceReload) {
                signature(ObjectKey(System.currentTimeMillis().toString()))
            }
            .into(imageView)
    }

    fun clearImageView(@IdRes resId: Int) {
        clearImageView(findViewById(resId))
    }

    fun clearImageView(imageView: ImageView) {
        Glide
            .with(this)
            .load(ColorDrawable(Color.TRANSPARENT))
            .into(imageView)
    }

    fun loadWordsJson(gameLang: String, gameTitle: String, callback: (words: MutableList<String>) -> Unit) {
        lifecycleScope.launch(Dispatchers.IO) {
            val file = File(getGameDirectory(gameLang, gameTitle), GAMEINFO)
            when {
                file.exists() -> {
                    try {
                        val typeToken = object : TypeToken<MutableList<String>>() {}.type
                        Gson().fromJson<MutableList<String>>(file.readText(), typeToken)
                    } catch(e: Exception) {
                        showError(e.toString())
                        mutableListOf()
                    }
                }
                else -> {
                    mutableListOf()
                }
            }.let {
                withContext(Dispatchers.Main) {
                    callback(it)
                }
            }
        }
    }

    inner class LanguagePicker(gameLang: String? = null): LinearLayout(this) {
        private val inputLanguage = Spinner(this@ActivityAbstract)
        private val installLanguageButton = Button(this@ActivityAbstract)

        init {
            inputLanguage.adapter = ArrayAdapter(this@ActivityAbstract, android.R.layout.simple_spinner_dropdown_item, languagesAvailable)
            gameLang?.let { lang ->
                languagesAvailable.indexOf(lang).let {
                    if (it >= 0) {
                        inputLanguage.setSelection(it)
                    }
                }
            }
            addView(inputLanguage)

            installLanguageButton.setText(R.string.install_more_languages)
            installLanguageButton.setOnClickListener {
                startActivity(Intent().apply {
                    action = "com.android.settings.TTS_SETTINGS"
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }
            addView(installLanguageButton)
        }

        fun getValue(): String {
            return inputLanguage.selectedItem.toString()
        }
    }
}

