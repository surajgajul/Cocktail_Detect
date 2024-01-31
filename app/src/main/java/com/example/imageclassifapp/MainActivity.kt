package com.example.imageclassifapp

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.get
import com.example.imageclassifapp.databinding.ActivityMainBinding
import com.example.imageclassifapp.ml.Model

import com.example.imageclassifapp.model.cocktail

import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var cocktailList: ArrayList<cocktail>
    private val CAMERA_PERMISSION_CODE = 1
    private val CAMERA_REQ_CODE = 2
    private val imageSize = 224
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        cocktailList = arrayListOf()

        loadData()

        binding.takePictureBtn.setOnClickListener {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                val cameraIntent: Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, CAMERA_REQ_CODE)
            } else {
                requestPermissions(
                    arrayOf(android.Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_CODE
                )
            }
        }

        binding.selectPictureBtn.setOnClickListener {
            val galleryIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, 1)
        }
    }

    private fun loadData() {
        val bloody_mary = cocktail(
            "Bloody mary",
            120,
            10,
            2,
            0,
            850,
            460,
            "A Bloody Mary is a popular cocktail that typically contains vodka, tomato juice," +
                    " Worcestershire sauce, hot sauce, lemon juice, and celery salt. It is usually " +
                    "served over ice and garnished with a celery stalk or other vegetables."
        )
        val cosmopolitan = cocktail(
            "Bloody mary",
            120,
            10,
            2,
            0,
            850,
            460,
            "A Bloody Mary is a popular cocktail that typically contains vodka, tomato juice," +
                    " Worcestershire sauce, hot sauce, lemon juice, and celery salt. It is usually " +
                    "served over ice and garnished with a celery stalk or other vegetables."
        )

        val espresso_martini = cocktail(
            "Bloody mary",
            120,
            10,
            2,
            0,
            850,
            460,
            "A Bloody Mary is a popular cocktail that typically contains vodka, tomato juice," +
                    " Worcestershire sauce, hot sauce, lemon juice, and celery salt. It is usually " +
                    "served over ice and garnished with a celery stalk or other vegetables."
        )

        val margharita = cocktail(
            "Bloody mary",
            120,
            10,
            2,
            0,
            850,
            460,
            "A Bloody Mary is a popular cocktail that typically contains vodka, tomato juice," +
                    " Worcestershire sauce, hot sauce, lemon juice, and celery salt. It is usually " +
                    "served over ice and garnished with a celery stalk or other vegetables."
        )

        val mimosa = cocktail(
            "Bloody mary",
            120,
            10,
            2,
            0,
            850,
            460,
            "A Bloody Mary is a popular cocktail that typically contains vodka, tomato juice," +
                    " Worcestershire sauce, hot sauce, lemon juice, and celery salt. It is usually " +
                    "served over ice and garnished with a celery stalk or other vegetables."
        )

        val mojito = cocktail(
            "Bloody mary",
            120,
            10,
            2,
            0,
            850,
            460,
            "A Bloody Mary is a popular cocktail that typically contains vodka, tomato juice," +
                    " Worcestershire sauce, hot sauce, lemon juice, and celery salt. It is usually " +
                    "served over ice and garnished with a celery stalk or other vegetables."
        )
        val moscow_mule = cocktail(
            "Bloody mary",
            120,
            10,
            2,
            0,
            850,
            460,
            "A Bloody Mary is a popular cocktail that typically contains vodka, tomato juice," +
                    " Worcestershire sauce, hot sauce, lemon juice, and celery salt. It is usually " +
                    "served over ice and garnished with a celery stalk or other vegetables."
        )

        val old_fashtioned = cocktail(
            "Bloody mary",
            120,
            10,
            2,
            0,
            850,
            460,
            "A Bloody Mary is a popular cocktail that typically contains vodka, tomato juice," +
                    " Worcestershire sauce, hot sauce, lemon juice, and celery salt. It is usually " +
                    "served over ice and garnished with a celery stalk or other vegetables."
        )

        cocktailList = arrayListOf(bloody_mary,cosmopolitan,espresso_martini,margharita,mimosa,mojito,moscow_mule,old_fashtioned)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQ_CODE) {
                var image: Bitmap = data!!.extras!!.get("data") as Bitmap
                val dimension = Math.min(image.width, image.height)
                image = ThumbnailUtils.extractThumbnail(image, dimension, dimension)
                binding.image.setImageBitmap(image)

                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false)
                classifyImage(image)
            } else {
                val dat: Uri? = data!!.data
                var image: Bitmap? = null
                try {
                    image = MediaStore.Images.Media.getBitmap(this.contentResolver, dat)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                binding.image.setImageBitmap(image)
                image = Bitmap.createScaledBitmap(image!!, imageSize, imageSize, false)
                classifyImage(image)
            }
        }
    }


    private fun classifyImage(image: Bitmap?) {




        val model = Model.newInstance(applicationContext)
        val resizedImage = Bitmap.createScaledBitmap(image!!, 224, 224, true)

        // Create a TensorImage object from the resized image
        val inputImage = TensorImage(DataType.FLOAT32)
        inputImage.load(resizedImage)

        // Get the ByteBuffer from the TensorImage
        val byteBuffer: ByteBuffer = inputImage.buffer

        // Set the byte order
        byteBuffer.order(ByteOrder.nativeOrder())

        // Run inference
        val outputs = model.process(inputImage.tensorBuffer)

        // Process the outputs
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer
        val confidences = outputFeature0.floatArray

        // Find the index of the maximum confidence
        val maxPos = confidences.indices.maxByOrNull { confidences[it] } ?: -1

        // Log the results
        Log.d("Classified Result", "Class Index: $maxPos, Confidence: ${confidences[maxPos]}")
        fetchdata(maxPos)

        // Close the model resources when done
        model.close()

    }

    private fun fetchdata(pos: Int) {
        val detectedCocktail = cocktailList[pos]
        binding.nutritionFactsTopRow.visibility = View.VISIBLE
        binding.nutritionFactsBottonRow.visibility = View.VISIBLE
        binding.result.text = detectedCocktail.name
        binding.calVal.text = detectedCocktail.calorie.toString()
        binding.carbVal.text = detectedCocktail.carb.toString()
        binding.fatVal.text = detectedCocktail.fat.toString()
        binding.potasVal.text = detectedCocktail.potass.toString()
        binding.desc.text = detectedCocktail.descp
        binding.proVal.text = detectedCocktail.protein.toString()
        binding.sodVal.text = detectedCocktail.sodium.toString()
    }


}