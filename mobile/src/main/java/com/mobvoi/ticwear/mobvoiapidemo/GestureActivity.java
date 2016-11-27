package com.mobvoi.ticwear.mobvoiapidemo;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mobvoi.android.common.ConnectionResult;
import com.mobvoi.android.common.api.MobvoiApiClient;
import com.mobvoi.android.common.api.ResultCallback;
import com.mobvoi.android.wearable.MessageApi;
import com.mobvoi.android.wearable.MessageEvent;
import com.mobvoi.android.wearable.Node;
import com.mobvoi.android.wearable.NodeApi;
import com.mobvoi.android.wearable.Wearable;

import com.mobvoi.ticwear.mobvoiapidemo.AndroidAudioRecorder2;

import java.util.ArrayList;

import cafe.adriel.androidaudiorecorder.model.AudioChannel;
import cafe.adriel.androidaudiorecorder.model.AudioSampleRate;
import cafe.adriel.androidaudiorecorder.model.AudioSource;

public class GestureActivity extends AppCompatActivity implements MobvoiApiClient.OnConnectionFailedListener,
        MobvoiApiClient.ConnectionCallbacks, NodeApi.NodeListener, MessageApi.MessageListener {

    private static final String TAG = "GestureActivity";
    private static final String START_ACTIVITY_PATH = "/start-gesture";
    private static final String DEFAULT_NODE = "default_node";

    private Button mStartGestureBtn;
    private TextView mGesTv;
    private MobvoiApiClient mMobvoiApiClient;
    private Handler mHandler;

    public AndroidAudioRecorder2 recorder;

    public static ArrayList fileIndexList = new ArrayList();
    public static ArrayList fileIndexSent = new ArrayList();

    public static String basePath = Environment.getExternalStorageDirectory().getPath();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gesture);
        mStartGestureBtn = (Button) findViewById(R.id.start_gesture);
        mGesTv = (TextView) findViewById(R.id.gesture_text);
        Util.requestPermission(this, Manifest.permission.RECORD_AUDIO);
        Util.requestPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        Util.requestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        mStartGestureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Button Click");
                Wearable.MessageApi.sendMessage(
                        mMobvoiApiClient, DEFAULT_NODE, START_ACTIVITY_PATH, new byte[0])
                        .setResultCallback(
                                new ResultCallback<MessageApi.SendMessageResult>() {
                                    @Override
                                    public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                                        if (!sendMessageResult.getStatus().isSuccess()) {
                                            Log.e(TAG, "Failed to send message with status code: "
                                                    + sendMessageResult.getStatus().getStatusCode());
                                        } else {
                                            Log.d(TAG, "Success");
                                        }
                                    }
                                }
                        );
            }
        });

        mMobvoiApiClient = new MobvoiApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mHandler = new Handler();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMobvoiApiClient.connect();
        Log.d(TAG, "onResume...");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.MessageApi.removeListener(mMobvoiApiClient, this);
        Wearable.NodeApi.removeListener(mMobvoiApiClient, this);
        mMobvoiApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Wearable.MessageApi.addListener(mMobvoiApiClient, this);
        Wearable.NodeApi.addListener(mMobvoiApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int cause) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e(TAG, "onConnectionFailed(): Failed to connect, with result: " + result);
    }

    @Override
    public void onMessageReceived(MessageEvent event) {
        if (event.getPath().equals("Gestures")) {
            Log.d(TAG, "onMessageReceived: " + event);
            byte[] data = event.getData();
            final String datas = new String(data);
            mHandler.post(new Runnable() {
                @Override
                public void run() {

                    //mGesTv.setText(getString(R.string.gesture_now) + datas);
                    if (datas.equals("两次翻腕")){
                        recordAudio();
                    }
                }
            });
        }
    }

    @Override
    public void onPeerConnected(Node node) {
        Log.d(TAG, "onPeerConncted:");
    }

    @Override
    public void onPeerDisconnected(Node node) {
        Log.d(TAG, "onPeerDisconnected:");
    }

    public void recordAudio() {
        //String filePath = Environment.getExternalStorageDirectory().getPath() + "/recorded_audio.wav";
        int color = getResources().getColor(R.color.colorPrimaryDark);
        int requestCode = 0;

        int FileCount = GestureActivity.fileIndexList.size();
        String filePath = Environment.getExternalStorageDirectory().getPath();
        if ( FileCount > 0){
            int lastIndex = (int)GestureActivity.fileIndexList.get(FileCount-1);
            int newIndex = lastIndex + 1;
            filePath = filePath + "/recorded_audio"+ newIndex + ".wav";
        }else{
            filePath = filePath + "/recorded_audio0.wav";
        }

        this.recorder = AndroidAudioRecorder2.with(this);//new recorder

        this.recorder.setFilePath(filePath)
                .setColor(color)
                .setRequestCode(requestCode)

                // Optional
                .setSource(AudioSource.MIC)
                .setChannel(AudioChannel.MONO)
                .setSampleRate(AudioSampleRate.HZ_16000)
                .setAutoStart(true)
                .setKeepDisplayOn(true)

                // Start recording
                .record();
    }
}