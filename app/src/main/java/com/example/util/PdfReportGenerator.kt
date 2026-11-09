package com.example.util

import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.local.model.Transaction
import com.example.data.model.CurrencyOption
import com.example.ui.viewmodel.CashFlowPeriod
import com.example.ui.viewmodel.DashboardSummary
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    fun generateAndDownloadReport(
        context: Context,
        transactions: List<Transaction>,
        dateRangeLabel: String = "All Time",
        selectedMemberName: String = "All Family",
        typeFilter: String = "All Types",
        currency: CurrencyOption,
        isAmountsHidden: Boolean = false
    ): File? {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 36f

        val totalIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
        val totalExpenses = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        val netBalance = totalIncome - totalExpenses

        val titlePaint = Paint().apply {
            color = Color.parseColor("#0F766E") // Deep Teal
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.parseColor("#475569")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val headerBgPaint = Paint().apply {
            color = Color.parseColor("#0F766E")
            style = Paint.Style.FILL
        }

        val headerTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val cellTextPaint = Paint().apply {
            color = Color.parseColor("#1E293B")
            textSize = 8.5f
            isAntiAlias = true
        }

        val greenTextPaint = Paint().apply {
            color = Color.parseColor("#059669")
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val redTextPaint = Paint().apply {
            color = Color.parseColor("#E11D48")
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val rowBgEven = Paint().apply {
            color = Color.parseColor("#F8FAFC")
            style = Paint.Style.FILL
        }

        val rowBgOdd = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }

        val borderPaint = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            style = Paint.Style.STROKE
            strokeWidth = 0.5f
        }

        val cardBgPaint = Paint().apply {
            color = Color.parseColor("#F1F5F9")
            style = Paint.Style.FILL
        }

        val nowFormatted = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date())
        val txRowsPerPageFirst = 20
        val txRowsPerPageSubsequent = 34

        var pageIndex = 1
        var txStartIndex = 0

        while (txStartIndex < transactions.size || pageIndex == 1) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex).create()
            val page = document.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            var y = margin

            if (pageIndex == 1) {
                // Header Banner
                canvas.drawText("HomeFlow — Family Financial Report", margin, y + 16f, titlePaint)
                y += 24f
                canvas.drawText("Generated on: $nowFormatted", margin, y + 10f, subtitlePaint)
                y += 18f

                // Applied Filters Box
                canvas.drawRoundRect(margin, y, pageWidth - margin, y + 42f, 8f, 8f, cardBgPaint)
                val filterLine1 = "Date Range: $dateRangeLabel  •  User: $selectedMemberName  •  Type: $typeFilter"
                val filterLine2 = "Currency: ${currency.name} (${currency.symbol})  •  Total Records: ${transactions.size}"
                canvas.drawText(filterLine1, margin + 12f, y + 16f, subtitlePaint)
                canvas.drawText(filterLine2, margin + 12f, y + 32f, subtitlePaint)
                y += 52f

                // Financial Overview 4-Stat Row
                val cardWidth = (pageWidth - margin * 2 - 24f) / 4f
                val cardHeight = 44f

                // Inflow Card
                drawStatBox(
                    canvas, margin, y, cardWidth, cardHeight,
                    "Total Inflow",
                    "+${currency.symbol}${"%.2f".format(totalIncome)}",
                    Color.parseColor("#059669")
                )

                // Outflow Card
                drawStatBox(
                    canvas, margin + cardWidth + 8f, y, cardWidth, cardHeight,
                    "Total Outflow",
                    "-${currency.symbol}${"%.2f".format(totalExpenses)}",
                    Color.parseColor("#E11D48")
                )

                // Net Balance Card
                drawStatBox(
                    canvas, margin + (cardWidth + 8f) * 2, y, cardWidth, cardHeight,
                    "Net Cash Flow",
                    "${if (netBalance >= 0) "+" else ""}${currency.symbol}${"%.2f".format(netBalance)}",
                    Color.parseColor("#0F766E")
                )

                // Total Records Card
                drawStatBox(
                    canvas, margin + (cardWidth + 8f) * 3, y, cardWidth, cardHeight,
                    "Report Records",
                    "${transactions.size} Items",
                    Color.parseColor("#475569")
                )

                y += cardHeight + 16f

                // Table Header
                canvas.drawText("Transactions Log", margin, y + 10f, titlePaint.apply { textSize = 13f })
                y += 18f
            } else {
                // Secondary page header
                canvas.drawText("HomeFlow — Family Financial Report (Cont.)", margin, y + 14f, titlePaint.apply { textSize = 12f })
                canvas.drawText("Page $pageIndex", pageWidth - margin - 40f, y + 14f, subtitlePaint)
                y += 26f
            }

            // Transactions Table Header
            val tableTop = y
            val colDate = margin + 8f
            val colTitle = margin + 70f
            val colCategory = margin + 210f
            val colMember = margin + 300f
            val colMethod = margin + 375f
            val colAmount = pageWidth - margin - 8f

            canvas.drawRoundRect(margin, tableTop, pageWidth - margin, tableTop + 20f, 4f, 4f, headerBgPaint)
            canvas.drawText("Date", colDate, tableTop + 14f, headerTextPaint)
            canvas.drawText("Title / Description", colTitle, tableTop + 14f, headerTextPaint)
            canvas.drawText("Category", colCategory, tableTop + 14f, headerTextPaint)
            canvas.drawText("Member", colMember, tableTop + 14f, headerTextPaint)
            canvas.drawText("Method", colMethod, tableTop + 14f, headerTextPaint)

            val amtHeaderWidth = headerTextPaint.measureText("Amount")
            canvas.drawText("Amount", colAmount - amtHeaderWidth, tableTop + 14f, headerTextPaint)
            y += 20f

            val rowsThisPage = if (pageIndex == 1) txRowsPerPageFirst else txRowsPerPageSubsequent
            val txEndIndex = (txStartIndex + rowsThisPage).coerceAtMost(transactions.size)

            val rowHeight = 18f
            for (i in txStartIndex until txEndIndex) {
                val tx = transactions[i]
                val rowBg = if ((i % 2) == 0) rowBgEven else rowBgOdd
                canvas.drawRect(margin, y, pageWidth - margin, y + rowHeight, rowBg)
                canvas.drawRect(margin, y, pageWidth - margin, y + rowHeight, borderPaint)

                val dateStr = SimpleDateFormat("MM/dd/yy", Locale.getDefault()).format(Date(tx.dateMillis))
                val titleTrunc = if (tx.title.length > 24) tx.title.take(22) + "..." else tx.title
                val memberTrunc = if (tx.memberName.length > 12) tx.memberName.take(10) + ".." else tx.memberName
                val isExpense = tx.type == "EXPENSE"
                val isIncome = tx.type == "INCOME"
                val sign = if (isExpense) "-" else if (isIncome) "+" else ""
                val amtStr = "$sign${currency.symbol}${"%.2f".format(tx.amount)}"
                val paintForAmt = if (isExpense) redTextPaint else if (isIncome) greenTextPaint else cellTextPaint

                canvas.drawText(dateStr, colDate, y + 12f, cellTextPaint)
                canvas.drawText(titleTrunc, colTitle, y + 12f, cellTextPaint)
                canvas.drawText(tx.category, colCategory, y + 12f, cellTextPaint)
                canvas.drawText(memberTrunc, colMember, y + 12f, cellTextPaint)
                canvas.drawText(tx.paymentMethod, colMethod, y + 12f, cellTextPaint)

                val amtWidth = paintForAmt.measureText(amtStr)
                canvas.drawText(amtStr, colAmount - amtWidth, y + 12f, paintForAmt)

                y += rowHeight
            }

            if (transactions.isEmpty() && pageIndex == 1) {
                canvas.drawRect(margin, y, pageWidth - margin, y + 30f, rowBgEven)
                canvas.drawText("No transactions found for the selected filters.", margin + 14f, y + 20f, subtitlePaint)
                y += 30f
            }

            // Footer
            val footerText = "Generated by HomeFlow • Page $pageIndex"
            canvas.drawText(footerText, margin, pageHeight - margin + 12f, subtitlePaint)

            document.finishPage(page)
            txStartIndex = txEndIndex
            pageIndex++

            if (txStartIndex >= transactions.size) break
        }

        // Save PDF to Documents
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "HomeFlow_Report_$timeStamp.pdf"

        val docsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
        val file = File(docsDir, fileName)

        try {
            val fos = FileOutputStream(file)
            document.writeTo(fos)
            fos.flush()
            fos.close()
            document.close()

            // Also copy to public Downloads if Android 10+ MediaStore is supported
            saveToPublicDownloads(context, file, fileName)

            // Show Toast & launch open/share intent
            Toast.makeText(context, "PDF Report exported successfully!", Toast.LENGTH_SHORT).show()
            launchPdfIntent(context, file)

            // Trigger notification
            NotificationHelper.showNotification(
                context = context,
                notificationId = System.currentTimeMillis().toInt(),
                title = "Financial PDF Report Ready",
                message = "Exported $fileName (${transactions.size} records) for $selectedMemberName."
            )

            return file
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to export PDF: ${e.message}", Toast.LENGTH_LONG).show()
            document.close()
            return null
        }
    }

    private fun drawStatBox(
        canvas: Canvas,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        label: String,
        value: String,
        valueColor: Int
    ) {
        val bgPaint = Paint().apply {
            color = Color.parseColor("#F8FAFC")
            style = Paint.Style.FILL
        }
        val border = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val labelPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 7.5f
            isAntiAlias = true
        }
        val valuePaint = Paint().apply {
            color = valueColor
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        canvas.drawRoundRect(x, y, x + width, y + height, 6f, 6f, bgPaint)
        canvas.drawRoundRect(x, y, x + width, y + height, 6f, 6f, border)

        canvas.drawText(label, x + 8f, y + 14f, labelPaint)
        canvas.drawText(value, x + 8f, y + 32f, valuePaint)
    }

    private fun saveToPublicDownloads(context: Context, sourceFile: File, fileName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        sourceFile.inputStream().use { input ->
                            input.copyTo(out)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun launchPdfIntent(context: Context, file: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "HomeFlow Financial Report")
                putExtra(Intent.EXTRA_TEXT, "Here is the family financial report exported from HomeFlow.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "View or Share PDF Report").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
