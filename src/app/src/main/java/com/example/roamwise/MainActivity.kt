package com.example.roamwise

import android.Manifest
import android.app.AppOpsManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.roamwise.databinding.ActivityMainBinding
import com.example.roamwise.settings.SettingsStore
import com.example.roamwise.settings.cycleStartMillis
import com.example.roamwise.stats.getRoamingBytesForSubscription
import com.example.roamwise.stats.listActiveSubscriptions
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val settings by lazy { SettingsStore(this) }

    private val phonePermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        Toast.makeText(this, if (granted) "Phone permission granted" else "Phone permission denied", Toast.LENGTH_SHORT).show()
        refreshUi()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnUsageAccess.setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
        binding.btnPhonePerm.setOnClickListener {
            phonePermLauncher.launch(Manifest.permission.READ_PHONE_STATE)
        }
        binding.btnRefresh.setOnClickListener { refreshUi() }
        binding.btnSetCycleDay.setOnClickListener { showSetCycleDayDialog() }

        refreshUi()
    }

    override fun onResume() {
        super.onResume()
        refreshUi()
    }

    private fun showSetCycleDayDialog() {
        val view = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_1, null)
        val input = EditText(this).apply {
            hint = "Enter reset day (1..28)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        AlertDialog.Builder(this)
            .setTitle("Reset day")
            .setView(input)
            .setPositiveButton("Save") { d, _ ->
                val day = input.text.toString().toIntOrNull()
                if (day != null && day in 1..28) {
                    lifecycleScope.launch {
                        settings.setCycleDay(day)
                        Toast.makeText(this@MainActivity, "Reset day set to $day", Toast.LENGTH_SHORT).show()
                        refreshUi()
                    }
                } else {
                    Toast.makeText(this, "Please enter a day between 1 and 28", Toast.LENGTH_SHORT).show()
                }
                d.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun refreshUi() {
        val hasUsage = hasUsageAccess()
        val hasPhone = ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED

        binding.btnUsageAccess.isEnabled = !hasUsage
        binding.btnPhonePerm.isEnabled = !hasPhone

        lifecycleScope.launch {
            val cycleDay = settings.getCycleDay()
            binding.cycleDayLabel.text = "Reset day: $cycleDay"

            val start = cycleStartMillis(cycleDay)
            if (Build.VERSION.SDK_INT >= 28 && hasUsage) {
                // List active SIMs, compute usage per SIM
                val subs = listActiveSubscriptions(this@MainActivity)
                // Reset panels
                binding.sim1Panel.visibility = android.view.View.GONE
                binding.sim2Panel.visibility = android.view.View.GONE

                if (subs.isEmpty()) {
                    binding.sim1Panel.visibility = android.view.View.VISIBLE
                    binding.sim1Title.text = "No active SIM"
                    binding.sim1Used.text = "Insert SIM to track roaming"
                    binding.sim1Progress.progress = 0
                    return@launch
                }

                // SIM 1
                val s1 = subs.getOrNull(0)
                if (s1 != null) {
                    val used1 = getRoamingBytesForSubscription(this@MainActivity, s1.subscriptionId, start)
                    binding.sim1Panel.visibility = android.view.View.VISIBLE
                    binding.sim1Title.text = "SIM 1 • ${s1.displayName ?: s1.carrierName ?: "Unknown"}"
                    binding.sim1Used.text = "Used: ${humanBytes(used1)}"
                    binding.sim1Progress.progress = 0 // (no quota/percent by design)
                }

                // SIM 2
                val s2 = subs.getOrNull(1)
                if (s2 != null) {
                    val used2 = getRoamingBytesForSubscription(this@MainActivity, s2.subscriptionId, start)
                    binding.sim2Panel.visibility = android.view.View.VISIBLE
                    binding.sim2Title.text = "SIM 2 • ${s2.displayName ?: s2.carrierName ?: "Unknown"}"
                    binding.sim2Used.text = "Used: ${humanBytes(used2)}"
                    binding.sim2Progress.progress = 0
                }
            } else {
                // not allowed / too old
                binding.sim1Panel.visibility = android.view.View.VISIBLE
                binding.sim1Title.text = if (!hasUsage) "Grant Usage Access, then Refresh" else "Requires Android 9+"
                binding.sim1Used.text = "Used: —"
                binding.sim1Progress.progress = 0

                binding.sim2Panel.visibility = android.view.View.GONE
            }
        }
    }

    private fun hasUsageAccess(): Boolean {
        val appOps = getSystemService(AppOpsManager::class.java) ?: return false
        val mode = if (Build.VERSION.SDK_INT >= 29) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }
}
