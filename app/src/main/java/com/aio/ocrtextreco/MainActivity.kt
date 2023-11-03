package com.aio.ocrtextreco

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.SparseArray
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.aio.ocrtextreco.databinding.ActivityMainBinding
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import com.googlecode.tesseract.android.TessBaseAPI
import com.yalantis.ucrop.UCrop
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var textRecognizer: TextRecognizer
    lateinit var binding: ActivityMainBinding
    private val REQUEST_IMAGE_CAPTURE = 1
    private var imageBitmap: Bitmap? = null
    private var currentPhotoPath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.apply {
            etcaptImg.setOnClickListener {
                takeImage()
                etText.text=""
            }

            etdetect.setOnClickListener {
                processImage()
            }
        }

        textRecognizer = TextRecognizer.Builder(this).build()
        if (!textRecognizer.isOperational) {
            Toast.makeText(this, "Dependencies not loaded yet", Toast.LENGTH_SHORT).show()
            return
        }
    }

    private fun processImage() {
        if (imageBitmap != null) {
            val textRecognizer = TextRecognizer.Builder(this).build()
            if (!textRecognizer.isOperational) {
                Toast.makeText(this, "Dependencies not loaded yet", Toast.LENGTH_SHORT).show()
                return
            }

            val frame = Frame.Builder().setBitmap(imageBitmap).build()
            val items: SparseArray<TextBlock> = textRecognizer.detect(frame)

            val stringBuilder = StringBuilder()
            for (i in 0 until items.size()) {
                val item = items.valueAt(i)
                stringBuilder.append(item.value)
                stringBuilder.append("\n")
            }
            binding.etText.text = stringBuilder.toString()
        }
        else {
            Toast.makeText(this, "Please select image first", Toast.LENGTH_SHORT).show()
        }
    }

    private fun takeImage() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoFile = createImageFile()
        val photoURI = FileProvider.getUriForFile(this@MainActivity, "com.example.android.fileprovider", photoFile)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){

            val file = File(currentPhotoPath)
            val uri = Uri.fromFile(file)
            imageBitmap= MediaStore.Images.Media.getBitmap(contentResolver, uri)

            //start the uCrop
            val options = UCrop.Options()
            options.setCompressionFormat(Bitmap.CompressFormat.JPEG)
            options.setCompressionQuality(100)
            UCrop.of(uri, uri)
                .withOptions(options)
                .start(this@MainActivity)
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            val resultUri = UCrop.getOutput(data!!)
            imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, resultUri)
            binding.image1.setImageBitmap(imageBitmap)

            /*val datapath = getExternalFilesDir(null)?.absolutePath + "/tesseract/"
            val language = "eng"
            val baseApi = TessBaseAPI()
            baseApi.init(datapath, language)

            // Recognize text from the cropped image
            baseApi.setImage(imageBitmap)
            val recognizedText = baseApi.utF8Text
            binding.etText.text = recognizedText

            // Clean up
            baseApi.end()*/
        }
        }
    }


