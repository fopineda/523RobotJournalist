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
import android.widget.TextView;
import android.widget.Toast;

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
    public TextView interviewStatus;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_vision, container, false);
        imageView = layout.findViewById(R.id.image_stream);

        interviewStatus = layout.findViewById(R.id.interviewStatus);
        interviewStatus.setBackgroundColor(0xFF808080);
        interviewStatus.setTextColor(0xFF000000);

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
        ArrayList<Integer> questionLengths = new ArrayList<Integer>();
        Boolean isNegative = false;

        @Override
        protected Void doInBackground(Void...arg0){

            try{
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://robotjournalisttest.cr2mefbyc9b2.us-east-1.rds.amazonaws.com:3306/robotjournalist", "roboj", "robotjournalist");

                Statement stmt = con.createStatement();

                ResultSet rs = stmt.executeQuery("SELECT * FROM interviewQuestions WHERE interviewID = '" + interviewName + "' AND userID = '" + userName + "'");

//                ConnectionService.getInstance().onStartRecording();
                getLoomoService().sendSound("Hello, my name is " + userName + " and I'd like to interview you about " + interviewName + ". Is it okay if I ask you some questions? Say Okay Loomo, followed by your answer to let me know if you would like to be interviewed");
//                questionList.add("Hello, my name is " +userName+ " and I'd like to interview you about " +interviewName+ ". Is it okay if I ask you some questions?");
                try
                {
                    int counter =0;
                    while(counter < 50) {
                        TimeUnit.SECONDS.sleep(1);
                        counter++;
                        Log.i("ConnectionMessage", ConnectionService.messageReceived);
                        if(ConnectionService.messageReceived.equals("Positive Received")){
                            getLoomoService().sendSound("Thank you for taking the time to Speak with me! Let's begin the interview.");
                            TimeUnit.SECONDS.sleep(7);
                            ConnectionService.messageReceived = "";
                            isNegative = false;
                            interviewStatus.setText("Interview Accepted");
//                            Toast.makeText(getActivity(), "Interview Accepted", Toast.LENGTH_LONG).show();

                            break;
                        }
                        else if(ConnectionService.messageReceived.equals("Negative Received")){
                            getLoomoService().sendSound("That's alright. Thanks anyway and have a great day.");
                            TimeUnit.SECONDS.sleep(5);
                            ConnectionService.messageReceived = "";
                            isNegative = true;
                            interviewStatus.setText("Interview Rejected");
//                            Toast.makeText(getActivity(), "Interview Denied", Toast.LENGTH_LONG).show();
                            break;
                        }
                        else if(counter == 50){
                            getLoomoService().sendSound("That's alright. Thanks anyway and have a great day.");
                            TimeUnit.SECONDS.sleep(5);
                            ConnectionService.messageReceived = "";
                            isNegative = true;
                            interviewStatus.setText("Response Timed Out");
//                            Toast.makeText(getActivity(), "Interview Denied", Toast.LENGTH_LONG).show();
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
                    questionLengths.add(rs.getInt(5));
                }

                //questionList.add("Thank you for taking the time to interview with me. Have a great day!");

                logMessage = "SQL Succeeded";

            } catch (Exception e) {
                e.printStackTrace();
                logMessage = "SQL Fail";
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.v("SQLStatus", logMessage);
            if (!isNegative){
                int countSpeak = 0;
                int questLengthCounter = 0;
                for (String question : questionList) {
                    if(countSpeak == 0) {
                        ConnectionService.getInstance().onStartRecording();
                        countSpeak++;
                    }
                    getLoomoService().sendSound(question);
                    try {
                        int counter = 0;
                        while(counter < questionLengths.get(questLengthCounter)){
                            //TimeUnit.SECONDS.sleep(15);
                            TimeUnit.SECONDS.sleep(1);
                            counter++;
                            if(ConnectionService.messageReceived.equals("Finished Early Received")){
                                getLoomoService().sendSound("Moving on!");
                                TimeUnit.SECONDS.sleep(2);
                                ConnectionService.messageReceived = "";
                                try {
                                    ConnectionService.getInstance().onStopRecording();
                                }
                                catch(Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Log.v("Sleep Time", "Sleep Failed");
                    }

                    questLengthCounter++;

                    //ConnectionService.getInstance().onStopRecording();
//                    try {
//                        getLoomoService().sendSound("Hopefully this stops recording");
//                        TimeUnit.SECONDS.sleep(5);
//                    }
//                    catch(Exception e){
//                        e.printStackTrace();
//                    }
                }

                ConnectionService.getInstance().onStopRecording();
                getLoomoService().sendSound("Thank you for taking the time to interview with me. Have a great day!");

            }
        }

    }

}
