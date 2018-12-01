package de.iteratec.slab.segway.remote.robot.service;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.voice.Speaker;
import com.segway.robot.sdk.voice.VoiceException;
import com.segway.robot.sdk.voice.tts.TtsListener;

import java.util.concurrent.TimeUnit;

import de.iteratec.slab.segway.remote.robot.listener.MessageListener;

/**
 * Created by abr on 22.12.17.
 */

public class LoomoSpeakService implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;

    MediaPlayer player = new MediaPlayer();

//    InvisibleVideoRecorder HDCamera;

    //InvisibleVideoRecorder HDCamera = new InvisibleVideoRecorder(this.context);

    @Override
    public void onInit(int status) {

    }

    private static final String TAG = "LoomoSpeakService";
    private Speaker speaker;
    private Context context;
    public static LoomoSpeakService instance;
//    public Camera2VideoFragment myCamera = new Camera2VideoFragment();

    public static LoomoSpeakService getInstance() {
        if (instance == null) {
            throw new IllegalStateException("LoomoSpeakService instance not initialized yet");
        }
        return instance;
    }

    public LoomoSpeakService(Context context) {
        this.context = context;
        init();
        this.instance = this;

        tts = new TextToSpeech(context, this);
//        HDCamera = new InvisibleVideoRecorder(this.context);
    }

    public void restartService() {
        init();
    }

    public void playVoice() {
        try {
            AssetManager assetManager = this.context.getAssets();
            AssetFileDescriptor descriptor = assetManager.openFd("beepboop.mp4");
            player.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            player.prepare();
            player.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void endVoice() {
        if (player.isPlaying()) {
            player.stop();
            player.reset();
        }
    }

    public void speak(String sentence) {
//        try {
//            Camera2VideoFragment myCamera = new Camera2VideoFragment(this.context);
//            myCamera.openCamera(640,480);
////            try { TimeUnit.SECONDS.sleep(10); } catch (InterruptedException e) { e.printStackTrace();}
//            myCamera.startRecordingVideo();
//            Log.d(TAG, "ran startRecordingVideo()");
//
//
//            tts.speak(sentence, TextToSpeech.QUEUE_FLUSH, null);
//
//
//            try { TimeUnit.SECONDS.sleep(10); } catch (InterruptedException e) { e.printStackTrace();}
//            myCamera.stopRecordingVideo();
//            Log.d(TAG, "ran stopRecordingVideo()");
//            myCamera.closeCamera();
//            Log.d(TAG, "ran closeCamera()");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


//        CameraControllerV2WithoutPreview hope = new CameraControllerV2WithoutPreview(this.context);
//        tts.speak(sentence, TextToSpeech.QUEUE_FLUSH, null);
//        hope.openCamera();
//        Log.d(TAG, "openCamera()");
//        try { Thread.sleep(20); } catch (InterruptedException e) {}
//        hope.takePicture();
//        Log.d(TAG, "Picture taken or at least went through takePicture()");
//
//
//        Toast.makeText(this.context, "Picture took!!!", Toast.LENGTH_SHORT).show();
        InvisibleVideoRecorder HDCamera = new InvisibleVideoRecorder(this.context);
        tts.speak(sentence, TextToSpeech.QUEUE_FLUSH, null);
            Log.v("receivingMessage", MessageListener.messageReceived);
        if(MessageListener.messageReceived.equals("Start Recording")) {
            HDCamera.start(); // starts recording (maybe allow sometime between start and stop, b/c it might throw an error)
//            try { TimeUnit.SECONDS.sleep(20); } catch (InterruptedException e) {}
            MessageListener.messageReceived = "";
            Log.i("Recording Status", "Started Recording");
        }
        if(MessageListener.messageReceived.equals("Stop Recording")){
            Log.i("Recording Status", "Stopped Recording");
            HDCamera.stop();
        }


//        try { Thread.sleep(20000); } catch (InterruptedException e) {}  // 20 seconds
//        if(MessageListener.messageReceived.equals("Stop Recording")) {
//            HDCamera.stop(); // stops recording
//            MessageListener.messageReceived = "";
//            Log.i("Recording Status", "Stopped Recording");
//        }
        // saves to /storage/sdcard0/Android/data/de.iteratec.slab.segway.remote.robot/files/movies/ on robot
    }

    public void recordingStatus(){
        Log.i("RecordingStatus", "Method Called");
        InvisibleVideoRecorder HDCamera = new InvisibleVideoRecorder(this.context);

        if(MessageListener.messageReceived.equals("Start Recording")) {
            HDCamera.start(); // starts recording (maybe allow sometime between start and stop, b/c it might throw an error)
            Log.i("RecordingStatus", MessageListener.messageReceived);
            MessageListener.messageReceived = "";
        }
        //try { Thread.sleep(20000); } catch (InterruptedException e) {}  // 20 seconds
        else if(MessageListener.messageReceived.equals("Stop Recording")) {
            HDCamera.stop(); // stops recording
            Log.i("RecordingStatus", MessageListener.messageReceived);
            MessageListener.messageReceived = "";
        }
    }

    private void init() {

        speaker = Speaker.getInstance();
        speaker.bindService(context, new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                Log.d(TAG, "Speaker onBind");
            }

            @Override
            public void onUnbind(String reason) {
                Log.d(TAG, "Speaker onUnbind");
            }
        });
    }


    public void disconnect() {
        this.speaker.unbindService();
    }


}
