package com.format.gesturelauncher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.support.wearable.view.ConfirmationOverlay;
import android.view.View;
import android.widget.TextView;

import com.google.android.wearable.intent.RemoteIntent;

import static com.format.gesturelauncher.WearConnectService.showQuickLauncher;



public class MainActivity extends Activity {

    static WearConnectService wearConnect;
//    static FloaterService floaterService;
    static boolean apiCompatibleMode =false;
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
//                apiCompatibleMode=true;
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

//            apiCompatibleMode=true;
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
//                    warningDialog();
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
                    if(!showQuickLauncher) {
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
//                apiCompatibleMode=true;
//                Intent intent = new Intent(getApplicationContext(), GesturePerformActivity.class);
//                startActivity(intent);
//            }

        }




        if(apiCompatibleMode){
            TextView text=findViewById(R.id.textViewIns);
//            text.setText("Compatible mode");
//            text.setVisibility(View.GONE);
//            findViewById(R.id.imageViewIn).setVisibility(View.GONE);
            text.setText(R.string.main_click_perfrom_gesture);
            text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    if(!showQuickLauncher) {
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
                        versionAlert();
                        sharedPref.edit().putBoolean("NEWINSTALL", false).apply();
                    }


                    apiCompatibleMode =true;

                }

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



    public void versionAlert(){
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







    @Override
    protected void onPause() {
        super.onPause();
        finish(); //TODO

//        if (!this.isFinishing()){
//            //TODOTODODONE find the problem caused which after user click the home button, the launching speed will be slow(Possible solution: swipe to open)
////            msg("This action would take longer to load next time, please use cancel button instead.");
//            Intent intent = new Intent(getApplicationContext(),GesturePerformActivity.class);
//
//            intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
//
//            startActivity(intent);
//
//        }

    }




}
