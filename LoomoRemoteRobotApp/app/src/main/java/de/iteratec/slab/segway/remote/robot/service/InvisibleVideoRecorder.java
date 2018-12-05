package de.iteratec.slab.segway.remote.robot.service;

import android.app.Activity;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

import com.segway.robot.algo.dts.DTSPerson;
import com.segway.robot.algo.dts.PersonTrackingListener;
import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.base.log.Logger;
import com.segway.robot.sdk.locomotion.head.Head;
import com.segway.robot.sdk.vision.DTS;
import com.segway.robot.sdk.vision.Vision;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.iteratec.slab.segway.remote.robot.CameraUtils.HeadPIDController;
import de.iteratec.slab.segway.remote.robot.CameraUtils.HeadControlHandlerImpl;
/**
 * @author Mark
 * @since 6/10/2016
 */
public class InvisibleVideoRecorder {
    private static final String TAG = "InvisibleVideoRecorder";
    private final CameraCaptureSessionStateCallback cameraCaptureSessionStateCallback = new CameraCaptureSessionStateCallback();
    private final CameraDeviceStateCallback cameraDeviceStateCallback = new CameraDeviceStateCallback();
    private MediaRecorder mediaRecorder;
    private CameraManager cameraManager;
    private Context context;

    private CameraDevice cameraDevice;

    private HandlerThread handlerThread;
    private Handler handler;
    private Vision mVision;
    private Head mHead;
    private boolean isVisionBind;
    private boolean isHeadBind;
    private DTS dts;
    private Surface mDTSSurface = null;
    private HeadPIDController headPIDController = new HeadPIDController();

    public InvisibleVideoRecorder(Context context) {
        this.context = context;

//        bindServices();

        handlerThread = new HandlerThread("HDCamera");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        try {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);

            final String filename = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES).getAbsolutePath() + File.separator + System.currentTimeMillis() + ".mp4";
            mediaRecorder.setOutputFile(filename);
            Log.d(TAG, "start: " + filename);


            CamcorderProfile profile = CamcorderProfile.get(CameraMetadata.LENS_FACING_BACK, CamcorderProfile.QUALITY_1080P);
            mediaRecorder.setOrientationHint(0);
            mediaRecorder.setProfile(profile);
            mediaRecorder.prepare();
        } catch (IOException e) {
            Log.d(TAG, "start: exception" + e.getMessage());
        }

    }


    public void start() {
        Log.d(TAG, "start: ");

        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.openCamera(cameraId, cameraDeviceStateCallback, handler);
        } catch (CameraAccessException | SecurityException e) {
            Log.d(TAG, "start: exception " + e.getMessage());
        }

    }

    public void stop() {
        Log.d(TAG, "stop: ");
        mediaRecorder.stop();
        mediaRecorder.reset();
        mediaRecorder.release();
        cameraDevice.close();
        try {
            handlerThread.join();
        } catch (InterruptedException e) {

        }
    }

    // Callback for the different states of the camera
    private class CameraDeviceStateCallback extends CameraDevice.StateCallback {
        private final static String TAG = "CamDeviceStateCb";

        @Override
        public void onClosed(CameraDevice camera) {
            Log.d(TAG, "onClosed: ");
            super.onClosed(camera);
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            Log.d(TAG, "onDisconnected: ");
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            Log.d(TAG, "onError: ");
        }

        @Override
        public void onOpened(CameraDevice camera) {
            Log.d(TAG, "onOpened: ");
            cameraDevice = camera;
            try {

                //Arrays.asList(LoomoVisionService.getInstance().mDTSSurface)
                List<Surface> surfaceList = new ArrayList<>();
                surfaceList.add(LoomoVisionService.getInstance().mDTSSurface);
                surfaceList.add(mediaRecorder.getSurface());
                camera.createCaptureSession(surfaceList, cameraCaptureSessionStateCallback, handler);
            } catch (CameraAccessException e) {
                Log.d(TAG, "onOpened: " + e.getMessage());
            }
        }
    }


    // Sessions
    private class CameraCaptureSessionStateCallback extends CameraCaptureSession.StateCallback {
        private final static String TAG = "CamCaptSessionStCb";

        @Override
        public void onActive(CameraCaptureSession session) {
            Log.d(TAG, "onActive: ");
            super.onActive(session);
        }

        @Override
        public void onClosed(CameraCaptureSession session) {
            Log.d(TAG, "onClosed: ");
            super.onClosed(session);
        }

        @Override
        public void onConfigured(CameraCaptureSession session) {
            Log.d(TAG, "onConfigured: ");
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {
            Log.d(TAG, "onConfigureFailed: ");
        }

        @Override
        public void onReady(CameraCaptureSession session) {
            Log.d(TAG, "onReady: ");
            super.onReady(session);
            try {
                CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
                builder.addTarget(LoomoVisionService.getInstance().mDTSSurface);
                builder.addTarget(mediaRecorder.getSurface());
                CaptureRequest request = builder.build();
                session.setRepeatingRequest(request, null, handler);

                LoomoVisionService.getInstance().startTrackingPerson();
                mediaRecorder.start();
                Log.d(TAG, "after MR.start()");


            } catch (CameraAccessException e) {
                Log.d(TAG, "onConfigured: " + e.getMessage());

            }
        }

        @Override
        public void onSurfacePrepared(CameraCaptureSession session, Surface surface) {
            Log.d(TAG, "onSurfacePrepared: ");
            super.onSurfacePrepared(session, surface);
        }
    }
}