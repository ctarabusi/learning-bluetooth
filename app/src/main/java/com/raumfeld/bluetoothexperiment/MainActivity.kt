package com.raumfeld.bluetoothexperiment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.raumfeld.bluetoothexperiment.socket.ConnectThread
import kotlin.concurrent.thread
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.IntentFilter


class MainActivity : AppCompatActivity() {

    var connectedThread: ConnectThread? = null
    val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    private val btDeviceList = ArrayList<BluetoothDevice>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()

        findViewById<Button>(R.id.searchForSdp).setOnClickListener { searchForSdp() }
        findViewById<Button>(R.id.sendFileViaSocket).setOnClickListener { sendFileViaSocket() }
        findViewById<Button>(R.id.scanLowEnergyDevices).setOnClickListener { startScanLE() }
        findViewById<Button>(R.id.connectToGATT).setOnClickListener { connectToGATT() }

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothDevice.ACTION_UUID)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(actionFoundReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        connectedThread?.cancel()
        connectedThread = null

        unregisterReceiver(actionFoundReceiver)
    }

    private fun searchForSdp() {
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        val bluetoothDevice = pairedDevices?.first() ?: return

        bluetoothDevice.fetchUuidsWithSdp()
    }

    private fun sendFileViaSocket() {
        thread {

            val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
            val bluetoothDevice = pairedDevices?.first() ?: return@thread

            bluetoothAdapter?.cancelDiscovery()

            connectedThread = ConnectThread(bluetoothDevice)
            connectedThread?.run()

        }
    }

    private fun startScanLE() {
        startActivity(Intent(this, ScanLeActivity::class.java))
    }

    private fun connectToGATT() {
        startActivity(Intent(this, DeviceControlActivity::class.java))
    }

    companion object {
        const val TAG: String = "learning-bluetooth"
    }

    private val actionFoundReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action;
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice
                Log.d(TAG, "\n  Device: " + device.name + ", " + device)
                btDeviceList.add(device)
            } else {
                if (BluetoothDevice.ACTION_UUID == action) {
                    val device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice
                    val uuidExtra = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID)
                    uuidExtra.forEach {
                        Log.d(TAG, "\n  Device: " + device.name + ", " + device + ", Service: " + it.toString())
                    }
                } else {
                    if (BluetoothAdapter.ACTION_DISCOVERY_STARTED == action) {
                        Log.d(TAG, "\nDiscovery Started...")
                    } else {
                        if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                            Log.d(TAG, "\nDiscovery Finished")
                            val itr = btDeviceList.iterator()
                            while (itr.hasNext()) {
                                // Get Services for paired devices
                                val device = itr.next()
                                Log.d(TAG, "\nGetting Services for " + device.name + ", " + device)
                                if (!device.fetchUuidsWithSdp()) {
                                    Log.d(TAG, "\nSDP Failed for " + device.name)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

