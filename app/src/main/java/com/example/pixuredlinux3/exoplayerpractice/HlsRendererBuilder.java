package com.example.pixuredlinux3.exoplayerpractice;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaCodec;
import android.os.Handler;
import android.view.SurfaceView;
import android.view.View;

import com.google.android.exoplayer.DefaultLoadControl;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.LoadControl;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecSelector;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.chunk.Format;
import com.google.android.exoplayer.extractor.mp4.Track;
import com.google.android.exoplayer.hls.DefaultHlsTrackSelector;
import com.google.android.exoplayer.hls.HlsChunkSource;
import com.google.android.exoplayer.hls.HlsPlaylist;
import com.google.android.exoplayer.hls.HlsPlaylistParser;
import com.google.android.exoplayer.hls.HlsSampleSource;
import com.google.android.exoplayer.hls.PtsTimestampAdjusterProvider;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;
import com.google.android.exoplayer.util.ManifestFetcher;
import com.google.android.exoplayer.util.PlayerControl;

import java.io.IOException;

/**
 * Created by pixuredlinux3 on 6/23/16.
 */
public class HlsRendererBuilder implements ManifestFetcher.ManifestCallback<HlsPlaylist>,
        ExoPlayer.Listener,HlsSampleSource.EventListener,AudioManager.OnAudioFocusChangeListener{

    private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
    private static final int MAIN_BUFFER_SEGMENTS = 254;
    private static final int AUDIO_BUFFER_SEGMENTS = 54;
    private static final int TEXT_BUFFER_SEGMENTS = 2;
    public static final int TYPE_VIDEO = 0;

    private SurfaceView surface;
    private final String url;
    private final Context context;
    private String userAgent;
    private Handler mainHandler;
    private AudioManager am;
    private MediaCodecVideoTrackRenderer videoRenderer;
    private MediaCodecAudioTrackRenderer audioRenderer;
    private AudioManager audioManager;
    private final ManifestFetcher<HlsPlaylist> playlistFetcher;
    private final ExoPlayer exoPlayer;

    public HlsRendererBuilder(SurfaceView surface, ExoPlayer exoPlayer, Context context, String userAgent, String url) {
        this.surface = surface;
        this.exoPlayer = exoPlayer;
        this.context = context;
        this.userAgent = userAgent;
        this.url = url;

        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        HlsPlaylistParser parser = new HlsPlaylistParser();
        playlistFetcher = new ManifestFetcher<>(url, new DefaultUriDataSource(context, userAgent),
                parser);
        mainHandler = new Handler();
        playlistFetcher.singleLoad(mainHandler.getLooper(), this);
    }

    @Override
    public void onSingleManifest(HlsPlaylist manifest) {
        LoadControl loadControl = new DefaultLoadControl(new DefaultAllocator(BUFFER_SEGMENT_SIZE));
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        PtsTimestampAdjusterProvider timestampAdjusterProvider = new PtsTimestampAdjusterProvider();

        DataSource dataSource = new DefaultUriDataSource(context, bandwidthMeter, userAgent);
        HlsChunkSource chunkSource = new HlsChunkSource(true /* isMaster */, dataSource, manifest,
                DefaultHlsTrackSelector.newDefaultInstance(context), bandwidthMeter,
                timestampAdjusterProvider, HlsChunkSource.ADAPTIVE_MODE_SPLICE);
        HlsSampleSource sampleSource = new HlsSampleSource(chunkSource, loadControl,
                MAIN_BUFFER_SEGMENTS * BUFFER_SEGMENT_SIZE, mainHandler, this, TYPE_VIDEO);
        MediaCodecVideoTrackRenderer videoRenderer = new MediaCodecVideoTrackRenderer(context, sampleSource,
                MediaCodecSelector.DEFAULT, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT);
        MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource,
                MediaCodecSelector.DEFAULT);

        this.videoRenderer = videoRenderer;
        this.audioRenderer = audioRenderer;

        pushSurface(false);
        exoPlayer.prepare(videoRenderer,audioRenderer);

        if (requestFocus())
            exoPlayer.setPlayWhenReady(true);
    }
    @Override
    public void onSingleManifestError(IOException e) {

    }

    public boolean requestFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager.requestAudioFocus(HlsRendererBuilder.this, AudioManager.STREAM_MUSIC,
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

    public MediaCodecVideoTrackRenderer getVideoRenderer(){
        return videoRenderer;
    }
    public MediaCodecAudioTrackRenderer getAudioRenderer(){
        return audioRenderer;
    }

    @Override
    public void onLoadStarted(int sourceId, long length, int type, int trigger, Format format, long mediaStartTimeMs, long mediaEndTimeMs) {

    }

    @Override
    public void onLoadCompleted(int sourceId, long bytesLoaded, int type, int trigger, Format format, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs) {

    }

    @Override
    public void onLoadCanceled(int sourceId, long bytesLoaded) {

    }

    @Override
    public void onLoadError(int sourceId, IOException e) {

    }

    @Override
    public void onUpstreamDiscarded(int sourceId, long mediaStartTimeMs, long mediaEndTimeMs) {

    }

    @Override
    public void onDownstreamFormatChanged(int sourceId, Format format, int trigger, long mediaTimeMs) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

    }

    @Override
    public void onPlayWhenReadyCommitted() {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onAudioFocusChange(int focusChange) {

    }
}
