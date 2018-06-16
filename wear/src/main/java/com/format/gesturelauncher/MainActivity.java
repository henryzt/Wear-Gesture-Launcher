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
import android.telephony.SmsManager;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.wearable.intent.RemoteIntent;

import static com.format.gesturelauncher.WearConnectService.showQuickLauncher;



public class MainActivity extends Activity {

    static WearConnectService wearConnect;
//    static FloaterService floaterService;
    static boolean apiCompatibleMode =false;
    boolean doInitiate =true;

    boolean show;

    public Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final SharedPreferences sharedPref = getSharedPreferences("main", MODE_PRIVATE);
        show = sharedPref.getBoolean("show", true);


        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();;
        mTracker.setScreenName("Wearable Main");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());



        try{


            if(getIntent().getStringExtra("extra").equals("first")){ //startup
//                Toast.makeText(getApplicationContext(),"Booted up",Toast.LENGTH_SHORT).show();
                initiateConnection();
                if(show){initiateFloater();}
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

//                Intent intentAndroid =
//                        new Intent(Intent.ACTION_VIEW)
//                                .addCategory(Intent.CATEGORY_BROWSABLE)
//                                .setData(getPackageManager().getLaunchIntentForPackage("com.android.browser").getData());
//
//                RemoteIntent.startRemoteActivity(
//                        getApplicationContext(),
//                        intentAndroid,
//                        mResultReceiver);

                //-----------------------------------------



//                Uri uri = Uri.parse("smsto:123456789");
//                Intent it = new Intent(Intent.ACTION_SENDTO, uri);
//                it.putExtra("sms_body", "The SMS text");
//                startActivity(it);


                //-----------------------------------------TODO SMS doesn't work
//                SmsManager smsManager = SmsManager.getDefault();
//                smsManager.sendTextMessage("PhoneNumber-example:+989147375410", null, "SMS Message Body", null, null);


                //---------------------------------------------
//                Intent intent = new Intent(getApplicationContext(),AddGesture.class);
//                intent.putExtra("method","Spotify phone##mapp##com.spotify.music");
//                intent.putExtra("name","Spotify on phone");
//                startActivity(intent);
                Intent localIntent2 = new Intent("android.intent.action.PICK_ACTIVITY");
//                Intent localIntent3 = new Intent("android.intent.action.MAIN",null);
//                localIntent3.addCategory("android.intent.category.LAUNCHER");
//                localIntent2.putExtra("android.intent.extra.INTENT",localIntent3);
                startActivityForResult(localIntent2, 2);

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
                    side = getString(R.string.settings_right_edge);
                    break;
                case "l":
                    side = getString(R.string.settings_left_edge);
                    break;
                case "t":
                    side = getString(R.string.settings_top_edge);
                    break;
                case "b":
                    side = getString(R.string.settings_bottom_edge);
                    break;

            }


            text.setText(String.format(getResources().getString(R.string.settings_open_instruction), side));

            //Analytics
            mTracker.send(new HitBuilders.EventBuilder().setCategory("Wearable Action").setAction("mainActivityIniWithFloater").setLabel(side).build());

        }else {
            text.setText(R.string.main_quicklauncher_disabled_notice);
            text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!showQuickLauncher) {
                        Intent intent = new Intent(getApplicationContext(), GesturePerformActivity.class);
                        startActivity(intent);
                        //Analytics
                        mTracker.send(new HitBuilders.EventBuilder().setCategory("Wearable Action").setAction("mainActivityIniWithoutFloaterToPerform").build());
                    }
                }
            });
        }


        //-------------------------------------Check oreo

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && show){
            if(!sharedPref.getBoolean("oreoWarned", false)){
                findViewById(R.id.oreoWarn).setVisibility(View.VISIBLE);
            }
        }

        findViewById(R.id.buttonOreo).setOnClickListener(new View.OnClickListener() { //按钮事件
            @Override
            public void onClick(View view) {
                sharedPref.edit().putBoolean("oreoWarned", true).apply();
                findViewById(R.id.oreoWarn).setVisibility(View.GONE);
                //Analytics
                mTracker.send(new HitBuilders.EventBuilder().setCategory("Wearable Action").setAction("Oreo Gone").build());

            }
        });

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
            //Analytics
            mTracker.send(new HitBuilders.EventBuilder().setCategory("Wearable Action").setAction("mainActivityCompatibleMode").build());
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
                        //Analytics
                        mTracker.send(new HitBuilders.EventBuilder().setCategory("Wearable Action").setAction("AW1.5Warned").build());
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
