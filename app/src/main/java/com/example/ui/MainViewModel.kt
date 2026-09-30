package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Purchase
import com.example.data.Sale
import com.example.data.Customer
import com.example.lib.BackupHelper
import com.example.lib.SecurityPreferences
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val securityPrefs = SecurityPreferences(application)
    private val db = AppDatabase.getDatabase(application)
    private val purchaseDao = db.purchaseDao()
    private val saleDao = db.saleDao()
    private val customerDao = db.customerDao()

    private val exchangePrefs = application.getSharedPreferences("exchange_rates", Context.MODE_PRIVATE)

    private val _rateUsdCupCash = MutableStateFlow(exchangePrefs.getFloat("rate_usd_cup_cash", 350f))
    val rateUsdCupCash: StateFlow<Float> = _rateUsdCupCash.asStateFlow()

    private val _rateUsdCupTransf = MutableStateFlow(exchangePrefs.getFloat("rate_usd_cup_transf", 355f))
    val rateUsdCupTransf: StateFlow<Float> = _rateUsdCupTransf.asStateFlow()

    private val _rateEurCupCash = MutableStateFlow(exchangePrefs.getFloat("rate_eur_cup_cash", 360f))
    val rateEurCupCash: StateFlow<Float> = _rateEurCupCash.asStateFlow()

    private val _rateEurCupTransf = MutableStateFlow(exchangePrefs.getFloat("rate_eur_cup_transf", 365f))
    val rateEurCupTransf: StateFlow<Float> = _rateEurCupTransf.asStateFlow()

    private val _pinExists = MutableStateFlow(securityPrefs.hasPin())
    val pinExists: StateFlow<Boolean> = _pinExists.asStateFlow()

    private val _isLocked = MutableStateFlow(securityPrefs.isLocked())
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _lockoutRemainingSecs = MutableStateFlow(securityPrefs.getLockTimeRemainingSecs())
    val lockoutRemainingSecs: StateFlow<Long> = _lockoutRemainingSecs.asStateFlow()

    private val _activeScreen = MutableStateFlow("login")
    val activeScreen: StateFlow<String> = _activeScreen.asStateFlow()

    private val _currentEditingPurchase = MutableStateFlow<Purchase?>(null)
    val currentEditingPurchase: StateFlow<Purchase?> = _currentEditingPurchase.asStateFlow()

    val purchases: StateFlow<List<Purchase>> = purchaseDao.getAllPurchasesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sales: StateFlow<List<Sale>> = saleDao.getAllSalesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers: StateFlow<List<Customer>> = customerDao.getAllCustomersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    private var lockoutJob: Job? = null

    init {
        if (!securityPrefs.hasPin()) {
            _activeScreen.value = "setup_pin"
        } else {
            _activeScreen.value = "login"
        }
        startLockoutTicker()
    }

    private fun startLockoutTicker() {
        lockoutJob?.cancel()
        lockoutJob = viewModelScope.launch {
            while (true) {
                val remaining = securityPrefs.getLockTimeRemainingSecs()
                _lockoutRemainingSecs.value = remaining
                _isLocked.value = remaining > 0
                delay(1000)
            }
        }
    }

    fun navigateTo(screen: String) {
        _activeScreen.value = screen
    }

    fun setEditingPurchase(purchase: Purchase?) {
        _currentEditingPurchase.value = purchase
    }

    fun verifyPin(pin: String): Boolean {
        if (securityPrefs.isLocked()) return false
        val ok = securityPrefs.verifyPin(pin)
        if (ok) {
            _activeScreen.value = "dashboard"
            viewModelScope.launch { _toastMessage.emit("¡Bienvenido!") }
        } else {
            val remaining = securityPrefs.getLockTimeRemainingSecs()
            if (remaining > 0) {
                _isLocked.value = true
                _lockoutRemainingSecs.value = remaining
                viewModelScope.launch { _toastMessage.emit("Bloqueado por 30 segundos.") }
            } else {
                val attempts = securityPrefs.getFailedAttempts()
                val remainingAttempts = 3 - attempts
                viewModelScope.launch {
                    _toastMessage.emit("PIN incorrecto. Quedan $remainingAttempts intentos.")
                }
            }
        }
        return ok
    }

    fun createPin(pin: String) {
        securityPrefs.setPin(pin)
        _pinExists.value = true
        _activeScreen.value = "dashboard"
        viewModelScope.launch { _toastMessage.emit("PIN creado con éxito. ¡Bienvenido!") }
    }

    fun changePin(currentPin: String, newPin: String): Boolean {
        val hashedCurrent = com.example.lib.Crypto.sha256(currentPin)
        val saved = securityPrefs.getHashedPin()
        if (hashedCurrent != saved) {
            viewModelScope.launch { _toastMessage.emit("PIN actual incorrecto.") }
            return false
        }
        securityPrefs.setPin(newPin)
        viewModelScope.launch { _toastMessage.emit("PIN actualizado con éxito.") }
        return true
    }

    fun resetApp() {
        viewModelScope.launch {
            purchaseDao.deleteAllPurchases()
            saleDao.deleteAllSales()
            customerDao.deleteAllCustomers()
            
            securityPrefs.clearAll()
            _pinExists.value = false
            _activeScreen.value = "setup_pin"
            _toastMessage.emit("Aplicación restablecida. Datos borrados.")
        }
    }

    fun updateRates(usdCash: Float, usdTransf: Float, eurCash: Float, eurTransf: Float) {
        exchangePrefs.edit()
            .putFloat("rate_usd_cup_cash", usdCash)
            .putFloat("rate_usd_cup_transf", usdTransf)
            .putFloat("rate_eur_cup_cash", eurCash)
            .putFloat("rate_eur_cup_transf", eurTransf)
            .apply()
        _rateUsdCupCash.value = usdCash
        _rateUsdCupTransf.value = usdTransf
        _rateEurCupCash.value = eurCash
        _rateEurCupTransf.value = eurTransf
        viewModelScope.launch { _toastMessage.emit("Tasas de cambio actualizadas.") }
    }

    fun savePurchase(purchase: Purchase) {
        viewModelScope.launch {
            if (purchase.id == 0) {
                purchaseDao.insertPurchase(purchase)
                _toastMessage.emit("Compra registrada correctamente.")
            } else {
                purchaseDao.updatePurchase(purchase)
                _toastMessage.emit("Compra actualizada correctamente.")
            }
            _activeScreen.value = "history"
        }
    }

    fun duplicatePurchase(purchase: Purchase) {
        viewModelScope.launch {
            val copy = purchase.copy(
                id = 0,
                name = "${purchase.name} (Copia)",
                status = "Planificado",
                purchaseDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            )
            purchaseDao.insertPurchase(copy)
            _toastMessage.emit("Compra duplicada.")
        }
    }

    fun deletePurchase(id: Int) {
        viewModelScope.launch {
            purchaseDao.deletePurchaseById(id)
            _toastMessage.emit("Compra eliminada.")
        }
    }

    fun deleteSale(saleId: Int) {
        viewModelScope.launch {
            val s = saleDao.getAllSales().firstOrNull { it.id == saleId } ?: return@launch
            saleDao.deleteSaleById(saleId)
            
            // Revert status of purchase if it was "Vendido"
            val purchase = purchaseDao.getPurchaseById(s.purchaseId)
            if (purchase != null && purchase.status == "Vendido") {
                purchaseDao.updatePurchase(purchase.copy(status = "En Cuba"))
            }
            _toastMessage.emit("Venta eliminada.")
        }
    }

    fun showToast(msg: String) {
        viewModelScope.launch {
            _toastMessage.emit(msg)
        }
    }

    fun saveCustomer(customer: Customer) {
        viewModelScope.launch {
            if (customer.id == 0) {
                customerDao.insertCustomer(customer)
                _toastMessage.emit("Cliente guardado correctamente.")
            } else {
                customerDao.updateCustomer(customer)
                _toastMessage.emit("Cliente actualizado correctamente.")
            }
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            customerDao.deleteCustomer(customer)
            _toastMessage.emit("Cliente eliminado.")
        }
    }

    fun recordSale(
        purchaseId: Int, 
        quantitySold: Int, 
        pricePerUnitUsd: Double, 
        date: String,
        customerId: Int? = null,
        amountPaidUsd: Double? = null
    ) {
        viewModelScope.launch {
            val purchase = purchaseDao.getPurchaseById(purchaseId) ?: return@launch
            
            val existingSales = saleDao.getAllSales().filter { it.purchaseId == purchaseId }
            val alreadySold = existingSales.sumOf { it.quantitySold }
            val remainingStock = purchase.quantity - alreadySold
            
            if (quantitySold > remainingStock) {
                _toastMessage.emit("Error: Stock insuficiente. Solo quedan $remainingStock unidades.")
                return@launch
            }

            val totalAmountUsd = quantitySold * pricePerUnitUsd
            val paid = amountPaidUsd ?: totalAmountUsd

            val sale = Sale(
                purchaseId = purchaseId,
                quantitySold = quantitySold,
                pricePerUnitUsd = pricePerUnitUsd,
                saleDate = date,
                customerId = customerId,
                totalAmountUsd = totalAmountUsd,
                amountPaidUsd = paid
            )
            saleDao.insertSale(sale)

            val newTotalSold = alreadySold + quantitySold
            if (newTotalSold == purchase.quantity) {
                val updatedPurchase = purchase.copy(status = "Vendido")
                purchaseDao.updatePurchase(updatedPurchase)
                _toastMessage.emit("Venta registrada. ¡Producto completamente vendido!")
            } else {
                if (purchase.status == "Planificado" || purchase.status == "En Tránsito") {
                    val updatedPurchase = purchase.copy(status = "En Cuba")
                    purchaseDao.updatePurchase(updatedPurchase)
                }
                _toastMessage.emit("Venta de $quantitySold unidades registrada con éxito.")
            }
        }
    }

    fun exportBackup(context: Context): String {
        return try {
            val currentPurchases = purchases.value
            val currentSales = sales.value
            val serialized = BackupHelper.serializeBackup(currentPurchases, currentSales)
            val fileName = BackupHelper.saveBackupToDownloads(context, serialized)
            viewModelScope.launch { _toastMessage.emit("Respaldo exportado correctamente a Descargas: $fileName") }
            fileName
        } catch (e: Exception) {
            e.printStackTrace()
            viewModelScope.launch { _toastMessage.emit("Error al exportar: ${e.message}") }
            ""
        }
    }

    fun importBackup(jsonStr: String) {
        viewModelScope.launch {
            try {
                val backupData = BackupHelper.deserializeBackup(jsonStr)
                
                purchaseDao.deleteAllPurchases()
                saleDao.deleteAllSales()
                
                val purchaseIdMap = HashMap<Int, Int>()
                for (p in backupData.purchases) {
                    val originalId = p.id
                    val newId = purchaseDao.insertPurchase(p.copy(id = 0)).toInt()
                    purchaseIdMap[originalId] = newId
                }
                
                for (s in backupData.sales) {
                    val mappedPurchaseId = purchaseIdMap[s.purchaseId]
                    if (mappedPurchaseId != null) {
                        saleDao.insertSale(s.copy(id = 0, purchaseId = mappedPurchaseId))
                    }
                }
                
                _toastMessage.emit("Respaldo restaurado: ${backupData.purchases.size} compras importadas.")
                _activeScreen.value = "dashboard"
            } catch (e: SecurityException) {
                _toastMessage.emit("Error: Archivo corrupto o modificado")
            } catch (e: Exception) {
                _toastMessage.emit("Error: Archivo no válido")
            }
        }
    }

    fun loadTestData() {
        viewModelScope.launch {
            purchaseDao.deleteAllPurchases()
            saleDao.deleteAllSales()

            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val date3DaysAgo = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(System.currentTimeMillis() - 3 * 24 * 3600 * 1000L))
            val date7DaysAgo = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(System.currentTimeMillis() - 7 * 24 * 3600 * 1000L))
            val date15DaysAgo = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(System.currentTimeMillis() - 15 * 24 * 3600 * 1000L))

            val pbId = purchaseDao.insertPurchase(
                Purchase(
                    name = "Power Bank 20000mAh",
                    category = "Electrónica",
                    description = "Carga rápida 22.5W con cables integrados",
                    quantity = 10,
                    unitCostUsd = 15.0,
                    status = "En Cuba",
                    purchaseDate = date15DaysAgo,
                    shippingMethod = "Por Peso",
                    weightKg = 0.45,
                    unitShippingCostUsd = 2.5,
                    otherExpensesUsd = 5.0,
                    potentialSellingPriceUsd = 30.0
                )
            ).toInt()

            val zdId = purchaseDao.insertPurchase(
                Purchase(
                    name = "Zapatillas Deportivas",
                    category = "Ropa",
                    description = "Talla 42, color negro transpirable",
                    quantity = 5,
                    unitCostUsd = 25.0,
                    status = "Vendido",
                    purchaseDate = date15DaysAgo,
                    shippingMethod = "Por Dimensiones",
                    lengthCm = 30.0,
                    widthCm = 20.0,
                    heightCm = 12.0,
                    unitShippingCostUsd = 5.0,
                    otherExpensesUsd = 10.0,
                    potentialSellingPriceUsd = 55.0
                )
            ).toInt()

            val abId = purchaseDao.insertPurchase(
                Purchase(
                    name = "Auriculares Bluetooth",
                    category = "Electrónica",
                    description = "Cancelación activa de ruido, estuche táctil",
                    quantity = 20,
                    unitCostUsd = 8.0,
                    status = "En Tránsito",
                    purchaseDate = date7DaysAgo,
                    shippingMethod = "Por Peso",
                    weightKg = 0.1,
                    unitShippingCostUsd = 1.0,
                    otherExpensesUsd = 4.0,
                    potentialSellingPriceUsd = 20.0
                )
            ).toInt()

            val rsId = purchaseDao.insertPurchase(
                Purchase(
                    name = "Reloj Smartwatch",
                    category = "Electrónica",
                    description = "Pantalla AMOLED, sensor cardíaco",
                    quantity = 3,
                    unitCostUsd = 30.0,
                    status = "En Cuba",
                    purchaseDate = date3DaysAgo,
                    shippingMethod = "Por Peso",
                    weightKg = 0.25,
                    unitShippingCostUsd = 3.0,
                    otherExpensesUsd = 6.0,
                    potentialSellingPriceUsd = 65.0
                )
            ).toInt()

            val miId = purchaseDao.insertPurchase(
                Purchase(
                    name = "Mochila Impermeable",
                    category = "Accesorios",
                    description = "Capacidad 35L con compartimento para laptop",
                    quantity = 8,
                    unitCostUsd = 12.0,
                    status = "Planificado",
                    purchaseDate = todayStr,
                    shippingMethod = "Por Dimensiones",
                    lengthCm = 45.0,
                    widthCm = 32.0,
                    heightCm = 15.0,
                    unitShippingCostUsd = 4.0,
                    otherExpensesUsd = 0.0,
                    potentialSellingPriceUsd = 25.0
                )
            ).toInt()

            val smId = purchaseDao.insertPurchase(
                Purchase(
                    name = "Set de Maquillaje",
                    category = "Belleza",
                    description = "Estuche profesional de 24 piezas",
                    quantity = 6,
                    unitCostUsd = 18.0,
                    status = "En Cuba",
                    purchaseDate = date3DaysAgo,
                    shippingMethod = "Por Peso",
                    weightKg = 0.6,
                    unitShippingCostUsd = 3.5,
                    otherExpensesUsd = 2.0,
                    potentialSellingPriceUsd = 40.0
                )
            ).toInt()

            val llId = purchaseDao.insertPurchase(
                Purchase(
                    name = "Linterna LED Recargable",
                    category = "Electrónica",
                    description = "Foco de alta potencia, zoom y puerto USB",
                    quantity = 15,
                    unitCostUsd = 5.0,
                    status = "En Cuba",
                    purchaseDate = date7DaysAgo,
                    shippingMethod = "Por Peso",
                    weightKg = 0.15,
                    unitShippingCostUsd = 1.2,
                    otherExpensesUsd = 3.0,
                    potentialSellingPriceUsd = 12.0
                )
            ).toInt()

            val cgId = purchaseDao.insertPurchase(
                Purchase(
                    name = "Café Grano Premium",
                    category = "Alimentos",
                    description = "Bolsa de 1kg, tostado medio",
                    quantity = 12,
                    unitCostUsd = 10.0,
                    status = "Planificado",
                    purchaseDate = todayStr,
                    shippingMethod = "Por Peso",
                    weightKg = 1.0,
                    unitShippingCostUsd = 2.0,
                    otherExpensesUsd = 0.0,
                    potentialSellingPriceUsd = 22.0
                )
            ).toInt()

            saleDao.insertSale(
                Sale(
                    purchaseId = pbId,
                    quantitySold = 4,
                    pricePerUnitUsd = 30.0,
                    saleDate = date3DaysAgo
                )
            )

            saleDao.insertSale(
                Sale(
                    purchaseId = zdId,
                    quantitySold = 5,
                    pricePerUnitUsd = 55.0,
                    saleDate = date3DaysAgo
                )
            )

            saleDao.insertSale(
                Sale(
                    purchaseId = rsId,
                    quantitySold = 2,
                    pricePerUnitUsd = 65.0,
                    saleDate = todayStr
                )
            )

            saleDao.insertSale(
                Sale(
                    purchaseId = smId,
                    quantitySold = 3,
                    pricePerUnitUsd = 40.0,
                    saleDate = todayStr
                )
            )

            _toastMessage.emit("Datos de prueba cargados.")
        }
    }
}
