package com.mobvoi.ticwear.mobvoiapidemo;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cleveroad.audiovisualization.DbmHandler;
import com.cleveroad.audiovisualization.GLAudioVisualizationView;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import cafe.adriel.androidaudiorecorder.*;
import cafe.adriel.androidaudiorecorder.model.AudioChannel;
import cafe.adriel.androidaudiorecorder.model.AudioSampleRate;
import cafe.adriel.androidaudiorecorder.model.AudioSource;
import omrecorder.AudioChunk;
import omrecorder.OmRecorder;
import omrecorder.PullTransport;
import omrecorder.Recorder;

import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cafe.adriel.androidaudiorecorder.Util;
import cafe.adriel.androidaudiorecorder.VisualizerHandler;
import cafe.adriel.androidaudiorecorder.R.dimen;
import cafe.adriel.androidaudiorecorder.R.drawable;
import cafe.adriel.androidaudiorecorder.R.id;
import cafe.adriel.androidaudiorecorder.R.layout;
import cafe.adriel.androidaudiorecorder.R.menu;
import cafe.adriel.androidaudiorecorder.R.string;
import cafe.adriel.androidaudiorecorder.model.AudioChannel;
import cafe.adriel.androidaudiorecorder.model.AudioSampleRate;
import cafe.adriel.androidaudiorecorder.model.AudioSource;
import com.cleveroad.audiovisualization.GLAudioVisualizationView;
import com.cleveroad.audiovisualization.DbmHandler.Factory;
import com.cleveroad.audiovisualization.GLAudioVisualizationView.Builder;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import omrecorder.AudioChunk;
import omrecorder.OmRecorder;
import omrecorder.Recorder;
import omrecorder.PullTransport.Default;
import omrecorder.PullTransport.OnAudioChunkPulledListener;

public class AudioRecorderActivity extends AppCompatActivity implements PullTransport.OnAudioChunkPulledListener, MediaPlayer.OnCompletionListener {
    public String filePath;
    public AudioSource source;
    public AudioChannel channel;
    public AudioSampleRate sampleRate;
    public int color;
    public boolean autoStart;
    public boolean keepDisplayOn;
    public MediaPlayer player;
    public Recorder recorder;
    public VisualizerHandler visualizerHandler;
    public Timer timer;
    public MenuItem saveMenuItem;
    public int recorderSecondsElapsed;
    public int playerSecondsElapsed;
    public boolean isRecording;
    public RelativeLayout contentLayout;
    public GLAudioVisualizationView visualizerView;
    public TextView statusView;
    public TextView timerView;
    public ImageButton restartView;
    public ImageButton recordView;
    public ImageButton playView;

    public AudioRecorderActivity() {
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(cafe.adriel.androidaudiorecorder.R.layout.aar_activity_audio_recorder);
        if(savedInstanceState != null) {
            this.filePath = savedInstanceState.getString("filePath");
            this.source = (AudioSource)savedInstanceState.getSerializable("source");
            this.channel = (AudioChannel)savedInstanceState.getSerializable("channel");
            this.sampleRate = (AudioSampleRate)savedInstanceState.getSerializable("sampleRate");
            this.color = savedInstanceState.getInt("color");
            this.autoStart = savedInstanceState.getBoolean("autoStart");
            this.keepDisplayOn = savedInstanceState.getBoolean("keepDisplayOn");
        } else {
            this.filePath = this.getIntent().getStringExtra("filePath");
            this.source = (AudioSource)this.getIntent().getSerializableExtra("source");
            this.channel = (AudioChannel)this.getIntent().getSerializableExtra("channel");
            this.sampleRate = (AudioSampleRate)this.getIntent().getSerializableExtra("sampleRate");
            this.color = this.getIntent().getIntExtra("color", Color.BLACK);
            this.autoStart = this.getIntent().getBooleanExtra("autoStart", false);
            this.keepDisplayOn = this.getIntent().getBooleanExtra("keepDisplayOn", false);
        }

        if(this.keepDisplayOn) {
            this.getWindow().addFlags(128);
        }

        if(this.getSupportActionBar() != null) {
            this.getSupportActionBar().setHomeButtonEnabled(true);
            this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            this.getSupportActionBar().setDisplayShowTitleEnabled(false);
            this.getSupportActionBar().setElevation(0.0F);
            this.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(cafe.adriel.androidaudiorecorder.Util.getDarkerColor(this.color)));
            this.getSupportActionBar().setHomeAsUpIndicator(ContextCompat.getDrawable(this, cafe.adriel.androidaudiorecorder.R.drawable.aar_ic_clear));
        }

