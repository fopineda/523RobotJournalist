package de.iteratec.slab.segway.remote.phone.fragment;

import de.iteratec.slab.segway.remote.phone.R;
import de.iteratec.slab.segway.remote.phone.fragment.base.RemoteFragment;
import de.iteratec.slab.segway.remote.phone.fragment.base.RemoteFragmentInterface;
import de.iteratec.slab.segway.remote.phone.service.ConnectionService;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;
import android.content.Context;

public class AudioRecorderFragment extends RemoteFragment  {

    // Declare variables
    Button btnStartRecord, btnStopRecord, btnPlay, btnStop;
    String pathSave = "";
    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;

    final int REQUEST_PERMISSION_CODE = 1000;


//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View layout =  inflater.inflate(R.layout.fragment_text_to_speech, container, false);
//
//        Button soundTestButton = layout.findViewById(R.id.BeginInterview);
//        soundTestButton.setOnClickListener(mButtonClickListener);
//        userIdInput = layout.findViewById(R.id.UserID);
//        interviewNumberInput = layout.findViewById(R.id.InterviewID);
//
//        //speechInput = layout.findViewById(R.id.speech_input);
//
//        //Button broadcastAudioButton = layout.findViewById(R.id.brodcast_audio);
//        //broadcastAudioButton.setOnClickListener(nButtonClickListener);
//
//        //Button stopBroadcastButton = layout.findViewById(R.id.end_broadcast);
//        //stopBroadcastButton.setOnClickListener(rButtonClickListener);
//
//        //volumeSeekBar = layout.findViewById(R.id.volumeSeekBar);
//        //volumeSeekBar.setOnSeekBarChangeListener(volumeListener);
//
//        return layout;
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        View layout =  inflater.inflate(R.layout.recorder, container, false);




        // request runtime permission
        if (!checkPermissionFromDevice())
            requestPermission();

        // init view
        btnPlay = (Button) layout.findViewById(R.id.btnPlay);
        btnStartRecord = (Button) layout.findViewById(R.id.btnStartRecord);
        btnStopRecord = (Button) layout.findViewById(R.id.btnStopRecord);
        btnStop = (Button) layout.findViewById(R.id.btnStop);





        // From Android M, you need to request Run-Time permission



            btnStartRecord.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (checkPermissionFromDevice()) {

                        pathSave = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+ UUID.randomUUID().toString()+"_audio_record.wav";
                        Toast.makeText(getActivity().getApplicationContext(), pathSave, Toast.LENGTH_SHORT).show();
                        setupMediaRecorder();
                        try{
                            mediaRecorder.prepare();
                            mediaRecorder.start();

                        } catch (IOException e){
                            e.printStackTrace();
                        }

                        btnPlay.setEnabled(false);
                        btnStop.setEnabled(false);
                        btnStopRecord.setEnabled(true);

//                        Toast.makeText(getActivity().getApplicationContext(), "Recording...", Toast.LENGTH_SHORT).show();

                    }

                    else{
                        requestPermission();
                    }


                }
            });


            btnStopRecord.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mediaRecorder.stop();
                    btnStopRecord.setEnabled(false);
                    btnPlay.setEnabled(true);
                    btnStartRecord.setEnabled(true);
                    btnStop.setEnabled(false);
                }
            });


            btnPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnStop.setEnabled(true);
                    btnStartRecord.setEnabled(false);
                    btnStartRecord.setEnabled(false);

                    mediaPlayer = new MediaPlayer();
                    try{
                        mediaPlayer.setDataSource(pathSave);
                        mediaPlayer.prepare();

                    } catch (IOException e){
                        e.printStackTrace();
                    }

                    mediaPlayer.start();
                    Toast.makeText(getActivity().getApplicationContext(), "Playing...", Toast.LENGTH_SHORT).show();

                }
            });

            btnStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnStopRecord.setEnabled(false);
                    btnStartRecord.setEnabled(true);
                    btnStop.setEnabled(false);
                    btnPlay.setEnabled(true);

                    if (mediaPlayer != null){
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        setupMediaRecorder();

                    }
                }
            });



        return layout;


    }


    private void setupMediaRecorder(){
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(pathSave);
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(getActivity(), new String[] {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO

        }, REQUEST_PERMISSION_CODE);


    }

    // Press CTRL + O


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){

            case REQUEST_PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(getActivity().getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity().getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean checkPermissionFromDevice(){
        int write_external_storage_result = ContextCompat.checkSelfPermission( getActivity().getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result = ContextCompat.checkSelfPermission( getActivity().getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        return write_external_storage_result == PackageManager.PERMISSION_GRANTED && record_audio_result == PackageManager.PERMISSION_GRANTED;
    }


}
