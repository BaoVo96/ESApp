package com.example.bao_vo.esapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.app.Activity;
import android.widget.Switch;

public class settingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);


        final Button btnOK = (Button) findViewById(R.id.buttonOK);
        final Button btnCancel = (Button) findViewById(R.id.buttonCancel);
        final Switch fanStatus = (Switch) findViewById(R.id.fanStatus);
        final Switch lightStatus = (Switch) findViewById(R.id.lightStatus);
        final Switch mistorizerStatus = (Switch) findViewById(R.id.mistorizerStatus);


        btnOK.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent returnIntent = new Intent();
                String returnMessage="";
                if(fanStatus.isChecked()){
                    returnMessage = returnMessage + "1|";
                }else{
                    returnMessage = returnMessage + "0|";
                }
                if(lightStatus.isChecked()){
                    returnMessage = returnMessage + "1|";
                }else{
                    returnMessage = returnMessage + "0|";
                }
                if(mistorizerStatus.isChecked()){
                    returnMessage = returnMessage + "1";
                }else{
                    returnMessage = returnMessage + "0";
                }

                returnIntent.putExtra("returnMessage", returnMessage);
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent returnIntent = new Intent();
                String returnMessage="cancel";
                returnIntent.putExtra("returnMessage", returnMessage);
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            }
        });
    }
}
