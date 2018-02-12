package com.format.gesturelauncher;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import static com.format.gesturelauncher.WearConnectService.showQuickLauncher;

public class DialogFirst extends Activity {

    Button button;
    Button buttonSkip;
    TextView text;


    boolean skipped =false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_first);


        button=findViewById(R.id.buttonsure);
        buttonSkip =findViewById(R.id.buttonskip);
        text=findViewById(R.id.textwelcome);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&!Settings.canDrawOverlays(getApplicationContext())) {
                        //If the draw over permission is not available open the settings screen
                        //to grant the permission.

                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName()));
//           startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
                        try {
                            startActivity(intent);
                            text.setText(R.string.welcome_begin);

                        }catch (Exception e){
                            text.setText(String.format("Sorry, fail to grant permission, your build version is %d , please contact developer at henryzhang9802@gmail.com and attach the build version. Thank you!", Build.VERSION.SDK_INT));

                        }

                        button.setText("Go");


//                    return;
                    }else {

                        SharedPreferences sharedPref =getSharedPreferences("main",MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putBoolean("show",true);
                        editor.apply();

                        Intent intent2 = new Intent(getApplicationContext(),MainActivity.class);
                        intent2.putExtra("extra","notini");
                        startActivity(intent2);
                        finish();
                    }




            }

        });





        buttonSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(skipped){
                    Intent intent2 = new Intent(getApplicationContext(),MainActivity.class);
                    intent2.putExtra("extra","notini");
                    startActivity(intent2);
                    finish();
                }


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&!Settings.canDrawOverlays(getApplicationContext())) {


                    text.setText(R.string.welcome_not_grant);
                    SharedPreferences sharedPref =getSharedPreferences("main",MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean("show",false);
                    editor.apply();
                    showQuickLauncher =false;

                    button.setVisibility(View.GONE);
                    buttonSkip.setText("Got it");
                    buttonSkip.setTextColor(Color.WHITE);

                    skipped=true;



                }else {
                    Intent intent2 = new Intent(getApplicationContext(),MainActivity.class);
                    intent2.putExtra("extra","notini");
                    startActivity(intent2);
                    finish();
                }

            }
        });
    }
}
