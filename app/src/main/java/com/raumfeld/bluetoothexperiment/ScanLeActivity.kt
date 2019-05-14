package com.raumfeld.bluetoothexperiment

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class ScanLeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_le)

        title = "Scanning Low Energy devices"
    }

    private fun PackageManager.missingSystemFeature(name: String): Boolean = !hasSystemFeature(name)

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled

    private var enable: Boolean = true
    private var scanning: Boolean = false


    private val foundDevices = HashSet<String>()

    private val leScanCallback = object: ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            Log.d(MainActivity.TAG, "scan result: $callbackType $result")
            foundDevices.add(
                "${result.device.address} ${result.device.name ?: ""} ${result.isConnectable}"
            )
        }
    }

    override fun onResume() {
        super.onResume()

        packageManager.takeIf { it.missingSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) }?.also {
            Toast.makeText(this, "BLE not supported", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Ensures Bluetooth is available on the device and it is enabled. If not,
// displays a dialog requesting user permission to enable Bluetooth.
        bluetoothAdapter?.takeIf { it.isDisabled }?.apply {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        enable = true

        when (enable) {
            true -> {
                // Stops scanning after a pre-defined scan period.
                Handler().postDelayed({
                    scanning = false
                    bluetoothAdapter?.bluetoothLeScanner?.stopScan(leScanCallback)
                    refreshUI()
                    enable = false
                }, SCAN_PERIOD)
                scanning = true
                bluetoothAdapter?.bluetoothLeScanner?.startScan(leScanCallback)
            }
            else -> {
                scanning = false
                bluetoothAdapter?.bluetoothLeScanner?.stopScan(leScanCallback)
            }
        }
    }

    private fun refreshUI(){
        foundDevices.forEach {
            val deviceTextView = TextView(this)
            deviceTextView.text = it
            findViewById<LinearLayout>(R.id.devicesContainer).addView(deviceTextView)
        }
        foundDevices.clear()
    }

    companion object {
        const val REQUEST_ENABLE_BT = 12
        const val SCAN_PERIOD: Long = 10000
    }

}
