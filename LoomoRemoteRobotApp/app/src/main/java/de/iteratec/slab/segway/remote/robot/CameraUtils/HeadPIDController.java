package de.iteratec.slab.segway.remote.robot.CameraUtils;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class HeadPIDController {
    private static final String TAG = "HeadPIDController";
    private final int TURN_HEAD = 12345;
    private final int TARGET_UPDATE_TIMEOUT = 34567;
    private static final String KEY_YAW = "targetYaw";
    private static final String KEY_RECT = "targetRect";
    private static final String KEY_IMAGE_HEIGHT = "imageHeight";
    private HeadPIDController.SimplePIDController mHeadYawController;
    private float mHeadSpeedFactor = 1.0F;
    private float mMinPitch = 0.2F;
    private float mMaxPitch = 1.0F;
    private HandlerThread mHandlerThread;
    private HeadPIDController.ControlHandler mHandler;
    private HeadPIDController.HeadControlHandler mHead;
    private boolean isTargetLoss;
    private boolean isStop;

    public HeadPIDController() {
    }

    public void init(HeadPIDController.HeadControlHandler headControlHandler) {
        this.mHead = headControlHandler;
        this.mHeadYawController = new HeadPIDController.SimplePIDController();
        this.mHeadYawController.init();
        this.mHandlerThread = new HandlerThread("headPidThread");
        this.mHandlerThread.start();
        this.mHandler = new HeadPIDController.ControlHandler(this.mHandlerThread.getLooper());
    }

    public void release() {
        this.mHandlerThread.quitSafely();
        this.mHead = null;
    }

    public void updateTarget(double theta, Rect personRect, int imageHeight) {
        this.mHandler.removeMessages(12345);
        this.mHandler.removeMessages(34567);
        this.isTargetLoss = false;
        this.isStop = false;
        Message msg = new Message();
        msg.what = 12345;
        Bundle data = new Bundle();
        data.putDouble("targetYaw", theta);
        data.putParcelable("targetRect", personRect);
        data.putInt("imageHeight", imageHeight);
        msg.setData(data);
        this.mHandler.sendMessage(msg);
        this.mHandler.sendEmptyMessageDelayed(34567, 1000L);
    }

    public void stop() {
        this.isStop = true;
        this.mHandler.removeMessages(12345);
    }

    public void reset() {
        this.mHeadYawController.resetController();
    }

    public void setHeadFollowFactor(float HeadSpeedFactor) {
        this.mHeadSpeedFactor = HeadSpeedFactor;
        this.mHeadYawController.setSpeedFactor(this.mHeadSpeedFactor);
    }

    public float getHeadFollowFactor() {
        return this.mHeadSpeedFactor;
    }

    public void setHeadPitchLimitation(float max, float min) {
        if (max < min) {
            throw new IllegalArgumentException("The max pitch angle must greater than min.");
        } else {
            this.mMaxPitch = max;
            this.mMinPitch = min;
        }
    }

    private HeadPIDController.HeadControlValue processHeadControl(double targetYaw2Base, double headYaw2Base, double headPitchAngle, Rect rect, int imgHeight) {
        HeadPIDController.HeadControlValue returnValue = new HeadPIDController.HeadControlValue(this.processPitchControl(headPitchAngle, rect, imgHeight), this.processYawControl(targetYaw2Base, headYaw2Base));
        return returnValue;
    }

    private double processYawControl(double targetYaw2Base, double headYaw2Base) {
        return this.mHeadYawController.generateOutput(targetYaw2Base - headYaw2Base);
    }

    private double processPitchControl(double headPitchAngle, Rect rect, int imgHeight) {
        float baseSpeed = 0.4F;
        double pitchSpeed;
        double minAngle;
        double maxAngle;
        double angleFactor;
        if ((float)rect.top < (float)imgHeight / 5.0F) {
            minAngle = 0.75D * (double)this.mMaxPitch;
            maxAngle = 1.25D * (double)this.mMaxPitch;
            if (headPitchAngle > maxAngle) {
                pitchSpeed = (double)(-baseSpeed);
            } else if (headPitchAngle < minAngle) {
                pitchSpeed = (double)baseSpeed;
            } else {
                angleFactor = (headPitchAngle - minAngle) / (maxAngle - minAngle);
                pitchSpeed = (double)baseSpeed + angleFactor * (double)(-baseSpeed - baseSpeed);
            }
        } else if ((float)rect.top > (float)imgHeight / 2.7F) {
            minAngle = 0.75D * (double)this.mMinPitch;
            maxAngle = 1.25D * (double)this.mMinPitch;
            if (headPitchAngle > maxAngle) {
                pitchSpeed = (double)(-baseSpeed);
            } else if (headPitchAngle < minAngle) {
                pitchSpeed = (double)baseSpeed;
            } else {
                angleFactor = (headPitchAngle - minAngle) / (maxAngle - minAngle);
                pitchSpeed = (double)baseSpeed + angleFactor * (double)(-baseSpeed - baseSpeed);
            }
        } else {
            pitchSpeed = 0.0D;
        }

        return pitchSpeed;
    }

    private class SimplePIDController {
        double kp;
        double kd;
        double ki;
        double integrator;
        double error;
        double lastError;
        double outputThreshold;
        double output;

        private SimplePIDController() {
        }

        void init() {
            this.kp = 10.0D;
            this.ki = 0.0D;
            this.kd = 0.0D;
            this.outputThreshold = 1.8D;
            this.resetController();
        }

        void setSpeedFactor(float factor) {
            this.kp *= (double)factor;
        }

        void resetController() {
            this.integrator = 0.0D;
            this.lastError = 0.0D;
        }

        void limitIntegration() {
            if (this.integrator > 0.2D) {
                this.integrator = 0.2D;
            }

            if (this.integrator < -0.2D) {
                this.integrator = -0.2D;
            }

        }

        void setParams(float p, float i, float d, float limit) {
            float MAXLIMIT = 1.2F;
            if (this.outputThreshold < (double)MAXLIMIT) {
                this.outputThreshold = (double)limit;
            } else {
                this.outputThreshold = (double)MAXLIMIT;
            }

            this.kp = (double)p;
            this.ki = (double)i;
            this.kd = (double)d;
        }

        void limitOutput() {
            if (this.output > this.outputThreshold) {
                this.output = this.outputThreshold;
            }

            if (this.output < -this.outputThreshold) {
                this.output = -this.outputThreshold;
            }

        }

        double generateOutput(double err) {
            this.error = err / 3.1415926D;
            this.output = this.kp * this.error;
            this.integrator += this.error;
            this.limitIntegration();
            double deltaError = this.error - this.lastError;
            this.output += this.kd * deltaError;
            this.output += this.ki * this.integrator;
            this.limitOutput();
            this.lastError = this.error;
            Log.d("HeadPIDController", "Angle PID output is " + this.output + "  err is " + this.error);
            return this.output;
        }
    }

    private class HeadControlValue {
        double pitchCtrlSpeed;
        double yawCtrlSpeed;

        HeadControlValue(double pitchValue, double yawValue) {
            this.pitchCtrlSpeed = pitchValue;
            this.yawCtrlSpeed = yawValue;
        }
    }

    public interface HeadControlHandler {
        float getJointYaw();

        float getJointPitch();

        void setYawAngularVelocity(float var1);

        void setPitchAngularVelocity(float var1);
    }

    private class ControlHandler extends Handler {
        ControlHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what) {
                case 12345:
                    double targetYaw = msg.getData().getDouble("targetYaw");
                    Rect targetRect = (Rect)msg.getData().getParcelable("targetRect");
                    int imageHight = msg.getData().getInt("imageHeight");
                    float headYaw = HeadPIDController.this.mHead.getJointYaw();
                    float headPitch = HeadPIDController.this.mHead.getJointPitch();
                    HeadPIDController.HeadControlValue headControlValue = HeadPIDController.this.processHeadControl(targetYaw, (double)headYaw, (double)headPitch, targetRect, imageHight);
                    float yawCtlSpeed = (float)headControlValue.yawCtrlSpeed;
                    float pitchCtlSpeed = (float)headControlValue.pitchCtrlSpeed;
                    HeadPIDController.this.mHead.setYawAngularVelocity(yawCtlSpeed);
                    if (!HeadPIDController.this.isTargetLoss) {
                        HeadPIDController.this.mHead.setPitchAngularVelocity(pitchCtlSpeed);
                    } else {
                        HeadPIDController.this.mHead.setPitchAngularVelocity(0.0F);
                    }

                    if (!HeadPIDController.this.isStop) {
                        Message message = new Message();
                        message.copyFrom(msg);
                        HeadPIDController.this.mHandler.sendMessageDelayed(message, 50L);
                    }
                    break;
                case 34567:
                    HeadPIDController.this.isTargetLoss = true;
            }

        }
    }
}