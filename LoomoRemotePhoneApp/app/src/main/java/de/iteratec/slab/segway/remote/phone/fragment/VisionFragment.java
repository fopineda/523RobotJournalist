package de.iteratec.slab.segway.remote.phone.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.segway.robot.mobile.sdk.connectivity.StringMessage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import de.iteratec.slab.segway.remote.phone.ConnectActivity;
import de.iteratec.slab.segway.remote.phone.R;
import de.iteratec.slab.segway.remote.phone.fragment.base.JoyStickControllerFragment;
import de.iteratec.slab.segway.remote.phone.service.ByteMessageReceiver;
import de.iteratec.slab.segway.remote.phone.service.ConnectionService;
import de.iteratec.slab.segway.remote.phone.util.CommandStringFactory;
import de.iteratec.slab.segway.remote.phone.util.MovementListenerFactory;
import io.github.controlwear.virtual.joystick.android.JoystickView;

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


        Button startInterviewButton = layout.findViewById(R.id.startInterview);
        startInterviewButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View view){
                Log.i("Vision userName", userName);
                Log.i("Vision interviewName", interviewName);
                try{
                    new VisionFragment.MyTask().execute();

                }
                catch(Exception e){
                    e.printStackTrace();
                }
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

    private class MyTask extends AsyncTask<Void, Void, Void> {

        String logMessage;
        ArrayList<String> questionList = new ArrayList<String>();


        @Override
        protected Void doInBackground(Void...arg0){

            try{
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://robotjournalisttest.cr2mefbyc9b2.us-east-1.rds.amazonaws.com:3306/robotjournalist", "roboj", "robotjournalist");

                Statement stmt = con.createStatement();

                ResultSet rs = stmt.executeQuery("SELECT * FROM interviewQuestions WHERE interviewID = '" + interviewName + "' AND userID = '" + userName + "'");

                getLoomoService().sendSound("Hello, my name is " + userName + " and I'd like to interview you about " + interviewName + ". Is it okay if I ask you some questions?");
//                questionList.add("Hello, my name is " +userName+ " and I'd like to interview you about " +interviewName+ ". Is it okay if I ask you some questions?");
                try
                {
                    int counter =0;
                    while(counter < 40) {
                        TimeUnit.SECONDS.sleep(1);
                        counter++;
                        Log.i("ConnectionMessage", ConnectionService.messageReceived);
                        if(ConnectionService.messageReceived.equals("Positive Received")){
                            getLoomoService().sendSound("Thank you for taking the time to Speak with me!");
                            TimeUnit.SECONDS.sleep(10);
                            break;
                        }
                    }
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                    Log.v("Sleep Time", "Sleep Failed");
                }


                while(rs.next())
                {
                    questionList.add(rs.getString(4));
                }

                logMessage = "SQL Succeeded";

            } catch (Exception e) {
                e.printStackTrace();
                logMessage = "SQL Fail";
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            Log.v("SQLStatus", logMessage);
            for(String question : questionList) {
                getLoomoService().sendSound(question);
                try
                {
                    TimeUnit.SECONDS.sleep(3);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                    Log.v("Sleep Time", "Sleep Failed");
                }
            }
        }

    }

}
