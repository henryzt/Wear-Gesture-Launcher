package com.format.gesturelauncher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.support.wearable.view.ConfirmationOverlay;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.wearable.intent.RemoteIntent;
import com.google.android.wearable.playstore.PlayStoreAvailability;

import static com.format.gesturelauncher.WearConnectService.ShowQuickLauncher;
import static com.format.gesturelauncher.WearConnectService.compatibleMode;
import static com.format.gesturelauncher.WearConnectService.lib;


public class MainActivity extends Activity {

    static WearConnectService wearconnect;
//    static FloaterService floaterService;
    static boolean APIcompatibleMode =false;
    boolean doInitiate =true;

    boolean show;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);






        try{


            if(getIntent().getStringExtra("extra").equals("first")){ //startup
//                Toast.makeText(getApplicationContext(),"Booted up",Toast.LENGTH_SHORT).show();
                finish();
            }

            if(getIntent().getStringExtra("extra").equals("notini")){ //not initiate
                doInitiate=false;
//                APIcompatibleMode=true;
            }

            if(getIntent().hasExtra("message")){
                new ConfirmationOverlay()
                        .setMessage(getIntent().getStringExtra("message"))
                        .setType(ConfirmationOverlay.SUCCESS_ANIMATION)
                        .showOn(MainActivity.this);
            }

//            if(getIntent().equals(AddConfirmGesture.class)){
//                Toast.makeText(getApplicationContext(),"Oh", Toast.LENGTH_SHORT).show();
//            }
        }catch (Exception e){

        }

//----------------------check whether enter gesture perform directly or not
        SharedPreferences sharedPref = getSharedPreferences("main", MODE_PRIVATE);
        show = sharedPref.getBoolean("show", true);

        if(!show && doInitiate){

//            APIcompatibleMode=true;
            Intent intent = new Intent(getApplicationContext(), GesturePerformActivity.class);
            startActivity(intent);
            initiateConnection();// for some reason, because we are cutting down all the following code
            finish();
        }





        findViewById(R.id.buttonhelp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),Help.class));
            }
        });

        findViewById(R.id.buttonall).setOnClickListener(new View.OnClickListener() { //按钮事件
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(),AllGestures.class);
                intent.putExtra("open","y");
                startActivity(intent);



            }
        });

        findViewById(R.id.buttonadd).setOnClickListener(new View.OnClickListener() { //按钮事件
            @Override
            public void onClick(View view) {
//                startActivity(new Intent(getApplicationContext(),AllGestures.class));
                startActivity(new Intent(getApplicationContext(),AddAction.class));

//                if(compatibleMode){
//                    warningdialog();
//                }
            }
        });

        findViewById(R.id.buttonMore).setOnClickListener(new View.OnClickListener() { //按钮事件
            @Override
            public void onClick(View view) {

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && !Settings.canDrawOverlays(getApplicationContext())) {
                    Intent intent = new Intent(MainActivity.this, DialogFirst.class);
                    startActivity(intent);
                }else {

                    startActivity(new Intent(getApplicationContext(), Setting.class));

                }
            }
        });

        //TODO-----------------------------------------------------------



        findViewById(R.id.buttonTest).setOnClickListener(new View.OnClickListener() { //按钮事件
            @Override
            public void onClick(View view) {

                Intent intentAndroid =
                        new Intent(Intent.ACTION_VIEW)
                                .addCategory(Intent.CATEGORY_BROWSABLE)
                                .setData(getPackageManager().getLaunchIntentForPackage("com.android.browser").getData());

                RemoteIntent.startRemoteActivity(
                        getApplicationContext(),
                        intentAndroid,
                        mResultReceiver);
            }
        });






