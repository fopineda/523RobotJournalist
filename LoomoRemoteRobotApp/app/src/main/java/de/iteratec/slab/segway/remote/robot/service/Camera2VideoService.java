package de.iteratec.slab.segway.remote.robot.service;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;

import com.segway.robot.algo.dts.DTSPerson;
import com.segway.robot.algo.dts.PersonTrackingListener;
import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.base.log.Logger;
import com.segway.robot.sdk.locomotion.head.Head;
import com.segway.robot.sdk.vision.DTS;
import com.segway.robot.sdk.vision.Vision;
//import com.segway.robot.support.control.HeadPIDController;

import com.segway.robot.sdk.vision.DTS;

import de.iteratec.slab.segway.remote.robot.CameraUtils.AutoFitDrawableView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Camera2VideoService extends Fragment{
    private static final String TAG = "Camera2VideoServices";
    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    /**
     * The {@link android.util.Size} of video recording.
     */
    private Size mVideoSize;
    /**
     * MediaRecorder
     */
    public MediaRecorder mMediaRecorder = new MediaRecorder();
    /**
     * A reference to the opened {@link android.hardware.camera2.CameraDevice}.
     */
    private CameraDevice mCameraDevice;
    private String mNextVideoAbsolutePath;
    private AutoFitDrawableView autoFitDrawableView;
    private boolean isSurfaceTextureAvailable;
    private Vision mVision;
    private Head mHead;
    private boolean isVisionBind;
    private boolean isHeadBind;
    private DTS dts;
    private Surface mDTSSurface = null;
//    private HeadPIDController headPIDController = new HeadPIDController();
    /**
     * Whether the app is recording video now --------------------------------------------------------------------------------------------------------------------
     */
    private boolean mIsRecordingVideo;

    public static Camera2VideoService newInstance() {
        return new Camera2VideoService();
    }

    /**
     * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
     * {@link TextureView}.
     */
//    public TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
//
//        @Override
//        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
//            isSurfaceTextureAvailable = true;
//            if (isSurfaceTextureAvailable && isVisionBind) {
//                openCamera(width, height);
//            }
//        }
//
//        @Override
//        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
//        }
//
//        @Override
//
//        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
//            isSurfaceTextureAvailable = false;
//            return true;
//        }
//
//        @Override
//        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
//        }
//
//
//    };

//    private void bindServices() {
//        mVision = Vision.getInstance();
//        mHead = Head.getInstance();
//
//        mVision.bindService(getActivity(), visionBindStateListener);
//        mHead.bindService(getActivity(), headBindStateListener);
//    }

//    private ServiceBinder.BindStateListener visionBindStateListener = new ServiceBinder.BindStateListener() {
//        @Override
//        public void onBind() {
//            Logger.e(TAG, "Vision onBind() called");
////            isVisionBind = true;
////            dts = mVision.getDTS();
////            dts.setVideoSource(DTS.VideoSource.SURFACE);
////            dts.start();
////            mDTSSurface = dts.getSurface();
////            dts.setPoseRecognitionEnabled(true);
//            if (isSurfaceTextureAvailable) {
//                openCamera(autoFitDrawableView.getWidth(), autoFitDrawableView.getHeight());
//            }
//        }
//
//        @Override
//        public void onUnbind(String reason) {
//            isVisionBind = false;
//        }
//    };

//    private ServiceBinder.BindStateListener headBindStateListener = new ServiceBinder.BindStateListener() {
//        @Override
//        public void onBind() {
//            Logger.e(TAG, "Head onBind() called");
//            isHeadBind = true;
//            mHead.setMode(Head.MODE_ORIENTATION_LOCK);
//            mHead.setWorldPitch(0.3f);
//            headPIDController.init(new HeadControlHandlerImpl(mHead));
//            headPIDController.setHeadFollowFactor(1.0f);
//        }
//
//
//        @Override
//        public void onUnbind(String reason) {
//            isHeadBind = false;
//        }
//    };

//    private void startTrackingPerson() {
//        dts.startPersonTracking(null, 15L * 60 * 1000 * 1000, new PersonTrackingListener() {
//            @Override
//            public void onPersonTracking(DTSPerson person) {
//                if (person == null) {
//                    return;
//                }
////                autoFitDrawableView.drawRect(person.getPoseRecognitionIndex(), person.getDrawingRect());
//
//                if (isHeadBind) {
//                    headPIDController.updateTarget(person.getTheta(), person.getDrawingRect(), 480);
//                }
//            }
//
//            @Override
//            public void onPersonTrackingResult(DTSPerson person) {
//                Logger.d(TAG, "onPersonTrackingResult() called with: person = [" + person + "]");
//            }
//
//            @Override
//            public void onPersonTrackingError(int errorCode, String message) {
//                Logger.e(TAG, "onPersonTrackingError() called with: errorCode = [" + errorCode + "], message = [" + message + "]");
//            }
//        });
//    }

    /**
     * In this sample, we choose a video size with 3x4 aspect ratio. Also, we don't use sizes
     * larger than 1080p, since MediaRecorder cannot handle such a high-resolution video.
     *
     * @param choices The list of available sizes
     * @return The video size
     */
    public static Size chooseVideoSize(Size[] choices) {
        for (Size size : choices) {
            if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1080) {
                return size;
            }
        }
        Logger.e(TAG, "Couldn't find any suitable video size");
        return choices[choices.length - 1];
    }
    /**
     * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its status.
     */
    public CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
