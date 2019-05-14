package com.raumfeld.bluetoothexperiment

import android.bluetooth.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView

class DeviceControlActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_control)

        title = "GATT services and characteristics"
    }

    override fun onResume() {
        super.onResume()

        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

        val device = bluetoothAdapter?.bondedDevices?.firstOrNull() ?: return

        findViewById<TextView>(R.id.deviceInfo).text = "Connected to: ${device.name}"

        device.connectGatt(this, false, gattCallback)
    }

    // Various callback methods defined by the BLE API.
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(
            gatt: BluetoothGatt,
            status: Int,
            newState: Int
        ) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.i(MainActivity.TAG, "Connected to GATT server.")
                    Log.i(MainActivity.TAG, "Attempting to start service discovery: " + gatt.discoverServices())
                }
                BluetoothProfile.STATE_CONNECTING -> {
                    Log.i(MainActivity.TAG, "Connecting to GATT server.")
                }
                BluetoothProfile.STATE_DISCONNECTING -> {
                    Log.i(MainActivity.TAG, "Disconnecting from GATT server.")
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.i(MainActivity.TAG, "Disconnected from GATT server.")
                }
            }
        }

        // New services discovered
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> displayGattServices(gatt.services)
                else -> Log.w(MainActivity.TAG, "onServicesDiscovered received: $status")
            }
        }

        // Result of a characteristic read operation
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> Log.i(MainActivity.TAG, "characteristic: $characteristic")
            }
        }
    }

    private fun displayGattServices(gattServices: List<BluetoothGattService>?) {
        runOnUiThread {
            val servicesContainer = findViewById<LinearLayout>(R.id.servicesGATT)
            servicesContainer.removeAllViews()

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 100)

            // Loops through available GATT Services.
            gattServices?.forEach { gattService ->
                val serviceLayout = LinearLayout(this)
                serviceLayout.layoutParams = params
                serviceLayout.orientation = LinearLayout.VERTICAL

                val serviceInfo = TextView(this)
                val serviceUuid = gattService.uuid.toString()
                serviceInfo.text = "Service: $serviceUuid"
                serviceLayout.addView(serviceInfo)

                gattService.characteristics.forEach { gattCharacteristic ->
                    val characteristicUuid = gattCharacteristic.uuid.toString()

                    val value = gattService.getCharacteristic(gattCharacteristic.uuid).value?.toString() ?: "-"

                    val characteristicInfo = TextView(this)
                    characteristicInfo.text = "Characteristic $characteristicUuid: $value"
                    serviceLayout.addView(characteristicInfo)
                }

                servicesContainer.addView(serviceLayout)
            }
        }
    }
}
