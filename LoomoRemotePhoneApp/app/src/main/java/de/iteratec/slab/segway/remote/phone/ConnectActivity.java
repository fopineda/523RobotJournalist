package de.iteratec.slab.segway.remote.phone;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.segway.robot.mobile.sdk.connectivity.MobileMessageRouter;
import com.segway.robot.mobile.sdk.connectivity.StringMessage;
import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.baseconnectivity.Message;
import com.segway.robot.sdk.baseconnectivity.MessageConnection;
import com.segway.robot.sdk.baseconnectivity.MessageRouter;

import de.iteratec.slab.segway.remote.phone.service.ConnectionCallback;
import de.iteratec.slab.segway.remote.phone.service.ConnectionService;

public class ConnectActivity extends AppCompatActivity implements ConnectionCallback {

    private static final String TAG = ConnectActivity.class.getName();

    private ConnectionService connectionService;
    private EditText ipInput;

    public static String message = "";
    private MobileMessageRouter mMobileMessageRouter = null;
    private MessageConnection mMessageConnection = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        ipInput = (EditText) findViewById(R.id.ip_input);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, ConnectionService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        connectionService.unregisterCallback(this);
        unbindService(serviceConnection);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i(TAG, "Connected to service. Redirecting to NavigationActivity");
            connectionService = ((ConnectionService.LocalBinder) iBinder).getService();
            connectionService.registerCallback(ConnectActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(TAG, "Disconnected from service");
            connectionService.unregisterCallback(ConnectActivity.this);
            connectionService = null;
        }
    };

    @Override
    public void onConnected() {
        Intent intent = new Intent(this, NavigationActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDisconnected() {
        try {
            Toast.makeText(connectionService, "Disconnected from Loomo", Toast.LENGTH_SHORT).show();
        } catch (RuntimeException ignored) {

        }
    }


    public void connectToRobot(View view) {
        if (serviceConnection == null) {
            Toast.makeText(connectionService, "Service is null", Toast.LENGTH_SHORT).show();
        } else {
            connectionService.connectToRobot(ipInput.getText().toString().trim());
        }
    }

    public void skipToController(View view) {
        connectionService.setConnectionSkipped(true);
        Intent intent = new Intent(this, NavigationActivity.class);
        startActivity(intent);
    }


    /******************************************
     *
     * This is where the Message Sending Begins
     *
     *****************************************/

    /*public void waitForMessage(){
        mMobileMessageRouter = MobileMessageRouter.getInstance();

        //robot-sample, you can read the IP from the robot app.
        mMobileMessageRouter.setConnectionIp(ipInput.getText().toString().trim());

        //bind the connectivity service in robot
        mMobileMessageRouter.bindService(this, mBindStateListener);
    }

    private ServiceBinder.BindStateListener mBindStateListener = new ServiceBinder.BindStateListener() {
        @Override
        public void onBind() {
            Log.d(TAG, "onBind: ");
            try {
                mMobileMessageRouter.register(mMessageConnectionListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUnbind(String reason) {
            Log.e(TAG, "onUnbind: " + reason);
        }
    };

    private MessageRouter.MessageConnectionListener mMessageConnectionListener = new MessageRouter.MessageConnectionListener() {
        @Override
        public void onConnectionCreated(final MessageConnection connection) {
            Log.d(TAG, "onConnectionCreated: " + connection.getName());
            //get the MessageConnection instance
            mMessageConnection = connection;
            try {
                mMessageConnection.setListeners(mConnectionStateListener, mMessageListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private MessageConnection.ConnectionStateListener mConnectionStateListener = new MessageConnection.ConnectionStateListener() {
        @Override
        public void onOpened() {
            //connection between mobile application and robot application is opened.
            //Now can send messages to each other.
            Log.d(TAG, "onOpened: " + mMessageConnection.getName());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "connected to: " + mMessageConnection.getName(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onClosed(String error) {
            //connection closed with error
            Log.e(TAG, "onClosed: " + error + ";name=" + mMessageConnection.getName());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "disconnected to: " + mMessageConnection.getName(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    };


    private MessageConnection.MessageListener mMessageListener = new MessageConnection.MessageListener() {
        @Override
        public void onMessageReceived(final Message message) {
            // message received
            Log.d(TAG, "onMessageReceived: id=" + message.getId() + ";timestamp=" + message.getTimestamp());
            if (message instanceof StringMessage) {
                //message received is StringMessage
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("Message Received", message.getContent().toString());
                    }
                });
            }
            else{
                Log.i("Message Received", "Message Not Received Correctly");
            }
        }

        @Override
        public void onMessageSentError(Message message, String error) {

        }

        @Override
        public void onMessageSent(Message message) {
            //the message  that is sent successfully
            Log.d(TAG, "onMessageSent: id=" + message.getId() + ";timestamp=" + message.getTimestamp());
        }
    };*/



}
