package com.example.lib

import android.content.Context
import android.content.SharedPreferences

class SecurityPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("security_prefs", Context.MODE_PRIVATE)

    fun getHashedPin(): String? {
        return prefs.getString("hashed_pin", null)
    }

    fun hasPin(): Boolean {
        return getHashedPin() != null
    }

    fun setPin(pin: String) {
        val hashed = Crypto.sha256(pin)
        prefs.edit().putString("hashed_pin", hashed).apply()
        resetAttempts()
    }

    fun verifyPin(pin: String): Boolean {
        val hashedInput = Crypto.sha256(pin)
        val savedPin = getHashedPin() ?: return false
        val matched = hashedInput == savedPin
        if (matched) {
            resetAttempts()
        } else {
            incrementAttempts()
        }
        return matched
    }

    fun getFailedAttempts(): Int {
        return prefs.getInt("failed_attempts", 0)
    }

    private fun incrementAttempts() {
        val current = getFailedAttempts() + 1
        val edit = prefs.edit().putInt("failed_attempts", current)
        if (current >= 3) {
            val lockTime = System.currentTimeMillis() + 30000 // 30 seconds
            edit.putLong("lock_until_millis", lockTime)
        }
        edit.apply()
    }

    fun getLockTimeRemainingSecs(): Long {
        val lockUntil = prefs.getLong("lock_until_millis", 0L)
        val now = System.currentTimeMillis()
        if (lockUntil <= now) return 0L
        return (lockUntil - now) / 1000L
    }

    fun isLocked(): Boolean {
        return getLockTimeRemainingSecs() > 0L
    }

    fun resetAttempts() {
        prefs.edit()
            .putInt("failed_attempts", 0)
            .putLong("lock_until_millis", 0L)
            .apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
