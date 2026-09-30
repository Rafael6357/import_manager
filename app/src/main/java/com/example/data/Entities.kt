package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(tableName = "purchases")
data class Purchase(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val photoBase64: String? = null,
    val name: String,
    val category: String? = null,
    val description: String? = null,
    val quantity: Int,
    val unitCostUsd: Double,
    val status: String, // "Planificado" | "En Tránsito" | "En Cuba" | "Vendido"
    val purchaseDate: String,
    val shippingMethod: String, // "Por Peso" | "Por Dimensiones" | "Mayor"
    val weightKg: Double? = null,
    val lengthCm: Double? = null,
    val widthCm: Double? = null,
    val heightCm: Double? = null,
    val unitShippingCostUsd: Double,
    val otherExpensesUsd: Double? = null,
    val potentialSellingPriceUsd: Double,
    val trackingNumber: String? = null,
    val shippingAgency: String? = null
)

@Entity(
    tableName = "sales",
    foreignKeys = [
        ForeignKey(
            entity = Purchase::class,
            parentColumns = ["id"],
            childColumns = ["purchaseId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val purchaseId: Int,
    val quantitySold: Int,
    val pricePerUnitUsd: Double,
    val saleDate: String,
    val customerId: Int? = null,
    val totalAmountUsd: Double = 0.0,
    val amountPaidUsd: Double = 0.0
)

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String? = null,
    val address: String? = null,
    val notes: String? = null
)
