package com.example.bao_vo.esapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

//=========================================
public class LoginActivity extends AppCompatActivity {

    private	String topic        = "event";
    private	int qos             = 1;
    private	String broker       = "tcp://m11.cloudmqtt.com:16416";
    private	String clientId     = "mobileapp";

    private boolean isUserAreaActivityRunning = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        /* create new mqtt client*/
        final MqttAndroidClient client = new MqttAndroidClient(this.getApplicationContext(), broker, clientId);
        final MqttConnectOptions options = new MqttConnectOptions();
        /* END create new mqtt client */


        /* create button*/
        final EditText userName = (EditText) findViewById(R.id.userName);
        final EditText userPassword = (EditText) findViewById(R.id.userPassword);
        final Button btnLogin = (Button) findViewById(R.id.btnLogin);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        /* END create button*/


        /* create click listener for btlLogin*/
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /* get username and password from text view*/
                final String textUserName = userName.getText().toString();
                final String textPassword = userPassword.getText().toString();
                /* END get username and password from text view*/


                /* check user name and password */
                if(textUserName.equals("")){// if user name is empty
                    Toast.makeText(LoginActivity.this, "Please input your user name", Toast.LENGTH_LONG).show();
                }else if(textPassword.equals("")){// if password is empty
                    Toast.makeText(LoginActivity.this, "Please input your password", Toast.LENGTH_LONG).show();
                }else {//

                    /* set username and password to connect to server */
                    options.setUserName(textUserName);
                    options.setPassword(textPassword.toCharArray());
                    /* END set username and password to connect to server*/


                    /* try to connect to server*/
                    try {
                        /* set visibility
                         * Hide: btnLogin, userName, userPassword
                          * Show progressBar*/
                        btnLogin.setVisibility(View.GONE);
                        userName.setVisibility(View.GONE);
                        userPassword.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);
                        /* END set visibility*/

                        /* try to connect to server*/
                        IMqttToken token = client.connect(options);
                        token.setActionCallback(new IMqttActionListener() {

                            /* if connect success
                            * it mean userName and userPassword are correct*/
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                /*try to disconnect*/
                                try {
                                    client.disconnect();
                                } catch (MqttException e) {
                                    e.printStackTrace();
                                }
                                /*END try to disconnect*/

                                /* create new UserAreaActivity and transfer user name and password */
                                Intent intent = new Intent(getApplicationContext(), UserAreaActivity.class);
                                Bundle bundle=new Bundle();
                                bundle.putString("textUserName", textUserName);
                                bundle.putString("textPassword", textPassword);
                                intent.putExtra("userInfo", bundle);
                                startActivity(intent);
                                /* END create new UserAreaActivity and transfer user name and password */


                                /* set visibility
                                * Hide: progress bar
                                * Show: btnLogin, userName, userPassword*/
                                btnLogin.setVisibility(View.VISIBLE);
                                userName.setVisibility(View.VISIBLE);
                                userPassword.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);
                                /*END set visibility*/

                            }/*END if connect success*/

                            /*if connect fail*/
                            @Override
                            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                // Something went wrong e.g. connection timeout or firewall problems

                                /* set visibility
                                * Hide: progressBar
                                * Show: btnLogin, userName, userPassword*/
                                btnLogin.setVisibility(View.VISIBLE);
                                userName.setVisibility(View.VISIBLE);
                                userPassword.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);
                                /*END set visibility*/
                                Toast.makeText(LoginActivity.this, "Someting went wrong, we can't connect to the server", Toast.LENGTH_LONG).show();
                            }
                            /*END if connect fail*/
                        });


                    } catch (MqttException e) {
                        e.printStackTrace();
                        Toast.makeText(LoginActivity.this, "No internet", Toast.LENGTH_LONG).show();


                    } /* END try to connect to server*/

                }/* END check user name and password */

            }
        });/* END create click listener for btlLogin*/

    }/* END onCreate*/

}/*END loginActivity*/

