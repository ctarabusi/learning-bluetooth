package com.raumfeld.bluetoothexperiment

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.raumfeld.bluetoothexperiment.socket.ConnectAndSendThread
import kotlin.concurrent.thread

class RFCommSocketActivity : AppCompatActivity() {

    var connectedThread: ConnectAndSendThread? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rfcomm_socket)

        title = "Sending a file via Bluetoothsocket"

        findViewById<Button>(R.id.sendFileViaSocket).setOnClickListener { sendFileViaSocket() }
    }

    private fun sendFileViaSocket() {
        thread {
            val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
            val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
            val bluetoothDevice = pairedDevices?.first() ?: return@thread

            val fileUri = "http://www.hochmuth.com/mp3/Tchaikovsky_Rococo_Var_orch.mp3"
            connectedThread = ConnectAndSendThread(bluetoothDevice, fileUri)
            connectedThread?.run()
        }
    }

    override fun onPause() {
        super.onPause()
        connectedThread?.cancel()
        connectedThread = null
    }
}
