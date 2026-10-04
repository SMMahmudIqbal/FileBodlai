package com.example.fileconverter

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fileconverter.ui.screens.*
import com.example.fileconverter.ui.theme.FileConverterTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        com.example.fileconverter.util.SensoryManager.init(this)
        com.example.fileconverter.util.ImpactStatsManager.init(this)

        val incomingRoute = resolveIncomingIntent(intent)

        setContent {
            FileConverterTheme {
                AppNavHost(initialDestination = incomingRoute ?: "home")
            }
        }
    }

    private fun resolveIncomingIntent(intent: Intent?): String? {
        if (intent == null) return null
        val action = intent.action
        val type = intent.type ?: ""

        if (Intent.ACTION_SEND == action || Intent.ACTION_VIEW == action) {
            return when {
                type.contains("pdf", ignoreCase = true) -> "organize_pdf"
                type.contains("presentation", ignoreCase = true) || type.contains("powerpoint", ignoreCase = true) -> "pptx_to_pdf"
                type.contains("word", ignoreCase = true) -> "docx_to_pdf"
                type.contains("image", ignoreCase = true) -> "batch_compress"
                type.contains("text", ignoreCase = true) -> "text_to_pdf"
                else -> null
            }
        } else if (Intent.ACTION_SEND_MULTIPLE == action) {
            return when {
                type.contains("pdf", ignoreCase = true) -> "merge_pdf"
                type.contains("image", ignoreCase = true) -> "images_to_pdf"
                else -> null
            }
        }
        return null
    }
}

@Composable
fun AppNavHost(initialDestination: String = "home") {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = initialDestination) {
        composable("home") {
            HomeScreen(onNavigate = { route -> navController.navigate(route) })
        }
        composable("pdf_to_pptx") {
            PdfToPptxScreen(onBack = { navController.popBackStack() })
        }
        composable("pptx_to_pdf") {
            PptxToPdfScreen(onBack = { navController.popBackStack() })
        }
        composable("docx_to_pdf") {
            DocxToPdfScreen(onBack = { navController.popBackStack() })
        }
        composable("docx_to_pptx") {
            DocxToPptxScreen(onBack = { navController.popBackStack() })
        }
        composable("merge_pdf") {
            MergePdfScreen(onBack = { navController.popBackStack() })
        }
        composable("organize_pdf") {
            PdfOrganizeScreen(onBack = { navController.popBackStack() })
        }
        composable("images_to_pdf") {
            ImageToPdfScreen(onBack = { navController.popBackStack() })
        }
        composable("pdf_to_images") {
            PdfToImagesScreen(onBack = { navController.popBackStack() })
        }
        composable("batch_compress") {
            BatchCompressScreen(onBack = { navController.popBackStack() })
        }
        composable("image_to_image") {
            ImageToImageScreen(onBack = { navController.popBackStack() })
        }
        composable("text_to_pdf") {
            TextToPdfScreen(onBack = { navController.popBackStack() })
        }
        composable("ocr_extract") {
            OcrExtractScreen(onBack = { navController.popBackStack() })
        }
        composable("compress_pdf") {
            CompressPdfScreen(
                onBack = { navController.popBackStack() },
                onViewPdf = { uri ->
                    navController.navigate("view_pdf?uri=" + Uri.encode(uri.toString()))
                }
            )
        }
        composable("quick_look?uri={uri}&mime={mime}") { backStackEntry ->
            val uriStr = backStackEntry.arguments?.getString("uri") ?: ""
            val mime = backStackEntry.arguments?.getString("mime") ?: "application/pdf"
            QuickLookScreen(
                uri = Uri.parse(uriStr),
                mimeType = mime,
                onBack = { navController.popBackStack() }
            )
        }
        composable("view_pdf?uri={uri}") { backStackEntry ->
            val uriStr = backStackEntry.arguments?.getString("uri") ?: ""
            QuickLookScreen(
                uri = Uri.parse(uriStr),
                mimeType = "application/pdf",
                onBack = { navController.popBackStack() }
            )
        }
    }
}
