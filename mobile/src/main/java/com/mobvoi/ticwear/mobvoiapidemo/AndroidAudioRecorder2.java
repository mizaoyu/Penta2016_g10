//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.mobvoi.ticwear.mobvoiapidemo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Environment;
import com.mobvoi.ticwear.mobvoiapidemo.AudioRecorderActivity2;
import cafe.adriel.androidaudiorecorder.model.AudioChannel;
import cafe.adriel.androidaudiorecorder.model.AudioSampleRate;
import cafe.adriel.androidaudiorecorder.model.AudioSource;

public class AndroidAudioRecorder2 {
    protected static final String EXTRA_FILE_PATH = "filePath";
    protected static final String EXTRA_COLOR = "color";
    protected static final String EXTRA_SOURCE = "source";
    protected static final String EXTRA_CHANNEL = "channel";
    protected static final String EXTRA_SAMPLE_RATE = "sampleRate";
    protected static final String EXTRA_AUTO_START = "autoStart";
    protected static final String EXTRA_KEEP_DISPLAY_ON = "keepDisplayOn";
    private Activity activity;
    private String filePath = Environment.getExternalStorageDirectory().getPath() + "/recorded_audio.wav";
    private AudioSource source;
    private AudioChannel channel;
    private AudioSampleRate sampleRate;
    private int color;
    private int requestCode;
    private boolean autoStart;
    private boolean keepDisplayOn;

    private AndroidAudioRecorder2(Activity activity) {
        this.source = AudioSource.MIC;
        this.channel = AudioChannel.STEREO;
        this.sampleRate = AudioSampleRate.HZ_44100;
        this.color = Color.parseColor("#546E7A");
        this.requestCode = 0;
        this.autoStart = false;
        this.keepDisplayOn = false;
        this.activity = activity;
    }

    public static AndroidAudioRecorder2 with(Activity activity) {
        return new AndroidAudioRecorder2(activity);
    }

    public AndroidAudioRecorder2 setFilePath(String filePath) {
        this.filePath = filePath;
        return this;
    }

    public AndroidAudioRecorder2 setColor(int color) {
        this.color = color;
        return this;
    }

    public AndroidAudioRecorder2 setRequestCode(int requestCode) {
        this.requestCode = requestCode;
        return this;
    }

    public AndroidAudioRecorder2 setSource(AudioSource source) {
        this.source = source;
        return this;
    }

    public AndroidAudioRecorder2 setChannel(AudioChannel channel) {
        this.channel = channel;
        return this;
    }

    public AndroidAudioRecorder2 setSampleRate(AudioSampleRate sampleRate) {
        this.sampleRate = sampleRate;
        return this;
    }

    public AndroidAudioRecorder2 setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
        return this;
    }

    public AndroidAudioRecorder2 setKeepDisplayOn(boolean keepDisplayOn) {
        this.keepDisplayOn = keepDisplayOn;
        return this;
    }

    public void record() {
        Intent intent = new Intent(this.activity, AudioRecorderActivity2.class);
        intent.putExtra("filePath", this.filePath);
        intent.putExtra("color", this.color);
        intent.putExtra("source", this.source);
        intent.putExtra("channel", this.channel);
        intent.putExtra("sampleRate", this.sampleRate);
        intent.putExtra("autoStart", this.autoStart);
        intent.putExtra("keepDisplayOn", this.keepDisplayOn);
        this.activity.startActivityForResult(intent, this.requestCode);
    }
}
