package com.raumfeld.bluetoothexperiment.socket

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import com.raumfeld.bluetoothexperiment.MainActivity.Companion.TAG
import java.io.*
import java.net.URL
import java.util.*
import kotlin.concurrent.thread

class ConnectAndSendThread(private val device: BluetoothDevice, private val fileUrl: String) : Thread() {

//    Device: Teufel One S, 50:1E:2D:11:01:8B, Service: 0000110b-0000-1000-8000-00805f9b34fb
//    Device: Teufel One S, 50:1E:2D:11:01:8B, Service: 0000110e-0000-1000-8000-00805f9b34fb
//    Device: Teufel One S, 50:1E:2D:11:01:8B, Service: 00000000-0000-1000-8000-00805f9b34fb
//    Device: Teufel One S, 50:1E:2D:11:01:8B, Service: 00000000-0000-1000-8000-00805f9b34fb

    private var bluetoothSocket: BluetoothSocket? = null

    override fun run() {

        Log.d(TAG, "connect to ${device.name} in bond state: ${device.bondState}")

//      val uuid = UUID.fromString("00001108-0000-1000-8000-00805f9b34fb") // Don't touch it works for REAL BLUE NC
//      val uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb") // Don't touch it works for Cinebar ONE

        device.uuids.forEach { Log.d(TAG, "uuid: $it") }

        val uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
        bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(uuid)
        sleep(500)

        bluetoothSocket?.connect()

        Log.d(TAG, "connected to ${bluetoothSocket?.remoteDevice?.name}")

        sendFile()
    }

    // Closes the client socket and causes the thread to finish.
    fun cancel() {
        Log.d(TAG, "cancel is called")
        try {
            bluetoothSocket?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Could not close the client socket", e)
        }
    }

    @Throws(IOException::class)
    fun sendFile() {
        if (bluetoothSocket == null) return

        Log.d(TAG, "Is socket connected?: ${bluetoothSocket?.isConnected}")

        thread {
            try {
                Log.d(TAG, "connection type: ${bluetoothSocket?.connectionType} (TYPE_RFCOMM 1, TYPE_SCO 2, TYPE_L2CAP 3)")
                Log.d(TAG, "maxTransmitPacketSize: " + bluetoothSocket?.maxTransmitPacketSize)
                Log.d(TAG, "maxReceivePacketSize: " + bluetoothSocket?.maxReceivePacketSize)

                val bis = BufferedInputStream(URL(fileUrl).openStream())
                bis.use { input ->
                    bluetoothSocket?.outputStream.use { fileOut ->
                        DataOutputStream(fileOut).use {
                            dataOut -> input.copyTo(dataOut)
                        }
                    }
                }
                Log.d(TAG, "done sending the file $fileUrl")

            } catch (e: Exception) {
                Log.e(TAG, "Error in reading URL or sending bytes to socket", e)
            }
        }
    }
}

