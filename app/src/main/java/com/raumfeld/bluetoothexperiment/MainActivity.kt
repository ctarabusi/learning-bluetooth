package com.raumfeld.bluetoothexperiment

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.mediaplayerUsage).setOnClickListener { mediaPlayerUsage() }
        findViewById<Button>(R.id.sendFileViaSocket).setOnClickListener { sendFileViaSocket() }
        findViewById<Button>(R.id.scanLowEnergyDevices).setOnClickListener { startScanLE() }
        findViewById<Button>(R.id.connectToGATT).setOnClickListener { connectToGATT() }
    }

    private fun mediaPlayerUsage() {
        startActivity(Intent(this, MediaPlayerUsageActivity::class.java))
    }

    private fun sendFileViaSocket() {
        startActivity(Intent(this, RFCommSocketActivity::class.java))
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
}

