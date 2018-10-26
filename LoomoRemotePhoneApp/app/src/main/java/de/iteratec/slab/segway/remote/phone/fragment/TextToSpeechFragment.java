package de.iteratec.slab.segway.remote.phone.fragment;


import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;

import com.segway.robot.mobile.sdk.connectivity.StringMessage;

import java.sql.*;

import de.iteratec.slab.segway.remote.phone.R;
import de.iteratec.slab.segway.remote.phone.fragment.base.RemoteFragment;
import de.iteratec.slab.segway.remote.phone.util.CommandStringFactory;

public class TextToSpeechFragment extends RemoteFragment {

    private static final String TAG = "TextToSpeechFragment";

    private EditText speechInput;

    private SeekBar volumeSeekBar;

    static final String databaseURL = "jdbc:mysql://robotjournalisttest.cr2mefbyc9b2.us-east-1.rds.amazonaws.com:3306/robotjournalist";
    static final String username = "roboj";
    static final String password = "robotjournalist";

    public String getConnection(){
        String logMessage;
        String tryCatch;

        try{
            new MyTask().execute();
            logMessage = "Connection successful";
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

        Button soundTestButton = layout.findViewById(R.id.sound_test);
        soundTestButton.setOnClickListener(mButtonClickListener);
        speechInput = layout.findViewById(R.id.speech_input);

        Button broadcastAudioButton = layout.findViewById(R.id.brodcast_audio);
        broadcastAudioButton.setOnClickListener(nButtonClickListener);

        Button stopBroadcastButton = layout.findViewById(R.id.end_broadcast);
        stopBroadcastButton.setOnClickListener(rButtonClickListener);

        volumeSeekBar = layout.findViewById(R.id.volumeSeekBar);
        volumeSeekBar.setOnSeekBarChangeListener(volumeListener);

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
            getLoomoService().sendSound(speak);
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

        @Override
        protected Void doInBackground(Void...arg0){

            try{
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://robotjournalisttest.cr2mefbyc9b2.us-east-1.rds.amazonaws.com:3306/robotjournalist", "roboj", "robotjournalist");

                Statement stmt = con.createStatement();

                ResultSet rs = stmt.executeQuery("SELECT * FROM tester WHERE testerID = 1");
                rs.next();

                //Log.d("mySQLCreationSuccess","successfully connected to database");
                logMessage = rs.getString(2);

            } catch (Exception e) {
                e.printStackTrace();
                logMessage = "SQL Fail";
            }

            return null;
        }


        public String getLogMessage(){
            return logMessage;
        }

        @Override
        protected void onPostExecute(Void result){
            Log.v("SQLStatus", logMessage);
        }

    }
}



