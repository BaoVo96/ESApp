package com.example.bao_vo.esapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.content.Intent;
import android.app.Activity;
import android.widget.TextView;

public class settingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);


        final Button btnOK = (Button) findViewById(R.id.buttonOK);
        final SeekBar seekBarGasLevel = (SeekBar) findViewById(R.id.seekBarGasLevel);
        final SeekBar seekBarHumidity = (SeekBar) findViewById(R.id.seekBarHumidity);
        final SeekBar seekBarTemperature = (SeekBar) findViewById(R.id.seekBarTemperature);
        final TextView textViewGasLevel = (TextView) findViewById(R.id.textViewGasLevel);
        final TextView textViewHumidity = (TextView) findViewById(R.id.textViewHumidity);
        final TextView textViewTemperature = (TextView) findViewById(R.id.textViewTemperature);



        btnOK.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                String humidity = Integer.toString(seekBarHumidity.getProgress());
                String temperature = Integer.toString(seekBarTemperature.getProgress());
                String gasLevel = Integer.toString(seekBarGasLevel.getProgress());

                Intent returnIntent = new Intent();
                returnIntent.putExtra("humidity", humidity);
                returnIntent.putExtra("temperature", temperature);
                returnIntent.putExtra("gasLevel", gasLevel);
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            }
        });

        seekBarGasLevel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 500;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                progress = progressValue;
                textViewGasLevel.setText(progressValue + "/" + seekBar.getMax());
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textViewGasLevel.setText(progress + "/" + seekBar.getMax());
            }
        });

        seekBarHumidity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 90;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                progress = progressValue;
                textViewHumidity.setText(progressValue + "/" + seekBar.getMax());
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textViewHumidity.setText(progress + "/" + seekBar.getMax());
            }
        });

        seekBarTemperature.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 40;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                progress = progressValue;
                textViewTemperature.setText(progressValue + "/" + seekBar.getMax());
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textViewTemperature.setText(progress + "/" + seekBar.getMax());
            }
        });

    }
}
