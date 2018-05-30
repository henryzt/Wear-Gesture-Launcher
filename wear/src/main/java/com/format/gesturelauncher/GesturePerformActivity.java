package com.format.gesturelauncher;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.AlarmClock;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static com.format.gesturelauncher.MainActivity.apiCompatibleMode;
import static com.format.gesturelauncher.MainActivity.wearConnect;
import static com.format.gesturelauncher.WearConnectService.sendMobile;
import static com.format.gesturelauncher.WearConnectService.sendMobileAction;
import static com.format.gesturelauncher.WearConnectService.showQuickLauncher;
import static com.format.gesturelauncher.WearConnectService.accuracy;
import static com.format.gesturelauncher.WearConnectService.lib;
import static com.format.gesturelauncher.WearConnectService.vibratorOn;
import static com.format.gesturelauncher.WearConnectService.wait;

public class GesturePerformActivity extends Activity {

    private BoxInsetLayout mContainerView;
    private TextView mTextView;
    private TextView mClockView;
//TODO 换mainactivity

    public Tracker mTracker;

    final Timer timer = new Timer();//for the delay

    TextView hintText;
    Button mButtonClose;
    Button mButtonWhat;
    ProgressBar mProgress;
    GestureOverlayView mGesture;

    static boolean active = false;//check if this is running

    String tag ="fzg";

//    public static boolean vibratorOn = true; //振动开关


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gesture_main);

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();
        mTracker.setScreenName("Gesture Perform Activity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        //Analytics
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("PerformActivity")
                .setAction("openPerformActivity")
                .setLabel("perform")
                .build());
        //-----------------------


        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            // Activity was brought to front and not created,
            // Thus finishing this will get us to the last viewed activity
//            msg("Clear");

            finish();
            return;
        }


        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mTextView = (TextView) findViewById(R.id.text);
        mButtonClose = findViewById(R.id.buttonClose);
        mButtonWhat=findViewById(R.id.buttonwhat);
        mProgress=findViewById(R.id.progressBar);

//        mClockView = (TextView) findViewById(R.id.clock);





        //-------------------------------------------------------------------------------

        final Vibrator v = (Vibrator) getSystemService(getApplicationContext().VIBRATOR_SERVICE);
        final long[] wrong = {0,30,100,30,50,30};//振动器
        final long[] right = {0,30,};

        if(vibratorOn){v.vibrate(right,-1);} //Vibrate to show it is open

        mGesture = findViewById(R.id.gesture); //定义
        hintText = findViewById(R.id.text);//定义


        mProgress.setVisibility(View.GONE);//progress bar

        //lib=GestureLibraries.fromRawResource(this,R.raw.gestures);//导入手势




        mGesture.addOnGestureListener(new GestureOverlayView.OnGestureListener() {
            @Override
            public void onGestureStarted(GestureOverlayView gestureOverlayView, MotionEvent motionEvent) {
                hintText.setVisibility(View.GONE);
                mButtonClose.setVisibility(View.INVISIBLE);
                mButtonWhat.setVisibility(View.GONE);
                mProgress.setVisibility(View.GONE);

            }

            @Override
            public void onGesture(GestureOverlayView gestureOverlayView, MotionEvent motionEvent) {

            }

            @Override
            public void onGestureEnded(GestureOverlayView gestureOverlayView, MotionEvent motionEvent) {
                hintText.setVisibility(View.VISIBLE);
                hintText.setText(R.string.gesture_matching);
                mButtonClose.setVisibility(View.VISIBLE);

            }

            @Override
            public void onGestureCancelled(GestureOverlayView gestureOverlayView, MotionEvent motionEvent) {
                hintText.setVisibility(View.VISIBLE);
                hintText.setText(R.string.draw_your_pattern);
                mButtonClose.setVisibility(View.VISIBLE);
            }
        });  //手势事件s



        mGesture.addOnGesturePerformedListener(new GestureOverlayView.OnGesturePerformedListener() {  //画完后识别手势
            @Override
            public void onGesturePerformed(GestureOverlayView gestureOverlayView, Gesture gesture) {

                if(!lib.load()){
                    //如果没有手势
//                    Toast.makeText(getApplicationContext(), R.string.empty_lib,Toast.LENGTH_LONG).show();
                    hintText.setText(R.string.empty_lib);
                    if(vibratorOn){v.vibrate(wrong,-1);}
                    return;
                }


                ArrayList<Prediction> predictionArrayList = lib.recognize(gesture);  //手势识别到列表
                boolean matched =false;
                double maxfound = 0.0;
                String maxName = "";



                for(Prediction prediction:predictionArrayList){

                    Log.v(tag,prediction.toString() + "---" + Double.toString(prediction.score));


                    if(prediction.score > maxfound){
                        maxfound=prediction.score;
                        maxName=prediction.name;
                    }

                }

                if(maxfound > accuracy){//2.0
                    hintText.setText(String.format(getString(R.string.gesture_sth_performed), maxName));
                    //msg(prediction.toString());
                    matched=true;
                    if(vibratorOn){v.vibrate(right,-1);}
                    matchOpen(maxName);//尝试打开app



                    //break;

                }else {
                    hintText.setText(R.string.gesture_no_match);


                }

                if(!matched){

                    if(vibratorOn){v.vibrate(wrong,-1);}

                    mButtonWhat.setVisibility(View.VISIBLE);

//                    msg("Please try again");
                }

                //Analytics
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("PerformActivity")
                        .setAction("gestureMatched")
                        .setLabel(maxName)
                        .setValue(Math.round(maxfound))
                        .build());
                //-----------------------

            }
        });




        mButtonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                timer.cancel();  // Terminates this timer, discarding any currently scheduled tasks.
                timer.purge();   // Removes all cancelled tasks from this timer's task queue.

                if(mProgress.getVisibility()==View.VISIBLE){//if confirming, reopen
                    //Analytics
                    mTracker.send(new HitBuilders.EventBuilder().setCategory("PerformActivity").setAction("userCanceledAction").build());
                    Intent intent=new Intent(GesturePerformActivity.this,GesturePerformActivity.class);
                    finish();
                    startActivity(intent);
                }else{
                    //Analytics
                    mTracker.send(new HitBuilders.EventBuilder().setCategory("PerformActivity").setAction("userCanceledPerform").build());
                    finish();

                }


            }
        });

        mButtonWhat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent=new Intent(getApplicationContext(),AllGestures.class);
