package com.example.lib

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.data.Purchase
import com.example.data.Sale
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupHelper {

    fun serializeBackup(purchases: List<Purchase>, sales: List<Sale>): String {
        val root = JSONObject()
        root.put("version", "1.0")
        root.put("app", "ImportManager")
        
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        root.put("export_date", sdf.format(Date()))

        // Data block
        val dataObj = JSONObject()
        
        val purchasesArray = JSONArray()
        for (p in purchases) {
            val pObj = JSONObject()
            pObj.put("id", p.id)
            pObj.put("photoBase64", p.photoBase64 ?: JSONObject.NULL)
            pObj.put("name", p.name)
            pObj.put("category", p.category ?: JSONObject.NULL)
            pObj.put("description", p.description ?: JSONObject.NULL)
            pObj.put("quantity", p.quantity)
            pObj.put("unitCostUsd", p.unitCostUsd)
            pObj.put("status", p.status)
            pObj.put("purchaseDate", p.purchaseDate)
            pObj.put("shippingMethod", p.shippingMethod)
            pObj.put("weightKg", p.weightKg ?: JSONObject.NULL)
            pObj.put("lengthCm", p.lengthCm ?: JSONObject.NULL)
            pObj.put("widthCm", p.widthCm ?: JSONObject.NULL)
            pObj.put("heightCm", p.heightCm ?: JSONObject.NULL)
            pObj.put("unitShippingCostUsd", p.unitShippingCostUsd)
            pObj.put("otherExpensesUsd", p.otherExpensesUsd ?: JSONObject.NULL)
            pObj.put("potentialSellingPriceUsd", p.potentialSellingPriceUsd)
            purchasesArray.put(pObj)
        }
        dataObj.put("purchases", purchasesArray)

        val salesArray = JSONArray()
        for (s in sales) {
            val sObj = JSONObject()
            sObj.put("id", s.id)
            sObj.put("purchaseId", s.purchaseId)
            sObj.put("quantitySold", s.quantitySold)
            sObj.put("pricePerUnitUsd", s.pricePerUnitUsd)
            sObj.put("saleDate", s.saleDate)
            salesArray.put(sObj)
        }
        dataObj.put("sales", salesArray)
        dataObj.put("config", JSONObject()) // empty config placeholder

        root.put("data", dataObj)

        // Calculate checksum on data block
        val dataString = dataObj.toString()
        val hash = Crypto.sha256(dataString)
        root.put("checksum", hash)

        return root.toString(2)
    }

    class BackupData(
        val purchases: List<Purchase>,
        val sales: List<Sale>
    )

    fun deserializeBackup(jsonStr: String): BackupData {
        val root = JSONObject(jsonStr)
        if (!root.has("app") || root.getString("app") != "ImportManager") {
            throw IllegalArgumentException("Archivo no válido: No pertenece a ImportManager")
        }
        
        if (!root.has("data") || !root.has("checksum")) {
            throw IllegalArgumentException("Archivo no válido: Estructura incompleta")
        }

        val dataObj = root.getJSONObject("data")
        val checksum = root.getString("checksum")

        // Verify checksum
        val calculatedHash = Crypto.sha256(dataObj.toString())
        if (calculatedHash != checksum) {
            throw SecurityException("Archivo corrupto o modificado")
        }

        val purchasesArray = dataObj.getJSONArray("purchases")
        val parsedPurchases = ArrayList<Purchase>()
        for (i in 0 until purchasesArray.length()) {
            val pObj = purchasesArray.getJSONObject(i)
            parsedPurchases.add(
                Purchase(
                    // Parse original ID for mapping sales, but it will be inserted as id=0 in Room
                    id = pObj.optInt("id", 0),
                    photoBase64 = if (pObj.isNull("photoBase64")) null else pObj.getString("photoBase64"),
                    name = pObj.getString("name"),
                    category = if (pObj.isNull("category")) null else pObj.getString("category"),
                    description = if (pObj.isNull("description")) null else pObj.getString("description"),
                    quantity = pObj.getInt("quantity"),
                    unitCostUsd = pObj.getDouble("unitCostUsd"),
                    status = pObj.getString("status"),
                    purchaseDate = pObj.getString("purchaseDate"),
                    shippingMethod = pObj.getString("shippingMethod"),
                    weightKg = if (pObj.isNull("weightKg")) null else pObj.getDouble("weightKg"),
                    lengthCm = if (pObj.isNull("lengthCm")) null else pObj.getDouble("lengthCm"),
                    widthCm = if (pObj.isNull("widthCm")) null else pObj.getDouble("widthCm"),
                    heightCm = if (pObj.isNull("heightCm")) null else pObj.getDouble("heightCm"),
                    unitShippingCostUsd = pObj.getDouble("unitShippingCostUsd"),
                    otherExpensesUsd = if (pObj.isNull("otherExpensesUsd")) null else pObj.getDouble("otherExpensesUsd"),
                    potentialSellingPriceUsd = pObj.getDouble("potentialSellingPriceUsd")
                )
            )
        }

        val salesArray = dataObj.getJSONArray("sales")
        val parsedSales = ArrayList<Sale>()
        for (i in 0 until salesArray.length()) {
            val sObj = salesArray.getJSONObject(i)
            parsedSales.add(
                Sale(
                    id = 0,
                    purchaseId = sObj.getInt("purchaseId"), // note: this might need mapping if parent IDs change. We will preserve it or map it during db write
                    quantitySold = sObj.getInt("quantitySold"),
                    pricePerUnitUsd = sObj.getDouble("pricePerUnitUsd"),
                    saleDate = sObj.getString("saleDate")
                )
            )
        }

        return BackupData(parsedPurchases, parsedSales)
    }

    fun saveBackupToDownloads(context: Context, jsonContent: String): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val fileName = "importmanager-backup-${sdf.format(Date())}.json"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri).use { outputStream ->
                    outputStream?.write(jsonContent.toByteArray())
                }
                return fileName
            }
        } else {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            FileOutputStream(file).use { outputStream ->
                outputStream.write(jsonContent.toByteArray())
            }
            return fileName
        }
        throw Exception("No se pudo guardar el archivo")
    }
}
