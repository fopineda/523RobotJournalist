package de.iteratec.slab.segway.remote.phone.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.segway.robot.mobile.sdk.connectivity.StringMessage;

import de.iteratec.slab.segway.remote.phone.R;
import de.iteratec.slab.segway.remote.phone.fragment.base.JoyStickControllerFragment;
import de.iteratec.slab.segway.remote.phone.service.ByteMessageReceiver;
import de.iteratec.slab.segway.remote.phone.util.CommandStringFactory;
import de.iteratec.slab.segway.remote.phone.util.MovementListenerFactory;
import io.github.controlwear.virtual.joystick.android.JoystickView;

/**
 * Created by mss on 22.12.17.
 */

public class VisionFragment extends JoyStickControllerFragment implements ByteMessageReceiver {

    private static final String TAG = "VisionFragment";

    private ImageView imageView;

    private JoystickView joySpeed;
    private JoystickView joyDirection;
    private JoystickView joyHeadPitch;
    private JoystickView joyHeadYaw;

    public static String userName = "";
    public static String interviewName = "";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_vision, container, false);
        imageView = layout.findViewById(R.id.image_stream);

        joySpeed = layout.findViewById(R.id.stream_joy_speed);
        joyDirection = layout.findViewById(R.id.stream_joy_direction);
        joyHeadPitch = layout.findViewById(R.id.stream_joy_head_pitch);
        joyHeadYaw = layout.findViewById(R.id.stream_joy_head_yaw);

        joySpeed.setOnMoveListener(MovementListenerFactory.getJoystickMoveListener(this, MovementListenerFactory.JOYSTICK_SPEED));
        joyDirection.setOnMoveListener(MovementListenerFactory.getJoystickMoveListener(this, MovementListenerFactory.JOYSTICK_DIRECTION));
        joyHeadPitch.setOnMoveListener(MovementListenerFactory.getJoystickMoveListener(this, MovementListenerFactory.JOYSTICK_PITCH));
        joyHeadYaw.setOnMoveListener(MovementListenerFactory.getJoystickMoveListener(this, MovementListenerFactory.JOYSTICK_YAW));

        joySpeed.setOnTouchListener(MovementListenerFactory.getJoyStickReleaseListener(this, MovementListenerFactory.JOYSTICK_SPEED));
        joyDirection.setOnTouchListener(MovementListenerFactory.getJoyStickReleaseListener(this, MovementListenerFactory.JOYSTICK_DIRECTION));
        joyHeadPitch.setOnTouchListener(MovementListenerFactory.getJoyStickReleaseListener(this, MovementListenerFactory.JOYSTICK_PITCH));
        joyHeadYaw.setOnTouchListener(MovementListenerFactory.getJoyStickReleaseListener(this, MovementListenerFactory.JOYSTICK_YAW));


        Button getIDButton = layout.findViewById(R.id.getID);
        getIDButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View view){
                Log.i("Vision userName", userName);
                Log.i("Vision interviewName", interviewName);
            }
        });

        Log.d(TAG, "sending vision start");
        String[] message = {"vision", "start"};
        getLoomoService().send(CommandStringFactory.getStringMessage(message));
        getLoomoService().registerByteMessageReceiver(this);

        return layout;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "sending vision stop");
        getLoomoService().unregisterByteMessageReceiver(this);
        String[] message = {"vision", "stop"};
        getLoomoService().send(CommandStringFactory.getStringMessage(message));
    }

    @Override
    public void handleByteMessage(final byte[] message) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = BitmapFactory.decodeByteArray(message, 0, message.length);
                imageView.setImageBitmap(bitmap);
            }
        });
    }

}
