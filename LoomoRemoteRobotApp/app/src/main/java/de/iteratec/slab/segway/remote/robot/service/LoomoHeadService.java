package de.iteratec.slab.segway.remote.robot.service;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;

import com.segway.robot.algo.dts.DTSPerson;
import com.segway.robot.algo.dts.PersonTrackingListener;
import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.base.log.Logger;
import com.segway.robot.sdk.locomotion.head.Head;
import com.segway.robot.sdk.vision.DTS;


import de.iteratec.slab.segway.remote.robot.CameraUtils.HeadControlHandlerImpl;
import de.iteratec.slab.segway.remote.robot.CameraUtils.HeadPIDController;

/**
 * Created by abr on 22.12.17.
 */

public class LoomoHeadService {

    private static final String TAG = "LoomoBaseService";

    private Head head = null;
    private Context context;
    private Handler timehandler;
    public boolean isHeadBind;

    public HeadPIDController headPIDController = new HeadPIDController();

    public static LoomoHeadService instance;

    public static LoomoHeadService getInstance() {
        if (instance == null) {
            throw new IllegalStateException("LoomoHeadService instance not initialized yet");
        }
        return instance;
    }

    public LoomoHeadService(Context context) {
        timehandler = new Handler();
        this.context = context;
        initBase();
        this.instance = this;
    }

    public void restartService() {
        initBase();
    }

    public void move(int mode, float yawValue, float pitchValue) {
        Log.d(TAG, "actual world head position: " + this.head.getWorldYaw().getAngle() + ";" + this.head.getWorldPitch().getAngle());
        this.head.setMode(mode);
        if (mode == Head.MODE_SMOOTH_TACKING) {
            Log.d(TAG, "Moving world position to: " + yawValue + " / " + pitchValue);
            this.head.setWorldYaw(yawValue);
            this.head.setWorldPitch(pitchValue);
        } else if (mode == Head.MODE_ORIENTATION_LOCK) {
            Log.d(TAG, "Accelerating Head with: " + yawValue + " / " + pitchValue);
            this.head.setYawAngularVelocity(yawValue);
            this.head.setPitchAngularVelocity(pitchValue);
        } else {
            Log.e(TAG, "Skipping command, since received unknown mode: " + mode);
        }
    }


    private void initBase() {
        head = Head.getInstance();
        head.bindService(context, new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                Log.e(TAG, "Head onBind() called");
                isHeadBind = true;
                head.setMode(Head.MODE_ORIENTATION_LOCK);
                head.setWorldPitch(0.3f);
                headPIDController.init(new HeadControlHandlerImpl(head));
                headPIDController.setHeadFollowFactor(1.0f);

            }

            @Override
            public void onUnbind(String reason) {
                Log.d(TAG, "Head bind failed");
                isHeadBind = false;
            }
        });
    }

    public void disconnect() {
        this.head.unbindService();
    }
}