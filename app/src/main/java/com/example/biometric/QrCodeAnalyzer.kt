package com.example.biometric

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class QrCodeAnalyzer(
    private val onQrCodeScanned: (String) -> Unit
): ImageAnalysis.Analyzer {

    private val options = BarcodeScannerOptions
        .Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()

    private val scanner = BarcodeScanning.getClient(options)
     @Volatile private var isScanning = false


    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override fun analyze(image: ImageProxy) {
        if (isScanning){
            image.close()
            return
        }
        val mediaImage = image.image
        if (mediaImage !== null) {
            val inputImage = InputImage.fromMediaImage(
                mediaImage,
                image.imageInfo.rotationDegrees
            )
            isScanning = true
            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()){
                        val rawValue = barcodes.first().rawValue
                        rawValue?.let { onQrCodeScanned(it) }
                    }
                    image.close()
                    isScanning = false


                }
                .addOnFailureListener { e->
                    Log.e("QrCodeAnalyzer", "Scan failed: ${e.message}")
                    image.close()
                    isScanning = false
                }

        }else{
            image.close()
        }



    }
}