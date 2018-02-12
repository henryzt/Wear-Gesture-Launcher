package com.format.gesturelauncher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import static com.format.gesturelauncher.MainActivity.apiCompatibleMode;
import static com.format.gesturelauncher.MainActivity.wearConnect;
import static com.format.gesturelauncher.WearConnectService.reload;
import static com.format.gesturelauncher.WearConnectService.sendMobile;

public class Setting extends Activity {

    Switch showQuick;
    Switch vibrate;
    Switch small;
    Switch wider;
    Switch longPress;
    RadioGroup location;
    int accuracy;
//    private TextViewew mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        //------------------------------------------------------Get pref
        SharedPreferences sharedPref = getSharedPreferences("main",MODE_PRIVATE);
        boolean showq = sharedPref.getBoolean("show",true);
        boolean vib = sharedPref.getBoolean("vibrate",true);
        String loca=sharedPref.getString("location","r");
        accuracy=sharedPref.getInt("accuracy",2);
        boolean smallQ=sharedPref.getBoolean("small",false);
        boolean b_longpress = sharedPref.getBoolean("longpress",true);
        boolean b_wider = sharedPref.getBoolean("wider",false);

        //------------------------------------------------------change
        showQuick =(Switch) findViewById(R.id.switch1);
        showQuick.setChecked(showq);

        vibrate =(Switch) findViewById(R.id.switchVib);
        vibrate.setChecked(vib);

        small =(Switch) findViewById(R.id.switchNarrow);
        small.setChecked(smallQ);

        longPress =(Switch) findViewById(R.id.switchLongpress);
        longPress.setChecked(b_longpress);

        wider =(Switch) findViewById(R.id.switchWide);
        wider.setChecked(b_wider);

        location=(RadioGroup)findViewById(R.id.radio);


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




        if(accuracy==2){
            findViewById(R.id.buttonAllge).setVisibility(View.GONE);
            findViewById(R.id.textView4).setVisibility(View.GONE);
        }



        findViewById(R.id.buttonAllge).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accuracy=2;
                Toast.makeText(getApplicationContext(),"Successfully restored",Toast.LENGTH_SHORT).show();
                findViewById(R.id.buttonAllge).setVisibility(View.GONE);
                findViewById(R.id.textView4).setVisibility(View.GONE);

            }
        });



        findViewById(R.id.buttonRolad).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(Setting.this);
//                builder.setTitle("Reload popular gestures");
                builder.setMessage(R.string.settings_reload_warn);


                builder.setPositiveButton(getString(R.string.settings_reload_gestures), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        reload(wearConnect);
                        dialog.cancel();
                    }
                });
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });



        findViewById(R.id.switch1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //hide or show the quick launcher settings

                if(((Switch) findViewById(R.id.switch1)).isChecked()){
                    findViewById(R.id.settings_quicklauncher).setVisibility(View.VISIBLE);
                }else {
                    findViewById(R.id.settings_quicklauncher).setVisibility(View.GONE);
                }

            }
        });



        if(!showq){
            findViewById(R.id.settings_quicklauncher).setVisibility(View.GONE);
        }

        if(apiCompatibleMode){
//            findViewById(R.id.switch1).setVisibility(View.GONE);
//            findViewById(R.id.switchNarrow).setVisibility(View.GONE);
//            findViewById(R.id.radio).setVisibility(View.GONE);
//            findViewById(R.id.textView).setVisibility(View.GONE);
            findViewById(R.id.textViewVersion).setVisibility(View.VISIBLE);
            findViewById(R.id.settings_quicklauncher).setVisibility(View.GONE);
        }

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



    public void savePref() {
        SharedPreferences sharedPref = getSharedPreferences("main", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("show", showQuick.isChecked());
        editor.putBoolean("vibrate", vibrate.isChecked());
        editor.putBoolean("small", small.isChecked());
        editor.putBoolean("longpress", longPress.isChecked());
        editor.putBoolean("wider", wider.isChecked());


        editor.putString("location", getLocationSelected());
        editor.putInt("accuracy", accuracy);
        editor.apply();
//        Toast.makeText(getApplicationContext(), "Changes saved", Toast.LENGTH_SHORT).show();



        //----------------------------------------------------refresh floater
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getApplicationContext())) {

            if (FloaterService.frameLayoutfloater != null) {

                FloaterService.frameLayoutfloater.removeAllViews();
                FloaterService.frameLayoutfloater = null;
                stopService(new Intent(Setting.this, FloaterService.class));
//            msg("service stopped");

                if (sharedPref.getBoolean("show", true)) {
                    startService(new Intent(Setting.this, FloaterService.class));
                }
            } else {
                if (sharedPref.getBoolean("show", true)) {
                    startService(new Intent(Setting.this, FloaterService.class));
                }
            }


//        }else {
//            if(showQuick.isChecked()){
//                Intent intent = new Intent(Setting.this, DialogFirst.class);
//                startActivity(intent);
//            }
//        }

        loadPref();
    }

    //---------------------------------------------------加载preference
    public void loadPref() {
        SharedPreferences sharedPref = getSharedPreferences("main", MODE_PRIVATE);
        wearConnect.showQuickLauncher = sharedPref.getBoolean("show", true);
        wearConnect.vibratorOn = sharedPref.getBoolean("vibrate", true);
        wearConnect.location = sharedPref.getString("location", "r");
        wearConnect.accuracy = sharedPref.getInt("accuracy", 2);
    }


    @Override
    protected void onDestroy() {
        savePref();
        Intent intent=new Intent(getApplicationContext(), MainActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra("extra","notini");
        intent.putExtra("message",getString(R.string.settings_changes_saved));
        startActivity(intent);
        sendMobile(wearConnect);
//        MobileConnectService.Sync(MainActivity.mobileconnect,true);
        super.onDestroy();
    }


    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }


}