        this.visualizerView = ((GLAudioVisualizationView.Builder)((GLAudioVisualizationView.Builder)(new GLAudioVisualizationView.Builder(this)).setLayersCount(1).setWavesCount(6).setWavesHeight(cafe.adriel.androidaudiorecorder.R.dimen.aar_wave_height).setWavesFooterHeight(cafe.adriel.androidaudiorecorder.R.dimen.aar_footer_height).setBubblesPerLayer(20).setBubblesSize(cafe.adriel.androidaudiorecorder.R.dimen.aar_bubble_size).setBubblesRandomizeSize(true).setBackgroundColor(cafe.adriel.androidaudiorecorder.Util.getDarkerColor(this.color))).setLayerColors(new int[]{this.color})).build();
        this.contentLayout = (RelativeLayout)this.findViewById(cafe.adriel.androidaudiorecorder.R.id.content);
        this.statusView = (TextView)this.findViewById(cafe.adriel.androidaudiorecorder.R.id.status);
        this.timerView = (TextView)this.findViewById(cafe.adriel.androidaudiorecorder.R.id.timer);
        this.restartView = (ImageButton)this.findViewById(cafe.adriel.androidaudiorecorder.R.id.restart);
        this.recordView = (ImageButton)this.findViewById(cafe.adriel.androidaudiorecorder.R.id.record);
        this.playView = (ImageButton)this.findViewById(cafe.adriel.androidaudiorecorder.R.id.play);
        this.contentLayout.setBackgroundColor(cafe.adriel.androidaudiorecorder.Util.getDarkerColor(this.color));
        this.contentLayout.addView(this.visualizerView, 0);
        this.restartView.setVisibility(View.INVISIBLE);
        this.playView.setVisibility(View.INVISIBLE);
        if(cafe.adriel.androidaudiorecorder.Util.isBrightColor(this.color)) {
            ContextCompat.getDrawable(this, cafe.adriel.androidaudiorecorder.R.drawable.aar_ic_clear).setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
            ContextCompat.getDrawable(this, cafe.adriel.androidaudiorecorder.R.drawable.aar_ic_check).setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
            this.statusView.setTextColor(Color.BLACK);
            this.timerView.setTextColor(Color.BLACK);
            this.restartView.setColorFilter(Color.BLACK);
            this.recordView.setColorFilter(Color.BLACK);
            this.playView.setColorFilter(Color.BLACK);
        }

    }

    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if(this.autoStart && !this.isRecording) {
            this.toggleRecording((View)null);
        }

    }

    public void onResume() {
        super.onResume();

        try {
            this.visualizerView.onResume();
        } catch (Exception var2) {
            ;
        }

    }

    protected void onPause() {
        this.restartRecording((View)null);

        try {
            this.visualizerView.onPause();
        } catch (Exception var2) {
            ;
        }

        super.onPause();
    }

    protected void onDestroy() {
        this.restartRecording((View)null);
        this.setResult(0);

        try {
            this.visualizerView.release();
        } catch (Exception var2) {
            ;
        }

        super.onDestroy();
    }

    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("filePath", this.filePath);
        outState.putInt("color", this.color);
        super.onSaveInstanceState(outState);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.aar_audio_recorder, menu);
        this.saveMenuItem = menu.findItem(cafe.adriel.androidaudiorecorder.R.id.action_save);
        this.saveMenuItem.setIcon(ContextCompat.getDrawable(this, cafe.adriel.androidaudiorecorder.R.drawable.aar_ic_check));
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if(i == 16908332) {
            this.finish();
        } else if(i == cafe.adriel.androidaudiorecorder.R.id.action_save) {
            this.selectAudio();
        }

        return super.onOptionsItemSelected(item);
    }

    public void onAudioChunkPulled(AudioChunk audioChunk) {
        float amplitude = this.isRecording?(float)audioChunk.maxAmplitude():0.0F;
        this.visualizerHandler.onDataReceived(Float.valueOf(amplitude));
    }

    public void onCompletion(MediaPlayer mediaPlayer) {
        this.stopPlaying();
    }

    public void selectAudio() {
        this.stopRecording();
        this.setResult(-1);
        this.finish();
    }

    public void toggleRecording(View v) {
        this.stopPlaying();
        cafe.adriel.androidaudiorecorder.Util.wait(100, new Runnable() {
            public void run() {
                if(AudioRecorderActivity.this.isRecording) {
                    AudioRecorderActivity.this.pauseRecording();
                } else {
                    AudioRecorderActivity.this.resumeRecording();
                }

            }
        });
    }

    public void togglePlaying(View v) {
        this.pauseRecording();
        cafe.adriel.androidaudiorecorder.Util.wait(100, new Runnable() {
            public void run() {
                if(AudioRecorderActivity.this.isPlaying()) {
                    AudioRecorderActivity.this.stopPlaying();
                } else {
                    AudioRecorderActivity.this.startPlaying();
                }

            }
        });
    }

    public void restartRecording(View v) {
        if(this.isRecording) {
            this.stopRecording();
        } else if(this.isPlaying()) {
            this.stopPlaying();
        } else {
            this.visualizerHandler = new VisualizerHandler();
            this.visualizerView.linkTo(this.visualizerHandler);
            this.visualizerView.release();
            if(this.visualizerHandler != null) {
                this.visualizerHandler.stop();
            }
        }

        this.saveMenuItem.setVisible(false);
        this.statusView.setVisibility(View.INVISIBLE);
        this.restartView.setVisibility(View.INVISIBLE);
        this.playView.setVisibility(View.INVISIBLE);
        this.recordView.setImageResource(cafe.adriel.androidaudiorecorder.R.drawable.aar_ic_rec);
        this.timerView.setText("00:00:00");
        this.recorderSecondsElapsed = 0;
        this.playerSecondsElapsed = 0;
    }

    public void resumeRecording() {
        this.isRecording = true;
        this.saveMenuItem.setVisible(false);
        this.statusView.setText(cafe.adriel.androidaudiorecorder.R.string.aar_recording);
        this.statusView.setVisibility(View.VISIBLE);
        this.restartView.setVisibility(View.INVISIBLE);
        this.playView.setVisibility(View.INVISIBLE);
        this.recordView.setImageResource(cafe.adriel.androidaudiorecorder.R.drawable.aar_ic_pause);
        this.playView.setImageResource(cafe.adriel.androidaudiorecorder.R.drawable.aar_ic_play);
        this.visualizerHandler = new VisualizerHandler();
        this.visualizerView.linkTo(this.visualizerHandler);
        if(this.recorder == null) {
            this.timerView.setText("00:00:00");
            this.recorder = OmRecorder.wav(new PullTransport.Default(cafe.adriel.androidaudiorecorder.Util.getMic(this.source, this.channel, this.sampleRate), this), new File(this.filePath));
        }

        this.recorder.resumeRecording();
        this.startTimer();
    }

    public void pauseRecording() {
        this.isRecording = false;
        if(!this.isFinishing()) {
            this.saveMenuItem.setVisible(true);
        }

        this.statusView.setText(cafe.adriel.androidaudiorecorder.R.string.aar_paused);
        this.statusView.setVisibility(View.VISIBLE);
        this.restartView.setVisibility(View.VISIBLE);
        this.playView.setVisibility(View.VISIBLE);
        this.recordView.setImageResource(cafe.adriel.androidaudiorecorder.R.drawable.aar_ic_rec);
        this.playView.setImageResource(cafe.adriel.androidaudiorecorder.R.drawable.aar_ic_play);
        this.visualizerView.release();
        if(this.visualizerHandler != null) {
            this.visualizerHandler.stop();
        }

        if(this.recorder != null) {
            this.recorder.pauseRecording();
        }

        this.stopTimer();
    }

    public void stopRecording() {
        this.visualizerView.release();
        if(this.visualizerHandler != null) {
            this.visualizerHandler.stop();
        }

        this.recorderSecondsElapsed = 0;
        if(this.recorder != null) {
            this.recorder.stopRecording();
            this.recorder = null;
        }

        this.stopTimer();
    }

    public void stopRecording2() {
        //this.visualizerView.release();
        //if(this.visualizerHandler != null) {
        //    this.visualizerHandler.stop();
        //}

        //this.recorderSecondsElapsed = 0;
        if(this.recorder != null) {
            this.recorder.stopRecording();
            this.recorder = null;
        }
    }

    public void startPlaying() {
        try {
            this.stopRecording();
            this.player = new MediaPlayer();
            this.player.setDataSource(this.filePath);
            this.player.prepare();
            this.player.start();
            this.visualizerView.linkTo(DbmHandler.Factory.newVisualizerHandler(this, this.player));
            this.visualizerView.post(new Runnable() {
                public void run() {
                    AudioRecorderActivity.this.player.setOnCompletionListener(AudioRecorderActivity.this);
                }
            });
            this.timerView.setText("00:00:00");
            this.statusView.setText(cafe.adriel.androidaudiorecorder.R.string.aar_playing);
            this.statusView.setVisibility(View.VISIBLE);
            this.playView.setImageResource(cafe.adriel.androidaudiorecorder.R.drawable.aar_ic_stop);
            this.playerSecondsElapsed = 0;
            this.startTimer();
        } catch (Exception var2) {
            var2.printStackTrace();
        }

    }

    public void stopPlaying() {
        this.statusView.setText("");
        this.statusView.setVisibility(View.INVISIBLE);
        this.playView.setImageResource(cafe.adriel.androidaudiorecorder.R.drawable.aar_ic_play);
        this.visualizerView.release();
        if(this.visualizerHandler != null) {
            this.visualizerHandler.stop();
        }

        if(this.player != null) {
            try {
                this.player.stop();
                this.player.reset();
            } catch (Exception var2) {
                ;
            }
        }

        this.stopTimer();
    }

    public boolean isPlaying() {
        try {
            return this.player != null && this.player.isPlaying() && !this.isRecording;
        } catch (Exception var2) {
            return false;
        }
    }

    public void startTimer() {
        this.stopTimer();
        this.timer = new Timer();
        this.timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                AudioRecorderActivity.this.updateTimer();
            }
        }, 0L, 1000L);
    }

    public void stopTimer() {
        if(this.timer != null) {
            this.timer.cancel();
            this.timer.purge();
            this.timer = null;
        }

    }

    public void updateTimer() {
        this.runOnUiThread(new Runnable() {
            public void run() {
                if(AudioRecorderActivity.this.isRecording) {
                    recorderSecondsElapsed++;
                    AudioRecorderActivity.this.timerView.setText(cafe.adriel.androidaudiorecorder.Util.formatSeconds(AudioRecorderActivity.this.recorderSecondsElapsed));
                } else if(AudioRecorderActivity.this.isPlaying()) {
                    playerSecondsElapsed++;
                    AudioRecorderActivity.this.timerView.setText(cafe.adriel.androidaudiorecorder.Util.formatSeconds(AudioRecorderActivity.this.playerSecondsElapsed));
                }

            }
        });
    }
}

