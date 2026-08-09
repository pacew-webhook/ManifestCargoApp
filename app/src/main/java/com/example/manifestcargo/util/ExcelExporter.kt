package com.example.manifestcargo.util

import android.content.Context
import android.os.Environment
import com.example.manifestcargo.data.CargoItem
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ExcelExporter {

    fun exportWithTemplate(context: Context, items: List<CargoItem>): File? {
        return try {
            // Read template from assets
            val inputStream: InputStream = context.assets.open("template_manifest.xlsx")
            val workbook = XSSFWorkbook(inputStream)
            val sheet = workbook.getSheetAt(0)

            // Dynamic starting row (misal data mulai di baris ke-5 / index 4)
            var startRow = 4

            for (item in items) {
                val row = sheet.getRow(startRow) ?: sheet.createRow(startRow)

                row.createCell(0).setCellValue(item.awbNo)
                row.createCell(1).setCellValue(item.flightNo)
                row.createCell(2).setCellValue(item.pti)
                row.createCell(3).setCellValue(item.pcsQty.toDouble())
                row.createCell(4).setCellValue(item.pcsQtyWt)
                row.createCell(5).setCellValue(item.subTotalKg)
                row.createCell(6).setCellValue(item.description)
                row.createCell(7).setCellValue(item.customer)
                row.createCell(8).setCellValue(item.noPag)

                startRow++
            }

            // Save new Excel File
            val fileName = "Manifest_Cargo_${System.currentTimeMillis()}.xlsx"
            val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val outputFile = File(outputDir, fileName)

            val outputStream = FileOutputStream(outputFile)
            workbook.write(outputStream)

            outputStream.close()
            workbook.close()
            inputStream.close()

            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

