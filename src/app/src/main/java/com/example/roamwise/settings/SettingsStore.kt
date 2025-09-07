package com.example.roamwise.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.roamwise.settings.SettingsKeys.CYCLE_DAY
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("roamwise_settings")

object SettingsKeys {
    val CYCLE_DAY: Preferences.Key<Int> = intPreferencesKey("cycle_day") // 1..28
}

class SettingsStore(private val context: Context) {
    suspend fun getCycleDay(): Int =
        context.dataStore.data.map { it[CYCLE_DAY] ?: 1 }.first()

    suspend fun setCycleDay(day: Int) {
        context.dataStore.edit { it[CYCLE_DAY] = day.coerceIn(1, 28) }
    }
}
