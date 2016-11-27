package com.mobvoi.ticwear.mobvoiapidemo;

/**
 * Created by Misky on 2016/11/26.
 */
import android.Manifest;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import omrecorder.AudioChunk;
import omrecorder.OmRecorder;
import omrecorder.Recorder;
import omrecorder.PullTransport.Default;
import omrecorder.PullTransport.OnAudioChunkPulledListener;

import java.net.URI;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.mobvoi.android.common.ConnectionResult;
import com.mobvoi.android.common.api.MobvoiApiClient;
import com.mobvoi.android.common.api.ResultCallback;
import com.mobvoi.android.wearable.MessageApi;
import com.mobvoi.android.wearable.MessageEvent;
import com.mobvoi.android.wearable.Node;
import com.mobvoi.android.wearable.NodeApi;
import com.mobvoi.android.wearable.Wearable;

import com.mobvoi.ticwear.mobvoiapidemo.AudioRecorderActivity;

import omrecorder.PullTransport;

import static android.widget.Toast.LENGTH_SHORT;

public class AudioRecorderActivity2 extends AudioRecorderActivity implements MobvoiApiClient.OnConnectionFailedListener,
        MobvoiApiClient.ConnectionCallbacks, NodeApi.NodeListener, MessageApi.MessageListener{

    private static final String TAG = "GestureActivity";
    private static final String START_ACTIVITY_PATH = "/start-gesture";
    private static final String DEFAULT_NODE = "default_node";

    private Button mStartGestureBtn;
    private TextView mGesTv;
    private MobvoiApiClient mMobvoiApiClient;
    private Handler mHandler;

    public Menu menu;
    public Timer timer2;
    public boolean recording = true;
    public boolean justOnCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recording = true;
        justOnCreate = true;
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        mMobvoiApiClient = new MobvoiApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mHandler = new Handler();
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
        timer2 = new Timer();
        timer2.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (recording && !justOnCreate){
                    //停止录音
                    AudioRecorderActivity2.this.stopRecording2();
                    AudioRecorderActivity2.this.setResult(-1);
                    //
                    int thisFileIndex = GestureActivity.fileIndexList.size();
                    GestureActivity.fileIndexList.add(thisFileIndex);
                    recording = false;
                    GestureActivity.fileIndexSent.add(thisFileIndex);

                    int newIndex = thisFileIndex + 1;
                    AudioRecorderActivity2.this.filePath = Environment.getExternalStorageDirectory().getPath() + "/recorded_audio"+ newIndex + ".wav";
                    AudioRecorderActivity2.this.recorder = OmRecorder.wav(new Default(Util.getMic(AudioRecorderActivity2.this.source, AudioRecorderActivity2.this.channel, AudioRecorderActivity2.this.sampleRate), AudioRecorderActivity2.this), new File(AudioRecorderActivity2.this.filePath));
                    AudioRecorderActivity2.this.recorder.resumeRecording();
                    recording = true;
                    sendAudioToServer(thisFileIndex);
                    //Toast.makeText(AudioRecorderActivity2.this, "Audio recorded"+thisFileIndex+"successfully!", Toast.LENGTH_SHORT).show();

                }else{
                    //开始录音
                }
                justOnCreate = false;
            }
        }, 0, 10000);
    }

    @Override
    public void onResume() {
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
                        AudioRecorderActivity2.this.toggleRecording((View)null);
                        MenuItem saveMenuItem = AudioRecorderActivity2.this.menu.findItem(id.action_save);
                        int thisFileIndex = GestureActivity.fileIndexList.size();
                        GestureActivity.fileIndexList.add(thisFileIndex);
                        recording = false;
                        Toast.makeText(AudioRecorderActivity2.this, "Audio recorded successfully!", Toast.LENGTH_SHORT).show();
                        AudioRecorderActivity2.this.onOptionsItemSelected(saveMenuItem);

                        Toast.makeText(AudioRecorderActivity2.this,"beforeSent",LENGTH_SHORT).show();
                        //传给服务器，传成功了给GestureActivity.fileIndexSentjia
                        sendAudioToServer(thisFileIndex);

                        GestureActivity.fileIndexSent.add(thisFileIndex);
                        Toast.makeText(AudioRecorderActivity2.this,"sent" + GestureActivity.fileIndexSent.size(),LENGTH_SHORT).show();
                        /*HttpClient httpclient = HttpClients.createDefault();

                        try
                        {
                            URIBuilder builder = new URIBuilder("https://api.projectoxford.ai/spid/v1.0/identify?identificationProfileIds=111f427c-3791-468f-b709-fcef7660fff9,111f427c-3791-468f-b709-fcef7660fff9,111f427c-3791-468f-b709-fcef7660fff9");

                            builder.setParameter("shortAudio", "true");
                            String location = GestureActivity.basePath + "/recorded_audio"+ thisFileIndex + ".wav";
                            File file = new File("music.wav");
                            byte[] data = new byte[(int) file.length()];
                            FileInputStream in = new FileInputStream(file);
                            in.read(data);
                            in.close();

                            URI uri = builder.build();
                            HttpPost request = new HttpPost(uri);
                            request.setHeader("Content-Type", "application/octet-stream");
                            request.setHeader("Ocp-Apim-Subscription-Key", "{subscription key}");


                            // Request body
                            StringEntity reqEntity = new StringEntity("{body}");
                            request.setEntity(reqEntity);

                            HttpResponse response = httpclient.execute(request);
                            HttpEntity entity = response.getEntity();

                            if (entity != null)
                            {
                                System.out.println(EntityUtils.toString(entity));
                            }
                        }
                        catch (Exception e)
                        {
                            System.out.println(e.getMessage());
                        }*/
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onPeerConnected(Node node) {
        Log.d(TAG, "onPeerConncted:");
    }

    @Override
    public void onPeerDisconnected(Node node) {
        Log.d(TAG, "onPeerDisconnected:");
    }

    /*@Override
    public void finish(){

    }*/

    public static String postwav(String actionUrl, Map<String, String> params, Map<String, File> files) throws IOException {
        String BOUNDARY = java.util.UUID.randomUUID().toString();
        String PREFIX = "--", LINEND = "\r\n";
        String MULTIPART_FROM_DATA = "multipart/form-data";
        String CHARSET = "UTF-8";
        URL uri = new URL(actionUrl);
        HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
        conn.setReadTimeout(5 * 1000);
        conn.setDoInput(true);//  允许输入
        conn.setDoOutput(true);//  允许输出
        conn.setUseCaches(false);
        conn.setRequestMethod("POST");  //  Post方式
        conn.setRequestProperty("connection", "keep-alive");
        conn.setRequestProperty("Charsert", "UTF-8");
        conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA
                + ";boundary=" + BOUNDARY);
      /*  new Thread(){
            @Override
            public void run(){

            }
        }*/
        conn.connect();
        //  首先组拼文本类型的参数
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            sb.append(PREFIX);
            sb.append(BOUNDARY);
            sb.append(LINEND);
            sb.append("Content-Disposition:  form-data;  name=\""
                    + entry.getKey() + "\"" + LINEND);
            sb.append("Content-Type:  text/plain;  charset=" + CHARSET + LINEND);
            sb.append("Content-Transfer-Encoding:  8bit" + LINEND);
            sb.append(LINEND);
            sb.append(entry.getValue());
            sb.append(LINEND);
        }
        DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
        outStream.write(sb.toString().getBytes());

        //  发送文件数据
        if (files != null)
            for (Map.Entry<String, File> file : files.entrySet()) {
                StringBuilder sb1 = new StringBuilder();
                sb1.append(PREFIX);
                sb1.append(BOUNDARY);
                sb1.append(LINEND);
                sb1
                        .append("Content-Disposition:  form-data;  name=\"file\";  filename=\""
                                + file.getKey() + "\"" + LINEND);
                sb1.append("Content-Type:  application/octet-stream;  charset="
                        + CHARSET + LINEND);
                sb1.append(LINEND);
                outStream.write(sb1.toString().getBytes());
                InputStream is = new FileInputStream(file.getValue());
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = is.read(buffer)) != -1) {
                    outStream.write(buffer, 0, len);
                }

                is.close();
                outStream.write(LINEND.getBytes());
            }
        //  请求结束标志
        byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
        outStream.write(end_data);
        outStream.flush();
        //  得到响应码
        int res = conn.getResponseCode();
        InputStream in = conn.getInputStream();
        InputStreamReader isReader = new InputStreamReader(in);
        BufferedReader bufReader = new BufferedReader(isReader);
        String line = null;
        String data = "OK";

        while ((line = bufReader.readLine()) == null)
            data += line;

        if (res == 200) {
            int ch;
            StringBuilder sb2 = new StringBuilder();
            while ((ch = in.read()) != -1) {
                sb2.append((char) ch);
            }
        }
        outStream.close();
        conn.disconnect();
        return in.toString();
    }

    public void sendAudioToServer(final int thisFileIndex){
        mHandler.post(new Runnable() {
            //this.thisFileIndex = thisFileIndex;
            @Override
            public void run() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("method",  "save");

                   //params.put("timelength",  timelength);
                try {
                    //得到SDCard的目录
                    String fileName = "recorded_audio"+ thisFileIndex + ".wav";
                    File uploadFile = new File(Environment.getExternalStorageDirectory(), fileName );
                    //上传音频文件
                    //Toast.makeText(MainActivity.this,"success4",LENGTH_SHORT).show();
                    Map<String, File> formfile = new HashMap<String, File>();
                    formfile.put(fileName, uploadFile);
                    Toast.makeText(AudioRecorderActivity2.this,"success5",LENGTH_SHORT).show();

                    //postwav("http://www.element14.site:5000/upload/audio", params, formfile);
                    postwav("http://172.20.10.4:5000/upload/audio", params, formfile);
                    //Toast.makeText(MainActivity.this,"successfinal",LENGTH_SHORT).show();
//                                                            Toast.makeText(MainActivity.this,  R.string.success,  1).show();
                    Toast.makeText(AudioRecorderActivity2.this, "Audio sent successfully!", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    //Toast.makeText(MainActivity.this,  R.string.error,  1).show();
                    Log.e(TAG,  e.toString());
                    //Toast.makeText(MainActivity.this,"failed",LENGTH_SHORT).show();
                }
            }
        });
    }
        /*Thread thread = new Thread(r);
        thread.start();*/
//        try { Thread.currentThread().sleep(1000);
//        } catch (InterruptedException e)
//        {
//        }
        //thread.stop();


}
