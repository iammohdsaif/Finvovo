package com.example.finvovo.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.finvovo.data.model.Transaction
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportUtils {

    fun exportToPdf(context: Context, transactions: List<Transaction>): Uri? {
        val fileName = "Transaction_Report_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
        return export(context, fileName, "application/pdf") { outputStream ->
            exportToPdf(outputStream, transactions)
        }
    }

    fun exportToPdf(outputStream: OutputStream, transactions: List<Transaction>): Boolean {
        return exportToPdfInternal(outputStream, transactions)
    }

    fun exportToExcel(context: Context, transactions: List<Transaction>): Uri? {
        val fileName = "Transaction_Report_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.xlsx"
        return export(context, fileName, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") { outputStream ->
            exportToExcel(outputStream, transactions)
        }
    }

    fun exportToExcel(outputStream: OutputStream, transactions: List<Transaction>): Boolean {
        return exportToExcelInternal(outputStream, transactions)
    }

    private fun export(context: Context, fileName: String, mimeType: String, action: (OutputStream) -> Boolean): Uri? {
        var uri: Uri? = null
        try {
            val outputStream: OutputStream?
            val resolver = context.contentResolver
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                outputStream = uri?.let { resolver.openOutputStream(it) }
            } else {
                // Use app-specific external storage which doesn't require write permissions
                // and is still accessible by FileProvider
                val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                if (downloadsDir != null && !downloadsDir.exists()) downloadsDir.mkdirs()
                
                val target = File(downloadsDir ?: context.cacheDir, fileName)
                outputStream = FileOutputStream(target)
                // Use FileProvider to get Uri (FileProvider should be used for sharing, but here we return File Uri)
                // Wait, the caller (AskAIScreen) uses this URI to SHARE.
                // Uri.fromFile() is 'file://' which causes FileUriExposedException on Android 7+.
                // We MUST use FileProvider.getUriForFile() here if we want to share it.
                // However, the existing code returned Uri.fromFile(target).
                // AskAIViewModel receives this Uri.
                
                // Let's check AskAIViewModel -> AskAIScreen.
                // AskAIScreen: Loop back to event.uri.
                // If event.uri is file://, Intent.ACTION_SEND will crash on N+ if clipData is not set correctly?
                // actually FileUriExposedException happens immediately when putting intent.
                
                // We should return a FileProvider URI here for < Q as well, OR return file path and let Caller handle it.
                // But exportToPdf returns Uri.
                
                // Let's fix this properly.
                uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    target
                )
            }

            return if (outputStream != null) {
                val success = action(outputStream)
                outputStream.close()
                if (success) uri else null
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun exportToPdfInternal(outputStream: OutputStream, transactions: List<Transaction>): Boolean {
        try {
            val document = Document()
            PdfWriter.getInstance(document, outputStream)
            document.open()

            // Title
            val titleFont = Font(Font.FontFamily.HELVETICA, 18f, Font.BOLD)
            val title = Paragraph("Transaction Report", titleFont)
            title.alignment = Element.ALIGN_CENTER
            title.spacingAfter = 20f
            document.add(title)

            // Table
            val table = PdfPTable(5) // Date, Description, Type, Category, Amount
            table.widthPercentage = 100f
            table.setWidths(floatArrayOf(3f, 6f, 2f, 2f, 3f))

            // Headers
            val headers = listOf("Date", "Description", "Type", "Category", "Amount")
            val headerFont = Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD, BaseColor.WHITE)
            
            for (header in headers) {
                val cell = PdfPCell(Phrase(header, headerFont))
                cell.backgroundColor = BaseColor.GRAY
                cell.horizontalAlignment = Element.ALIGN_CENTER
                cell.setPadding(5f)
                table.addCell(cell)
            }

            // Data
            val dataFont = Font(Font.FontFamily.HELVETICA, 10f, Font.NORMAL)
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

            for (t in transactions) {
                table.addCell(PdfPCell(Phrase(dateFormat.format(Date(t.date)), dataFont)))
                table.addCell(PdfPCell(Phrase(t.description, dataFont)))
                table.addCell(PdfPCell(Phrase(t.type.name, dataFont)))
                table.addCell(PdfPCell(Phrase(t.category.name, dataFont)))
                
                val amountStr = String.format(Locale.getDefault(), "%.2f", t.amount)
                val amountCell = PdfPCell(Phrase(amountStr, dataFont))
                amountCell.horizontalAlignment = Element.ALIGN_RIGHT
                table.addCell(amountCell)
            }

            document.add(table)
            document.close()

            return true

        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    private fun exportToExcelInternal(outputStream: OutputStream, transactions: List<Transaction>): Boolean {
        try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Transactions")

            // Header Style
            val headerStyle = workbook.createCellStyle()
            val font = workbook.createFont()
            font.bold = true
            headerStyle.setFont(font)
            headerStyle.fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
            headerStyle.fillPattern = FillPatternType.SOLID_FOREGROUND

            // Create Header
            val headerRow = sheet.createRow(0)
            val headers = listOf("Date", "Description", "Type", "Category", "Amount")
            for ((index, value) in headers.withIndex()) {
                val cell = headerRow.createCell(index)
                cell.setCellValue(value)
                cell.cellStyle = headerStyle
            }

            // Fill Data
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            for ((index, t) in transactions.withIndex()) {
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(dateFormat.format(Date(t.date)))
                row.createCell(1).setCellValue(t.description)
                row.createCell(2).setCellValue(t.type.name)
                row.createCell(3).setCellValue(t.category.name)
                row.createCell(4).setCellValue(t.amount)
            }

            // Manual column width (approximate 15 chars wide)
            for (i in headers.indices) {
                sheet.setColumnWidth(i, 15 * 256) 
            }

            workbook.write(outputStream)
            workbook.close()

            return true

        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

}
