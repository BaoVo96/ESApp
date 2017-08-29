package com.example.bao_vo.esapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import android.net.ConnectivityManager;
import android.content.Context;
import android.net.NetworkInfo;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.support.v4.app.NotificationCompat;

import java.lang.Integer;

import android.os.Vibrator;
import android.media.RingtoneManager;
import android.net.Uri;
import android.media.Ringtone;

import android.app.Activity;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class UserAreaActivity extends AppCompatActivity {
    private	String topic        = "co3053";
    private	int qos             = 1;
    private	String broker       = "tcp://m11.cloudmqtt.com:12375";
    private	String clientId     = "mobileapp";
    private String userName;
    private String password;
    private static MqttAndroidClient client;
    private static MqttConnectOptions options;
    private String synchTopic   = "synchronize";
    private String synchMessage = "synchronize";
    private int humidity = 90;
    private int temperature = 40;
    private int gasLevel = 500;

    //private
    TextView textViewGas;
    TextView textViewHumidity;
    TextView textViewTemperature;
    TextView textViewLastUpdate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_area);

        // create button and text view
        final Button btnLogOut = (Button) findViewById(R.id.buttonLogOut);
        final Button btnSynch = (Button) findViewById(R.id.buttonSynch);
        final ImageButton btnSetting = (ImageButton) findViewById(R.id.buttonSetting);

        textViewGas = (TextView) findViewById(R.id.textViewGas);
        textViewHumidity = (TextView) findViewById(R.id.textViewHumidity);
        textViewTemperature = (TextView) findViewById(R.id.textViewTemperature);
        textViewLastUpdate = (TextView) findViewById(R.id.textViewLastUpdate);
        ////end create button and text view////



        // get user name and password
        Intent callerIntent=getIntent();
        Bundle packageFromCaller = callerIntent.getBundleExtra("userInfo");
        userName = packageFromCaller.getString("textUserName");
        password = packageFromCaller.getString("textPassword");
        ////end get user name and password////


        createClient();
        tryToSetCallback();
        tryToConnect();



        //Click button Log out
        btnLogOut.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                tryToDisconnect();
                finish();
            }
        });////end Click button Log out////


        //Click button Synchronize
        btnSynch.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                    if(haveNetworkConnection()){
                        publishMessageSynchronize();
                        Toast.makeText(UserAreaActivity.this, "Synchronize succeed", Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(UserAreaActivity.this, "Connection lost! Check your internet and loggin again!", Toast.LENGTH_LONG).show();
                        finish();
                    }
            }
        });////end Click button Synchronize////

        btnSetting.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent i = new Intent(getApplicationContext(), settingActivity.class);
                startActivityForResult(i, 1);
            }
        });


    }//end onCreate()

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                humidity = Integer.parseInt(data.getStringExtra("humidity"));
                temperature = Integer.parseInt(data.getStringExtra("temperature"));
                gasLevel = Integer.parseInt(data.getStringExtra("gasLevel"));
                //Toast.makeText(UserAreaActivity.this,data.getStringExtra("humidity")+ ";" +data.getStringExtra("temperature")+ ";" +  data.getStringExtra("gasLevel"), Toast.LENGTH_LONG).show();

            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }//onActivityResult



    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    private void tryToSubscribe(){
        try {
            IMqttToken subToken = client.subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The message was published
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards
                    Toast.makeText(UserAreaActivity.this, "Can't susbcribe!", Toast.LENGTH_LONG).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }//end tryToSubscribe

//    private void tryToUnscribe(){
//        try {
//            IMqttToken subToken = client.unsubscribe(topic);
//            subToken.setActionCallback(new IMqttActionListener() {
//                @Override
//                public void onSuccess(IMqttToken asyncActionToken) {
//                    Toast.makeText(UserAreaActivity.this, "You are unsubscribe", Toast.LENGTH_LONG).show();
//                }
//
//                @Override
//                public void onFailure(IMqttToken asyncActionToken,
//                                      Throwable exception) {
//                    // The subscription could not be performed, maybe the user was not
//                    // authorized to subscribe on the specified topic e.g. using wildcards
//                    Toast.makeText(UserAreaActivity.this, "Can't unsubscribe!", Toast.LENGTH_LONG).show();
//                }
//            });
//        } catch (MqttException e) {
//            e.printStackTrace();
//        }
//    }// end tryToUnsubscribe()

    private void tryToConnect(){
        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    //Toast.makeText(UserAreaActivity.this, "Connected", Toast.LENGTH_LONG).show();
                    // try to subscribe the topic
                    tryToSubscribe();
                    publishMessageSynchronize();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Toast.makeText(UserAreaActivity.this, "Can't Connect", Toast.LENGTH_LONG).show();
                }

            });
        } catch (MqttException e) {
            e.printStackTrace();
        }////end connect to server////
    }
    private void tryToDisconnect(){
        try {
            IMqttToken token = client.disconnect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(UserAreaActivity.this, "You are disconnected", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(UserAreaActivity.this, "Can't disconnect", Toast.LENGTH_LONG).show();
                }

            });
        } catch (MqttException e) {
            e.printStackTrace();
        }////end connect to server////
    }


    private void publishMessageSynchronize(){
        try {
            client.publish(synchTopic, synchMessage.getBytes(), 0, false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }



    private void createClient(){
        //create client
        client = new MqttAndroidClient(this.getApplicationContext(), broker, clientId);
        options = new MqttConnectOptions();
        options.setUserName(userName);
        options.setPassword(password.toCharArray());
    }

    //user only once in function onCreate;
    private void tryToSetCallback(){
        // Get message from mqtt cloud
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {
                Toast.makeText(UserAreaActivity.this, "Connection Lost!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                String[] parts = mqttMessage.toString().split(";");
                textViewTemperature.setText(parts[0]);
                textViewHumidity.setText(parts[1]);
                textViewGas.setText(parts[2]);
                textViewLastUpdate.setText(parts[3]);
                if(Integer.parseInt(textViewGas.getText().toString())>gasLevel){
                    addNotification("Warning", "Gas level is high: " + textViewGas.getText().toString());
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                    r.play();

                    vibrator.vibrate(2000);
                }
                if(Integer.parseInt(textViewHumidity.getText().toString()) > humidity){
                    addNotification("Warning", "Humidity is high: " + textViewHumidity.getText().toString());
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                    r.play();
                    vibrator.vibrate(2000);
                }
                if(Integer.parseInt(textViewTemperature.getText().toString()) > temperature){
                    addNotification("Warning", "Temperature is high: " + textViewTemperature.getText().toString());
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                    r.play();

                    vibrator.vibrate(2000);
                }

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });////end Get message from mqtt cloud////
    }

    /*addNotification
    * This function send a notification to user*/
    private void addNotification(String title, String text) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.warning_icon)
                        .setContentTitle(title)
                        .setContentText(text);

        Intent notificationIntent = new Intent(this, UserAreaActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }/*END addNotification*/

}/* END UserAreaActivity()*/