//            startPreview();
            mCameraOpenCloseLock.release();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            Activity activity = getActivity();
            if (null != activity) {
                activity.finish();
            }
        }

    };

    public void initRecording(){
//        if (mIsRecordingVideo) {
//            stopRecordingVideo();
//        } else {
//            startRecordingVideo();
//        }
        startRecordingVideo();
    }

    @SuppressWarnings("MissingPermission")
    public void openCamera(int width, int height) {
        final Activity activity = getActivity();
        if (null == activity || activity.isFinishing()) {
            return;
        }
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            Logger.e(TAG, "tryAcquire");
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            String cameraId = manager.getCameraIdList()[0];

            // Choose the sizes for camera preview and video recording
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map == null) {
                throw new RuntimeException("Cannot get available preview/video sizes");
            }
            mVideoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder.class));
//            mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), width, height, mVideoSize);
            // TODO
//            int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
//            autoFitDrawableView.setPreviewSizeAndRotation(mPreviewSize.getWidth(), mPreviewSize.getHeight(), rotation);

            MediaRecorder mMediaRecorder = new MediaRecorder();
            Log.d(TAG, "mediaRecorder created");
            manager.openCamera(cameraId, mStateCallback, null);
        } catch (CameraAccessException e) {
            Toast.makeText(activity, "Cannot access the camera.", Toast.LENGTH_SHORT).show();
            activity.finish();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.");
        }
    }

    public void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
