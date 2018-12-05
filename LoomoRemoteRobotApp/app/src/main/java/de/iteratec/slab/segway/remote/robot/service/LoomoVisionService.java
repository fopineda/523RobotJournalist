package de.iteratec.slab.segway.remote.robot.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.nfc.Tag;
import android.util.Log;
import android.view.Surface;

import com.segway.robot.algo.dts.DTSPerson;
import com.segway.robot.algo.dts.PersonTrackingListener;
import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.base.log.Logger;
import com.segway.robot.sdk.baseconnectivity.MessageConnection;
import com.segway.robot.sdk.connectivity.BufferMessage;
import com.segway.robot.sdk.locomotion.head.Head;
import com.segway.robot.sdk.vision.DTS;
import com.segway.robot.sdk.vision.Vision;
import com.segway.robot.sdk.vision.frame.Frame;
import com.segway.robot.sdk.vision.stream.StreamType;

import java.io.ByteArrayOutputStream;
import de.iteratec.slab.segway.remote.robot.CameraUtils.AutoFitDrawableView;

/**
 * Created by mss on 22.12.17.
 */

public class LoomoVisionService {

    private static final String TAG = "LoomoVisionService";
    private final Context context;

    public Vision vision;
    private boolean isVisionBind;
    public DTS dts;
    public Surface mDTSSurface = null;


    public static LoomoVisionService instance;

    public static LoomoVisionService getInstance() {
        if (instance == null) {
            throw new IllegalStateException("LoomoVisionService instance not initialized yet");
        }
        return instance;
    }

    public LoomoVisionService(Context context) {
        this.context = context;
        init();
        instance = this;
    }

    public void restartService() {
        init();
    }

    private void init() {
        this.vision = Vision.getInstance();
        this.vision.bindService(this.context, new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                Log.e(TAG, "Vision onBind() called");
                isVisionBind = true;
                dts = vision.getDTS();
                dts.setVideoSource(DTS.VideoSource.SURFACE);
                dts.start();
                mDTSSurface = dts.getSurface();
                dts.setPoseRecognitionEnabled(true);
//                startTrackingPerson();
            }

            @Override
            public void onUnbind(String reason) {
                Log.d(TAG, "Vision service unbound");
                isVisionBind = false;
            }
        });
    }

    public void startTransferringImageStream(final MessageConnection messageConnection) {
        Log.i(TAG, "startTransferringImageStream called");
        /*
        StreamInfo[] infos = this.vision.getActivatedStreamInfo();

        for (StreamInfo info : infos) {
            Log.d(TAG, "Streaminfo: Height: " + info.getHeight() + "Width: " + info.getWidth() + "Fps: " + info.getFps() + "PixelFormat: " + info.getPixelFormat() + "Resolution: " + info.getResolution() + "StreamType: " + info.getStreamType());
        }*/
        this.vision.startListenFrame(StreamType.COLOR, new Vision.FrameListener() {

            private int frameCount = 0;

            @Override
            public void onNewFrame(int streamType, Frame frame) {
                if (frameCount == 0) {
                    Log.d(TAG, "sending frame");

                    try {
                        // Get image frame as bitmap
                        Bitmap bitmap = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888);
                        bitmap.copyPixelsFromBuffer(frame.getByteBuffer());

                        // Compress via JPG
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, os);
                        byte[] byteArray = os.toByteArray();

                        Log.d(TAG, "Byte size: " + byteArray.length);
                        if (byteArray.length > 1000000) {
                            Log.w(TAG, "Byte size is > 1 MB! Exceeds max sending size.");
                        } else {
                            messageConnection.sendMessage(new BufferMessage(byteArray));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d(TAG, e.getMessage());
                    }
                    frameCount = 0;
                } else {
                    frameCount++;
                }
            }
        });
    }

    public void stopTransferringImageStream() {
        Log.d(TAG, "stopTransferringImageStream called");
        this.vision.stopListenFrame(StreamType.COLOR);
    }



    public void startTrackingPerson() {
        Log.e(TAG, "inside stp");
        dts.startPersonTracking(null, 15L * 60 * 1000 * 1000, new PersonTrackingListener() {

            @Override
            public void onPersonTracking(DTSPerson person) {
                if (person == null) {
                    Log.e(TAG, "inside the if null :/");
                    return;
                }
//                autoFitDrawableView.drawRect(person.getPoseRecognitionIndex(), person.getDrawingRect());
                Log.e(TAG, "inside OPT");
                if (LoomoHeadService.getInstance().isHeadBind) {
                    Log.e(TAG, "inside isheadbind");
                    LoomoHeadService.getInstance().headPIDController.updateTarget(person.getTheta(), person.getDrawingRect(), 480);
                }
            }

            @Override
            public void onPersonTrackingResult(DTSPerson person) {
                Logger.d(TAG, "onPersonTrackingResult() called with: person = [" + person + "]");
            }

            @Override
            public void onPersonTrackingError(int errorCode, String message) {
                Logger.e(TAG, "onPersonTrackingError() called with: errorCode = [" + errorCode + "], message = [" + message + "]");
            }
        });
    }

    public void disconnect() {
        this.vision.unbindService();
    }
}