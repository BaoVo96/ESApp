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

import java.lang.Integer;
import java.text.SimpleDateFormat;
import java.util.Calendar;


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
    private	String topic        = "event";
    private	int qos             = 1;
    private	String broker       = "tcp://m11.cloudmqtt.com:16416";
    private	String clientId     = "mobileapp";
    private String userName;
    private String password;
    private static MqttAndroidClient client;
    private static MqttConnectOptions options;
    private String commandTopic   = "command";
    private String synchMessage = "synchronize";

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
                String returnMessage = data.getStringExtra("returnMessage");
                //Toast.makeText(UserAreaActivity.this,data.getStringExtra("humidity")+ ";" +data.getStringExtra("temperature")+ ";" +  data.getStringExtra("gasLevel"), Toast.LENGTH_LONG).show();
                if(!returnMessage.equals("cancel")){
                    publishMessageStatus(returnMessage);
                }

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
            client.publish(commandTopic, synchMessage.getBytes(), 0, false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void publishMessageStatus(String aStatus){
        try {
            client.publish(commandTopic, aStatus.getBytes(), 0, false);
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
                String[] parts = mqttMessage.toString().split("\\|");
                textViewTemperature.setText(parts[0]);
                textViewHumidity.setText(parts[1]);
                textViewGas.setText(parts[2]);
                textViewLastUpdate.setText(getDateTime());


            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });////end Get message from mqtt cloud////
    }

    private String getDateTime(){

        Calendar c = Calendar.getInstance();
        System.out.println("Current time => "+c.getTime());

        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss\nyyyy-MM-dd");
        return df.format(c.getTime());

    }


}/* END UserAreaActivity()*/
