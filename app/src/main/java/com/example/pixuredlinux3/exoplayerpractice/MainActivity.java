package com.example.pixuredlinux3.exoplayerpractice;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.util.PlayerControl;
import com.google.android.exoplayer.util.Util;

public class MainActivity extends AppCompatActivity implements AudioManager.OnAudioFocusChangeListener {
    private Button play;
    private Button pause;

    private SurfaceView surface;
    private ExoPlayer exoPlayer;
    private PlayerControl playerControl;
    private AudioManager audioManager;
    private String userAgent;
    private String video_url;

    private MediaCodecVideoTrackRenderer videoRenderer;
    private MediaCodecAudioTrackRenderer audioRenderer;
    private HlsRendererBuilder hls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        surface = (SurfaceView) findViewById(R.id.surface_view);
        play = (Button) findViewById(R.id.btn_play);
        pause = (Button) findViewById(R.id.btn_pause);

        exoPlayer = ExoPlayer.Factory.newInstance(2);
        playerControl = new PlayerControl(exoPlayer);
        userAgent = Util.getUserAgent(this, "MainActivity");
        audioManager = (AudioManager) this.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        video_url = "http://playertest.longtailvideo.com/adaptive/bbbfull/bbbfull.m3u8";
        //video_url = "https://www.youtube.com/watch?v=kpPBHC_Kyks";
        hls = new HlsRendererBuilder(surface, exoPlayer, this, userAgent, video_url);
        videoRenderer = hls.getVideoRenderer();
        audioRenderer = hls.getAudioRenderer();

        /*pushSurface(false);
        exoPlayer.prepare(videoRenderer,audioRenderer);

        if (requestFocus())
            exoPlayer.setPlayWhenReady(true);*/

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Started", "");
                playerControl.start();
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Paused", "");
                playerControl.pause();
            }
        });
    }

    public boolean requestFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager.requestAudioFocus(MainActivity.this, AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN);
    }

    private void pushSurface(boolean blockForSurfacePush) {

        if (videoRenderer == null) {return;}
        if (blockForSurfacePush) {
            exoPlayer.blockingSendMessage(
                    videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surface.getHolder().getSurface());
        } else {
            exoPlayer.sendMessage(
                    videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surface.getHolder().getSurface());
        }
    }


    @Override
    public void onAudioFocusChange(int focusChange) {

    }
}
