package com.raumfeld.bluetoothexperiment

import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MediaPlayerUsageActivity : AppCompatActivity() {

    var player = MediaPlayer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_music_via_bluetooth)

        title = "Play music on the device"

        findViewById<Button>(R.id.startMusic).setOnClickListener { startMusic() }
        findViewById<Button>(R.id.stopMusic).setOnClickListener { stopMusic() }
    }

    private fun startMusic() {
        val musicUri = Uri.parse("http://www.hochmuth.com/mp3/Tchaikovsky_Rococo_Var_orch.mp3")
        player.setAudioStreamType(AudioManager.STREAM_MUSIC)
        player.setDataSource(this, musicUri)
        player.prepare()
        player.start()
    }

    private fun stopMusic() {
        player.stop()
        player.release()
    }
}