//            closePreviewSession();
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mMediaRecorder) {
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.");
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    public void setUpMediaRecorder() throws IOException {
        final Activity activity = getActivity();
        if (null == activity) {
            return;
        }
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        if (mNextVideoAbsolutePath == null || mNextVideoAbsolutePath.isEmpty()) {
            mNextVideoAbsolutePath = getVideoFilePath(getActivity());
        }
        mMediaRecorder.setOutputFile(mNextVideoAbsolutePath);
        mMediaRecorder.setVideoEncodingBitRate(10000000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.prepare();
    }

    public String getVideoFilePath(Context context) {
        final File dir = context.getExternalFilesDir(null);
        return (dir == null ? "" : (dir.getAbsolutePath() + "/"))
                + System.currentTimeMillis() + ".mp4";
    }

    public void startRecordingVideo() {
        if (null == mCameraDevice ) {
            return;
        }
        try {
//            closePreviewSession();
            setUpMediaRecorder();
//            SurfaceTexture texture = autoFitDrawableView.getPreview().getSurfaceTexture();
//            assert texture != null;
//            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
//            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
//            List<Surface> surfaces = new ArrayList<>();

            // Set up Surface for the camera preview
//            Surface previewSurface = new Surface(texture);
//            surfaces.add(previewSurface);
//            mPreviewBuilder.addTarget(previewSurface);
            //TODO
//            Logger.e(TAG, "startRecordingVideo() called" + mDTSSurface.isValid());
//            surfaces.add(mDTSSurface);
//            mPreviewBuilder.addTarget(mDTSSurface);

            // Set up Surface for the MediaRecorder
//            Surface recorderSurface = mMediaRecorder.getSurface();
//            surfaces.add(recorderSurface);
//            mPreviewBuilder.addTarget(recorderSurface);

            // Start a capture session
            // Once the session starts, we can update the UI and start recording


//            mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
//
//                @Override
//                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
//                    mPreviewSession = cameraCaptureSession;
//                    updatePreview();
//                    getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            // UI
//                            mButtonVideo.setText(R.string.stop);
//                            mIsRecordingVideo = true;
//
//                            // Start recording
//                            mMediaRecorder.start();
//                        }
//                    });
//                }
//
//                @Override
//                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
//                    Activity activity = getActivity();
//                    if (null != activity) {
//                        Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }, mBackgroundHandler);


            mMediaRecorder.start();
            Log.d(TAG, "startRecordingVideo() called");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void stopRecordingVideo() {
        // UI
        mIsRecordingVideo = false;
//        mButtonVideo.setText(R.string.record);
        // Stop recording
        mMediaRecorder.stop();
        mMediaRecorder.reset();

        Activity activity = getActivity();
        if (null != activity) {
            Toast.makeText(activity, "Video saved: " + mNextVideoAbsolutePath,
                    Toast.LENGTH_SHORT).show();
            Logger.e(TAG, "Video saved: " + mNextVideoAbsolutePath);
            insertVideoToMediaStore(getActivity(), mNextVideoAbsolutePath, System.currentTimeMillis(), 640, 480, 0);

        }
        mNextVideoAbsolutePath = null;
//        startPreview();
    }

    public void insertVideoToMediaStore(Context context, String filePath, long createTime, int width, int height, long duration) {
        ContentValues values = initCommonContentValues(filePath, createTime);
        values.put(MediaStore.Video.VideoColumns.DATE_TAKEN, createTime);
        if (duration > 0) {
            values.put(MediaStore.Video.VideoColumns.DURATION, duration);
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            if (width > 0) {
                values.put(MediaStore.Video.VideoColumns.WIDTH, width);
            }
            if (height > 0) {
                values.put(MediaStore.Video.VideoColumns.HEIGHT, height);
            }
        }
        values.put(MediaStore.MediaColumns.MIME_TYPE, getVideoMimeType(filePath));
        context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
    }

    public static String getVideoMimeType(String path) {
        String lowerPath = path.toLowerCase();
        if (lowerPath.endsWith("mp4") || lowerPath.endsWith("mpeg4")) {
            return "video/mp4";
        } else if (lowerPath.endsWith("3gp")) {
            return "video/3gp";
        }
        return "video/mp4";
    }

    public ContentValues initCommonContentValues(String filePath, long time) {
        ContentValues values = new ContentValues();
        File saveFile = new File(filePath);
        values.put(MediaStore.MediaColumns.TITLE, saveFile.getName());
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, saveFile.getName());
        values.put(MediaStore.MediaColumns.DATE_MODIFIED, time);
        values.put(MediaStore.MediaColumns.DATE_ADDED, time);
        values.put(MediaStore.MediaColumns.DATA, saveFile.getAbsolutePath());
        values.put(MediaStore.MediaColumns.SIZE, saveFile.length());
        return values;
    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }






} // END