//-----------------------------------------------------------------
//        SharedPreferences sharedPref = getSharedPreferences("main", MODE_PRIVATE);
        String location = sharedPref.getString("location", "r");
        boolean show = sharedPref.getBoolean("show",true);

        TextView text = findViewById(R.id.textViewIns);

        if(show) {
            String side = "right";
            switch (location) {
                case "r":
                    side = "right";
                    break;
                case "l":
                    side = "left";
                    break;
                case "t":
                    side = "top";
                    break;
                case "b":
                    side = "bottom";
                    break;

            }


            text.setText(String.format(getResources().getString(R.string.settings_open_instruction), side));

        }else {
            text.setText(R.string.main_quicklauncher_disabled_notice);
            text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!ShowQuickLauncher) {
                        Intent intent = new Intent(getApplicationContext(), GesturePerformActivity.class);
                        startActivity(intent);
                    }
                }
            });
        }


    }


    //TODO--------------------------------------------------------------------------------------------------------------------------



    // TODO Result from sending RemoteIntent to phone to open app in play/app store.
    private final ResultReceiver mResultReceiver = new ResultReceiver(new Handler()) {
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if (resultCode == RemoteIntent.RESULT_OK) {
                new ConfirmationOverlay()
                        .setType(ConfirmationOverlay.OPEN_ON_PHONE_ANIMATION)
                        .showOn(MainActivity.this);

            } else if (resultCode == RemoteIntent.RESULT_FAILED) {
                new ConfirmationOverlay()
                        .setMessage(getString(R.string.connection_phone_disconnect))
                        .setType(ConfirmationOverlay.FAILURE_ANIMATION)
                        .showOn(MainActivity.this);

            } else {
                throw new IllegalStateException("Unexpected result " + resultCode);
            }
        }
    };


    //===============================================================================================================================

    @Override
    protected void onStart() {
        super.onStart();

        if(doInitiate) { //If initiate, will create floater, if <AW1.5, will open gesture perform activity
//            SharedPreferences sharedPref = getSharedPreferences("main", MODE_PRIVATE);
//            show = sharedPref.getBoolean("show", true);

            initiateConnection();
            if(show){initiateFloater();}
//            }else{
//                APIcompatibleMode=true;
//                Intent intent = new Intent(getApplicationContext(), GesturePerformActivity.class);
//                startActivity(intent);
//            }

        }

        if(compatibleMode){
            warningdialog();
        }


        if(APIcompatibleMode){
            TextView text=findViewById(R.id.textViewIns);
//            text.setText("Compatible mode");
//            text.setVisibility(View.GONE);
//            findViewById(R.id.imageViewIn).setVisibility(View.GONE);
            text.setText(R.string.main_click_perfrom_gesture);
            text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    if(!ShowQuickLauncher) {
                        Intent intent = new Intent(getApplicationContext(), GesturePerformActivity.class);
                        startActivity(intent);
//                    }
                }
            });
        }
    }




    public void initiateFloater(){

        //--------------------------------------------------------------------------------------------------check for AW 1.5
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.M){

            if(WearConnectService.alreadyCreated==false) {  //First run

                if (!MainActivity.this.isFinishing()) {


                    SharedPreferences sharedPref = getSharedPreferences("main", MODE_PRIVATE);

                    if(sharedPref.getBoolean("NEWINSTALL", true)) {
                        VersionAlert();
                        sharedPref.edit().putBoolean("NEWINSTALL", false).apply();
                    }


                    APIcompatibleMode=true;

                }
//            Toast.makeText(getApplicationContext(),"You are using Android wear 1.5 or lower, which currently isn't fully supported to show the edge quick launcher.",Toast.LENGTH_LONG).show();
            }//else {
                Intent intent = new Intent(getApplicationContext(), GesturePerformActivity.class);
                startActivity(intent);
            //}


//            requestPermissions(Manifest.permission.SYSTEM_ALERT_WINDOW,);
            return;
        }
//--------------------------------------------------------------------------------------------------check for AW 1.5




       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&!Settings.canDrawOverlays(this)) {
           //If the draw over permission is not available open the settings screen
           //to grant the permission.

           Intent intent = new Intent(MainActivity.this, DialogFirst.class);
           startActivity(intent);
           finish();
           return;
       }

       if(FloaterService.frameLayoutfloater==null) {
           startService(new Intent(MainActivity.this, FloaterService.class));

       }

   }

    public void initiateConnection(){
        if(WearConnectService.alreadyCreated==false) {
//            Toast.makeText(getApplicationContext(),"Initiating Connection",Toast.LENGTH_SHORT).show();
            startService(new Intent(MainActivity.this, WearConnectService.class));

        }
    }



    public void VersionAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//            builder.setTitle(R.string.app_name);
        builder.setMessage(R.string.Version_Warning)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //do things
                        Intent intent = new Intent(getApplicationContext(), GesturePerformActivity.class);
                        startActivity(intent);
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }



    public void warningdialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.app_name);
        builder.setMessage("All gesture library changes on your watch might be lost when you sync from phone. This is because your mobile app is running an old version, it might because you just updated the wearable app. Normally you only need to wait for the auto-update on your phone. Please make sure you are using the same version to ensure sync running properly.")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //do things
                    }
                });
        AlertDialog alert = builder.create();
        if (!MainActivity.this.isFinishing()){
            alert.show();
        }

    }//Version Warning



    @Override
    protected void onPause() {
        super.onPause();
        finish(); //TODO

//        if (!this.isFinishing()){
//            //TODOTODODONE find the problem caused which after user click the home button, the launching speed will be slow(Possible solution: swipe to open)
////            Msg("This action would take longer to load next time, please use cancel button instead.");
//            Intent intent = new Intent(getApplicationContext(),GesturePerformActivity.class);
//
//            intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
//
//            startActivity(intent);
//
//        }

    }




}
