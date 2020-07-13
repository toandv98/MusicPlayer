package com.toandv.demo.musicplayer.ui.main

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.toandv.demo.musicplayer.R
import com.toandv.demo.musicplayer.data.SongRepository
import com.toandv.demo.musicplayer.data.local.SongLocalDataSource
import com.toandv.demo.musicplayer.data.model.Song
import com.toandv.demo.musicplayer.services.PlayerService
import com.toandv.demo.musicplayer.utils.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), MainContract.View {

    private val songAdapter = SongAdapter()
    private var isBound = false
    private lateinit var playerService: PlayerService
    private lateinit var presenter: MainContract.Presenter

    private val playerServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(componentName: ComponentName?) {
            isBound = false
        }

        override fun onServiceConnected(componentName: ComponentName?, iBinder: IBinder?) {
            val binder = iBinder as PlayerService.LocalBinder
            playerService = binder.getService()
            isBound = true
            playerService.updatePlayList()
            playerService.restoreDetail()
        }
    }

    private var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Actions.ACTION_PREPARED -> {
                    intent.getParcelableExtra<Song>(Extras.EXTRA_SONG)?.let { updateSongDetail(it) }
                }

                Actions.ACTION_PLAYING_CHANGED -> {
                    when (intent.getBooleanExtra(Extras.EXTRA_PLAYING, false)) {
                        true -> btnPlay.setImageResource(R.drawable.ic_baseline_pause_circle_outline)
                        else -> btnPlay.setImageResource(R.drawable.ic_baseline_play_circle_outline)
                    }
                }

                Actions.ACTION_SEEK_CHANGED -> {
                    intent.getIntExtra(Extras.EXTRA_POSITION, 0).let {
                        seekDuration.progress = it
                        tvCurrentTime.text = TimeUtils.toTimer(it.toLong())
                    }
                }

                Actions.ACTION_ERROR -> {
                    Toast.makeText(
                        context,
                        intent.getStringExtra(Extras.EXTRA_MSG),
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        presenter = MainPresenter(
            this,
            SongRepository.getInstance(SongLocalDataSource.getInstance(this))
        )
        requestPermission()
        setupView()
        initListener()
    }

    override fun onStart() {
        super.onStart()
        bindService(
            Intent(this, PlayerService::class.java),
            playerServiceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    override fun onResume() {
        super.onResume()
        IntentFilter().also {
            it.addAction(Actions.ACTION_ERROR)
            it.addAction(Actions.ACTION_PLAYING_CHANGED)
            it.addAction(Actions.ACTION_PREPARED)
            it.addAction(Actions.ACTION_SEEK_CHANGED)
            @Suppress("DEPRECATION")
            LocalBroadcastManager.getInstance(this).registerReceiver(receiver, it)
        }
        if (isBound) {
            playerService.restoreDetail()
        }
    }

    override fun onPause() {
        @Suppress("DEPRECATION")
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
        super.onPause()
    }

    override fun onStop() {
        Intent(this, PlayerService::class.java).let {
            it.action = Actions.ACTION_START_FOREGROUND
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> startForegroundService(it)
                else -> startService(it)
            }
        }
        if (isBound) {
            unbindService(playerServiceConnection)
        }
        super.onStop()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == CODE.PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                presenter.load()
            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun setupView() {
        recyclerPlayList.run {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = songAdapter
        }
    }

    private fun initListener() {
        seekDuration.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, p1: Int, p2: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.run { playerService.seekTo(progress) }
            }
        })

        songAdapter.setItemClickListener {
            playerService.setSong(it.id)
        }

        btnPlay.setOnClickListener {
            playerService.playOrPause()
        }

        btnNext.setOnClickListener {
            playerService.next()
        }

        btnPrevious.setOnClickListener {
            playerService.previous()
        }
    }

    override fun updateSongDetail(song: Song) {
        song.run {
            tvPlayTitle.text = title
            tvPlayArtist.text = artist
            playerImage.setImageURI(Uri.parse(thumbnail))
            seekDuration.max = duration.toInt()
            tvDuration.text = TimeUtils.toTimer(duration)

            val swatch = ImageUtils.getMostPopulousSwatch(
                this@MainActivity,
                Uri.parse(thumbnail)
            )

            swatch?.run {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = rgb
                frameBackground.setBackgroundColor(rgb)

                bodyTextColor.let {
                    tvPlayTitle.setTextColor(it)
                    tvPlayArtist.setTextColor(it)
                    btnPlay.setColorFilter(it)
                    btnNext.setColorFilter(it)
                    btnPrevious.setColorFilter(it)
                    tvCurrentTime.setTextColor(it)
                    tvDuration.setTextColor(it)
                    seekDuration.progressDrawable.colorFilter =
                        BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                            it,
                            BlendModeCompat.SRC_ATOP
                        )
                }
            }
        }
    }

    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                CODE.PERMISSION_REQUEST
            )
        } else {
            presenter.load()
        }
    }

    override fun updateRecyclerView(list: List<Song>) {
        songAdapter.submitList(list)
    }
}
