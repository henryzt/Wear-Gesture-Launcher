package com.format.gesturelauncher;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

public class Settings extends AppCompatActivity {

    Switch showQuick;
    Switch vibrate;

    RadioGroup location;
    SeekBar seek;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //------------------------------------------------------Back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //------------------------------------------------------Get pref
        SharedPreferences sharedPref = getSharedPreferences("main",MODE_PRIVATE);
        boolean showq = sharedPref.getBoolean("show",true);
        boolean vib = sharedPref.getBoolean("vibrate",true);
        String loca=sharedPref.getString("location","r");
        int accuracy=sharedPref.getInt("accuracy",2);


        //------------------------------------------------------change
        showQuick =(Switch) findViewById(R.id.switch1);
        showQuick.setChecked(showq);

        vibrate =(Switch) findViewById(R.id.switchVib);
        vibrate.setChecked(vib);


        location=(RadioGroup)findViewById(R.id.radio);

        seek=(SeekBar)findViewById(R.id.seekBar2);
        seek.setProgress(accuracy);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(i>=3){
                    Toast.makeText(getApplicationContext(), R.string.settings_high_notice,Toast.LENGTH_LONG).show();
                }
                if(i==1){
                    Toast.makeText(getApplicationContext(), R.string.settings_low_notice,Toast.LENGTH_LONG).show();
                }
                if(i==0){
                    Toast.makeText(getApplicationContext(), R.string.settings_zero_notice,Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //------------------------------------------------------Radios
        int RadioID = 0;

        switch (loca){
            case "r":
                RadioID=R.id.radioRight;
                break;
            case "l":
                RadioID=R.id.radioLeft;
                break;
            case "t":
                RadioID=R.id.radioTop;
                break;
            case "b":
                RadioID=R.id.radioBottom;
                break;
        }

        RadioButton b =(RadioButton)findViewById(RadioID);
        b.setChecked(true);


        //----------------------------------------Button
        findViewById(R.id.buttonGo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),BackUp.class));
            }
        });
    }




    public String getLocationSelected(){
        switch (location.getCheckedRadioButtonId()){
            case R.id.radioRight:
                return "r";

            case R.id.radioLeft:
                return "l";

            case R.id.radioTop:
                return "t";

            case R.id.radioBottom:
                return "b";

        }
        return "f";
    }



    public void savePref(){
        SharedPreferences sharedPref =getSharedPreferences("main",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("show",showQuick.isChecked());
        editor.putBoolean("vibrate",vibrate.isChecked());
        editor.putString("location",getLocationSelected());
        editor.putInt("accuracy",seek.getProgress());
        editor.apply();
        Toast.makeText(getApplicationContext(), R.string.settings_changes_saved,Toast.LENGTH_SHORT).show();


    }


    @Override
    protected void onDestroy() {
        savePref();
        MobileConnectService.Sync(MainActivity.mobileconnect,true);
        super.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
