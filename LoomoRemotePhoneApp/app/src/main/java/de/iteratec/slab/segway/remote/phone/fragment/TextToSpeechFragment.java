package de.iteratec.slab.segway.remote.phone.fragment;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import java.util.concurrent.TimeUnit;
import android.os.Bundle;
import android.util.Log;
import java.util.ArrayList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;

import com.segway.robot.mobile.sdk.connectivity.StringMessage;
import com.segway.robot.sdk.vision.Vision;

import java.sql.*;

import de.iteratec.slab.segway.remote.phone.R;
import de.iteratec.slab.segway.remote.phone.fragment.base.RemoteFragment;
import de.iteratec.slab.segway.remote.phone.util.CommandStringFactory;

public class TextToSpeechFragment extends RemoteFragment {

    private static final String TAG = "TextToSpeechFragment";

    private EditText userIdInput;
    private EditText interviewNumberInput;

    private SeekBar volumeSeekBar;

    static final String databaseURL = "jdbc:mysql://robotjournalisttest.cr2mefbyc9b2.us-east-1.rds.amazonaws.com:3306/robotjournalist";
    static final String username = "roboj";
    static final String password = "robotjournalist";
    //String message;
    String logMessage;

    public String getConnection(){
        //String logMessage;

        try{
            new MyTask().execute();
            //AsyncTask task = new MyTask();//.execute();
            //task.execute();
            //logMessage = "Connection successful";
            //logMessage = ((MyTask) task).getLogMessage();
            //ogMessage = logMessage;
        }
        catch(Exception e){
            e.printStackTrace();
            logMessage = "Execute not working";
        }

        return logMessage;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout =  inflater.inflate(R.layout.fragment_text_to_speech, container, false);

        Button soundTestButton = layout.findViewById(R.id.BeginInterview);
        soundTestButton.setOnClickListener(mButtonClickListener);

        Button loginButton = layout.findViewById(R.id.login);
        loginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View view){
                //startActivity(new Intent(TextToSpeechFragment.this.getActivity(), VisionFragment.class));
                VisionFragment.userName = userIdInput.getText().toString();
                VisionFragment.interviewName = interviewNumberInput.getText().toString();

                if(!userIdInput.getText().toString().equals("") && !interviewNumberInput.getText().toString().equals(""))
                {
                    VisionFragment visionFrag = new VisionFragment();
                    FragmentManager fragmentManager=getActivity().getFragmentManager();
                    FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.content_frame,visionFrag,"tag");
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
            }
        });

        userIdInput = layout.findViewById(R.id.UserID);
        interviewNumberInput = layout.findViewById(R.id.InterviewID);

        //speechInput = layout.findViewById(R.id.speech_input);

        //Button broadcastAudioButton = layout.findViewById(R.id.brodcast_audio);
        //broadcastAudioButton.setOnClickListener(nButtonClickListener);

        //Button stopBroadcastButton = layout.findViewById(R.id.end_broadcast);
        //stopBroadcastButton.setOnClickListener(rButtonClickListener);

        //volumeSeekBar = layout.findViewById(R.id.volumeSeekBar);
        //volumeSeekBar.setOnSeekBarChangeListener(volumeListener);

        return layout;
    }

    private View.OnClickListener nButtonClickListener = new View.OnClickListener() {

        public void onClick(View view) {
            String[] message = {"broadcast", "start"};
            getLoomoService().send(CommandStringFactory.getStringMessage(message));
        }
    };

    private View.OnClickListener mButtonClickListener = new View.OnClickListener() {

        public void onClick(View view) {
            //String message = getConnection();

            String speak = getConnection();
            //String speak = speechInput.getText().toString().trim();
            Log.i(TAG, "Trying to say: " + speak);
            //Log.i("UserId", userIdInput.getText().toString());
            //Log.i("InterviewNumber",interviewNumberInput.getText().toString());
            //getLoomoService().sendSound(speak);
        }
    };

    private View.OnClickListener rButtonClickListener = new View.OnClickListener() {
        public void onClick(View view) {
            String[] message = {"broadcast", "stop"};
            getLoomoService().send(CommandStringFactory.getStringMessage(message));
        }
    };

    private SeekBar.OnSeekBarChangeListener volumeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
            //Log.i(TAG, "Progress shifted: " + progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            String volume = Integer.toString(volumeSeekBar.getProgress());
            Log.i(TAG, "Volume adjust: " + volume);


        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            String[] message = {"volume", Integer.toString(volumeSeekBar.getProgress())};
            getLoomoService().send(CommandStringFactory.getStringMessage(message));
        }
    };



    private class MyTask extends AsyncTask<Void, Void, Void>{

        String logMessage;
        ArrayList<String> questionList = new ArrayList<String>();


        @Override
        protected Void doInBackground(Void...arg0){

            try{
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://robotjournalisttest.cr2mefbyc9b2.us-east-1.rds.amazonaws.com:3306/robotjournalist", "roboj", "robotjournalist");

                Statement stmt = con.createStatement();

                String userName = userIdInput.getText().toString();
                String interviewID = interviewNumberInput.getText().toString();

                Log.i("UserId", userName);
                Log.i("InterviewID", interviewID);

                ResultSet rs = stmt.executeQuery("SELECT * FROM interviewQuestions WHERE interviewID = '" + interviewID + "' AND userID = '" + userName + "'");
                //rs.next();

                //Log.d("mySQLCreationSuccess","successfully connected to database");
                //logMessage = rs.getString(4);
                //questionList.add(rs.getString(4));
                //questionList.add(rs.getString(5));
                //questionList.add(rs.getString(6));

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

        public String getLogMessage(){
            //logMessage = "Got Log Message";
            return logMessage;
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