//                intent.putExtra("open","y");
                Intent intent=new Intent(GesturePerformActivity.this,MainActivity.class);
//                if(apiCompatibleMode) {
                    intent.putExtra("extra", "notini");
//                }
                finish();
                startActivity(intent);

            }
        });



        if( apiCompatibleMode ==true || !showQuickLauncher){
            mButtonWhat.setVisibility(View.VISIBLE);
        }

    }

//=====================================================================================================================================================================================



    //============================================================================================== 打开应用
    @SuppressLint("SetTextI18n")
    public void matchOpen(String activity){

        mGesture.setVisibility(View.GONE);
        mProgress.setVisibility(View.VISIBLE);
        mButtonClose.setTextColor(Color.rgb(118, 255, 3));//match color of the spinner
        final NameFilter name = new NameFilter(activity);
        int delay=0;
        if(wait){delay=2600;}


        TimerTask task = null;


        try {
            switch (name.getMethod()) {
                case "wearapp":

                    if(name.getPackName().equals(getApplicationContext().getPackageName())) {
                        hintText.setText(R.string.gesture_open_main);

                        //delayed opening
                        task=new TimerTask() {@Override public void run() {
                            Intent intent = new Intent(GesturePerformActivity.this,MainActivity.class); //spilt 2 是pakagename
                            finish();
                            startActivity(intent);
                        }};



                    }else {
                        hintText.setText(String.format(getString(R.string.gesture_opening), name.getFilteredName()));

                        //delayed opening
                        task=new TimerTask() {@Override public void run() {
                                Intent intent = getPackageManager().getLaunchIntentForPackage(name.getPackName()); //spilt 2 是pakagename
                                startActivity(intent);
                            }};


                    }


                    break;


                case "timer":
                    hintText.setText(name.getFilteredName());
                    //delayed opening
                    task=new TimerTask() {@Override public void run() {
                        timerOpen(name.getPackName());
                    }};

                    break;


                case "call":

                    hintText.setText(name.getFilteredName());

                    //delayed opening
                    task=new TimerTask() {@Override public void run() {
                        callOpen(name.getPackName());
                    }};

                    break;


                case "mapp":
                    hintText.setText(String.format(getString(R.string.gesture_open_phone), name.getFilteredName()));
                    if(delay==0){delay=1000;}
                    task=new TimerTask() {@Override public void run() {
                        mobileOpen(name.getOriginalName());
                        moveTaskToBack(true);
                    }};
                    break;

                case "tasker":
                    if(delay==0){delay=1000;}
                    hintText.setText(String.format(getString(R.string.gesture_open_phone), name.getFilteredName()));
                    task=new TimerTask() {@Override public void run() {
                        mobileOpen(name.getOriginalName());
                        moveTaskToBack(true);
                    }};
                    break;


            }

        }catch (Exception e){
            msg(getString(R.string.gesture_failed)+name.getFilteredName());
            //Analytics
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("PerformActivity")
                    .setAction("failToOpenMatch")
                    .setLabel(activity)
                    .build());
            //-----------------------
            return;
        }

        if(task!=null) {
            timer.schedule(task, delay);//Timer Action
        }


          //moveTaskToBack(true);//推出 TODO does this matter??

    }


    public void timerOpen(String method) {
        Intent intent = null;
        switch (method) {
            case "Alarm":
                intent = new Intent(AlarmClock.ACTION_SET_ALARM);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                break;
            case "Timer":
                intent = new Intent(AlarmClock.ACTION_SET_TIMER);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                break;
            case "Stopwatch":
                intent = new Intent("com.google.android.wearable.action.STOPWATCH");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                break;
            case "Alarm List":
                intent = new Intent("android.intent.action.SHOW_ALARMS");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);//
                break;

        }
      if (intent != null){
    //UNKNOWN: What was this here to prevent? Add answer in group collab space.
//      if(intent.resolveActivity(getPackageManager()) != null) {
        startActivity(intent);
      }
    }


    public void callOpen(String phonenumber){
        Uri number = Uri.parse("tel:" + phonenumber);
        Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
        startActivity(callIntent);
    }


    public void mobileOpen(String action){
        sendMobileAction(wearConnect,action);
    }


    //==============================================================================================



//    //============================================================================================== 初始状态
//    public void refreashLayout(){
//
//            hintText.setText(R.string.draw_your_pattern);
//            mButtonClose.setVisibility(View.VISIBLE);
//
//    }

    //==============================================================================================


    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }



    @Override
    protected void onPause() {
//        msg("Killed");

//        if (!this.isFinishing()){
//            //TODODONE find the problem caused which after user click the home button, the launching speed will be slow(Possible solution: swipe to open)
////            msg("This action would take longer to load next time, please use cancel button instead.");
//            Intent intent = new Intent(getApplicationContext(),GesturePerformActivity.class);
//
//                intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
//
//            startActivity(intent);
//
//        }

        finishAndRemoveTask();
        super.onPause();



    }


//----------------------------------------------------------------------------------------------------------------

    public void msg(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        Log.v(tag, message);
    }
}