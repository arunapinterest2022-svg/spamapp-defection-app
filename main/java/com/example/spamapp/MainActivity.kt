package com.example.spamapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class MainActivity : AppCompatActivity() {

    private lateinit var tflite: Interpreter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val inputText = findViewById<EditText>(R.id.inputText)
        val button = findViewById<Button>(R.id.checkButton)
        val resultText = findViewById<TextView>(R.id.resultText)

        // load model
        tflite = Interpreter(loadModelFile())

        button.setOnClickListener {
            val text = inputText.text.toString()

            val input = preprocess(text)
            val output = Array(1) { FloatArray(2) }

            tflite.run(input, output)

            val spamScore = output[0][1]

            resultText.text = if (spamScore > 0.5)
                "🚨 SPAM"
            else
                "✅ NOT SPAM"
        }
    }

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = assets.openFd("spam_model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val channel = inputStream.channel

        return channel.map(
            FileChannel.MapMode.READ_ONLY,
            fileDescriptor.startOffset,
            fileDescriptor.declaredLength
        )
    }

    private fun preprocess(text: String): Array<FloatArray> {
        val arr = FloatArray(100)

        for (i in text.indices) {
            if (i >= 100) break
            arr[i] = text[i].code.toFloat()
        }

        return arrayOf(arr)
    }
}