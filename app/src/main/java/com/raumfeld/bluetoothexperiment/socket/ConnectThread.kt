package com.raumfeld.bluetoothexperiment.socket

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import com.raumfeld.bluetoothexperiment.MainActivity.Companion.TAG
import java.io.BufferedInputStream
import java.io.IOException
import java.net.URL
import java.util.*
import kotlin.concurrent.thread

class ConnectThread(val device: BluetoothDevice) : Thread() {

//    Device: Teufel One S, 50:1E:2D:11:01:8B, Service: 0000110b-0000-1000-8000-00805f9b34fb
//    Device: Teufel One S, 50:1E:2D:11:01:8B, Service: 0000110e-0000-1000-8000-00805f9b34fb
//    Device: Teufel One S, 50:1E:2D:11:01:8B, Service: 00000000-0000-1000-8000-00805f9b34fb
//    Device: Teufel One S, 50:1E:2D:11:01:8B, Service: 00000000-0000-1000-8000-00805f9b34fb

    private var mmSocket: BluetoothSocket? = null

    override fun run() {
        Log.d(TAG, "connect to ${device.name} in bond state: ${device.bondState}")

        val uuid = UUID.fromString("0000110b-0000-1000-8000-00805f9b34fb")
        mmSocket = device.createInsecureRfcommSocketToServiceRecord(uuid)
        sleep(5000)

        device.uuids.forEach { Log.d(TAG, "uuid: $it") }

        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mmSocket?.connect()
        } catch (e: IOException) {
            Log.w(TAG, "Exception while connecting", e)
            var clazz = device.javaClass
            var paramTypes = arrayOf<Class<*>>(Integer.TYPE)
            var m = clazz.getMethod("createRfcommSocket", *paramTypes)
            mmSocket = m.invoke(device, Integer.valueOf(2)) as BluetoothSocket
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket?.connect()
            } catch (e: IOException) {
                Log.e(TAG, "Exception while connecting", e)
                mmSocket = null
            }
        }

        sleep(5000)

        if (mmSocket == null) return

        Log.d(TAG, "connected to $mmSocket")

        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.
        sendFile("http://www.music.helsinki.fi/tmt/opetus/uusmedia/esim/a2002011001-e02.wav", mmSocket!!)
    }

    // Closes the client socket and causes the thread to finish.
    fun cancel() {
        try {
            mmSocket?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Could not close the client socket", e)
        }
    }

    @Throws(IOException::class)
    fun sendFile(url: String, bs: BluetoothSocket) {
        Log.d(TAG, "Is socket connected: ${bs.isConnected}")
        Log.d(TAG, "remote device name: ${bs.remoteDevice.name}")

        thread {
            val bis = BufferedInputStream(URL(url).openStream())
            val os = bs.outputStream
            bis.use {
                val bufferSize = 1024
                val buffer = ByteArray(bufferSize)

                // we need to know how may bytes were read to write them to the byteBuffer
                while (true) {
                    val len = it.read(buffer)

                    Log.i(TAG, "len: $len")
                    if (len == -1) break
                    os.write(buffer, 0, len)
                }
            }
        }
    }
}

