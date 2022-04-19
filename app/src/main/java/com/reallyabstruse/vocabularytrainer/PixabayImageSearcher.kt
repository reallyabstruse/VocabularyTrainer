package com.reallyabstruse.vocabularytrainer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import org.json.JSONException
import org.json.JSONObject
import java.net.URL
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.reallyabstruse.vocabularytrainer.databinding.ActivityMainBinding
import com.reallyabstruse.vocabularytrainer.databinding.ActivityPixabayImageSearcherBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.URLEncoder
import java.util.*

const val CACHE_TIME: Long = 24*60*60*1000

class PixabayImageSearcher : ActivityAbstract() {
    private val apiKey: String = "26803716-d8e058b3026e07280c5612d5e"
    private val pixabayUrl: String = "https://pixabay.com"

    private lateinit var binding: ActivityPixabayImageSearcherBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityPixabayImageSearcherBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)

        binding.buttonPixabay.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(pixabayUrl)
            })
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val obj = doSearch(intent.getStringExtra(WORD)!!) ?: return@launch
            withContext(Dispatchers.Main) {
                val grid = GridMaker(this@PixabayImageSearcher, binding.searchResultTable, 2)

                try {
                    val hits = obj.getJSONArray("hits")
                    for (i in 0 until hits.length()) {
                        val hit = hits.getJSONObject(i)

                        val previewUri = Uri.parse(hit.getString("previewURL"))

                        val imageButton = ImageView(this@PixabayImageSearcher)
                        imageButton.scaleType = ImageView.ScaleType.FIT_XY
                        imageButton.adjustViewBounds = true
                        loadImageToImageView(previewUri, imageButton)

                        imageButton.setOnClickListener {
                            setResult(RESULT_OK, Intent().apply {
                                this.data = Uri.parse(hit.getString("largeImageURL"))
                            })
                            finish()
                        }

                        grid.addItem(imageButton)
                    }
                } catch (e: JSONException) {
                    showError(e.toString())
                }
                grid.finalize()
            }
        }
    }

    private fun getCacheFile(q: String): File {
        return File(cacheDir, URLEncoder.encode(q, "UTF-8"))
    }

    private fun isTooOld(f: File): Boolean {
        return Date().time - f.lastModified() > CACHE_TIME
    }

    private fun doSearch(q: String): JSONObject? {
        try {
            val cacheFile = getCacheFile(q)

            if (cacheFile.exists()) {
                if (!isTooOld(cacheFile)) {
                    return JSONObject(cacheFile.readText())
                }
                cacheFile.delete()
            }

            return JSONObject(URL("$pixabayUrl/api/?key=$apiKey&q=$q").readText().also {
                cacheFile.writeText(it)
            })
        } catch(e: IOException) {
            showError(e.toString())
        }

        return null
    }
}