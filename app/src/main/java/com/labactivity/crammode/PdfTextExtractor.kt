package com.labactivity.crammode

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.CountDownLatch

object PdfTextExtractor {

    fun extractText(context: Context, uri: Uri): String {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val textResults = mutableListOf<String>()

        // Copy PDF to cache
        val file = File(context.cacheDir, "temp.pdf")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }

        val parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(parcelFileDescriptor)

        for (i in 0 until renderer.pageCount) {
            val page = renderer.openPage(i)

            val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()

            val image = InputImage.fromBitmap(bitmap, 0)

            // Run ML Kit OCR synchronously using CountDownLatch
            var pageText = ""
            val latch = CountDownLatch(1)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    pageText = visionText.text
                    latch.countDown()
                }
                .addOnFailureListener {
                    latch.countDown()
                }
            latch.await()
            textResults.add(pageText)
        }

        renderer.close()
        parcelFileDescriptor.close()
        return textResults.joinToString("\n\n")
    }
}
