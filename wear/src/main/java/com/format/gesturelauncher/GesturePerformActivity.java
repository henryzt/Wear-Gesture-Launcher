package com.format.gesturelauncher;

import android.app.Activity;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.AlarmClock;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static com.format.gesturelauncher.MainActivity.APIcompatibleMode;
import static com.format.gesturelauncher.WearConnectService.ShowQuickLauncher;
import static com.format.gesturelauncher.WearConnectService.accuracy;
import static com.format.gesturelauncher.WearConnectService.lib;
import static com.format.gesturelauncher.WearConnectService.vibratorOn;

public class GesturePerformActivity extends Activity {

    private BoxInsetLayout mContainerView;
    private TextView mTextView;
    private TextView mClockView;
//TODO 换mainactivity



    TextView hinttext;
    Button mButtonClose;
    Button mButtonWhat;

    static boolean active = false;//check if this is running

    String TAG ="fzg";

//    public static boolean vibratorOn = true; //振动开关


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gesture_main);



        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            // Activity was brought to front and not created,
            // Thus finishing this will get us to the last viewed activity
//            Msg("Clear");

            finish();
            return;
        }


        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mTextView = (TextView) findViewById(R.id.text);
        mButtonClose = findViewById(R.id.buttonClose);
        mButtonWhat=findViewById(R.id.buttonwhat);
//        mClockView = (TextView) findViewById(R.id.clock);





        //-------------------------------------------------------------------------------

        final Vibrator v = (Vibrator) getSystemService(getApplicationContext().VIBRATOR_SERVICE);
        final long[] wrong = {0,30,100,30,50,30};//振动器
        final long[] right = {0,30,};

        if(vibratorOn){v.vibrate(right,-1);} //Vibrate to show it is open

        GestureOverlayView mGesture = findViewById(R.id.gesture); //定义
        hinttext = findViewById(R.id.text);//定义


        //lib=GestureLibraries.fromRawResource(this,R.raw.gestures);//导入手势




        mGesture.addOnGestureListener(new GestureOverlayView.OnGestureListener() {
            @Override
            public void onGestureStarted(GestureOverlayView gestureOverlayView, MotionEvent motionEvent) {
                hinttext.setVisibility(View.GONE);
                mButtonClose.setVisibility(View.INVISIBLE);
                mButtonWhat.setVisibility(View.GONE);
            }

            @Override
            public void onGesture(GestureOverlayView gestureOverlayView, MotionEvent motionEvent) {

            }

            @Override
            public void onGestureEnded(GestureOverlayView gestureOverlayView, MotionEvent motionEvent) {
                hinttext.setVisibility(View.VISIBLE);
                hinttext.setText("Matching...");
                mButtonClose.setVisibility(View.VISIBLE);

            }

            @Override
            public void onGestureCancelled(GestureOverlayView gestureOverlayView, MotionEvent motionEvent) {
                hinttext.setVisibility(View.VISIBLE);
                hinttext.setText(R.string.draw_your_pattern);
                mButtonClose.setVisibility(View.VISIBLE);
            }
        });  //手势事件s



        mGesture.addOnGesturePerformedListener(new GestureOverlayView.OnGesturePerformedListener() {  //画完后识别手势
            @Override
            public void onGesturePerformed(GestureOverlayView gestureOverlayView, Gesture gesture) {

                if(!lib.load()){
                    //如果没有手势
//                    Toast.makeText(getApplicationContext(), R.string.empty_lib,Toast.LENGTH_LONG).show();
                    hinttext.setText(R.string.empty_lib);
                    if(vibratorOn){v.vibrate(wrong,-1);}
                    return;
                }


                ArrayList<Prediction> predictionArrayList = lib.recognize(gesture);  //手势识别到列表
                boolean matched =false;
                double maxfound = 0.0;
                String maxName = "";



                for(Prediction prediction:predictionArrayList){

                    Log.v(TAG,prediction.toString() + "---" + Double.toString(prediction.score));


                    if(prediction.score > maxfound){
                        maxfound=prediction.score;
                        maxName=prediction.name;
                    }

                }

                if(maxfound > accuracy){//2.0
                    hinttext.setText(maxName +" performed!");
                    //Msg(prediction.toString());
                    matched=true;
                    if(vibratorOn){v.vibrate(right,-1);}
                    MatchOpen(maxName);//尝试打开app

                    //break;

                }else {
                    hinttext.setText("No match");


                }

                if(!matched){

                    if(vibratorOn){v.vibrate(wrong,-1);}

                    mButtonWhat.setVisibility(View.VISIBLE);

//                    Msg("Please try again");
                }

            }
        });




        mButtonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                this.setVisibility(View.GONE);
//                moveTaskToBack(true);
                finish();
            }
        });

        mButtonWhat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent=new Intent(getApplicationContext(),AllGestures.class);
//                intent.putExtra("open","y");
                Intent intent=new Intent(GesturePerformActivity.this,MainActivity.class);
//                if(APIcompatibleMode) {
                    intent.putExtra("extra", "notini");
//                }
                finish();
                startActivity(intent);

            }
        });



        if( APIcompatibleMode==true || !ShowQuickLauncher){
            mButtonWhat.setVisibility(View.VISIBLE);
        }

    }

//=====================================================================================================================================================================================



    //============================================================================================== 打开应用
    public void MatchOpen(String activity){

        NameFilter name = new NameFilter(activity);
        try {
            switch (name.getMethod()) {
                case "wearapp":

                    if(name.getPackName().equals(getApplicationContext().getPackageName())) {
                        hinttext.setText("Opening Main screen");

                        Intent intent = new Intent(GesturePerformActivity.this,MainActivity.class); //spilt 2 是pakagename
                        finish();
                        startActivity(intent);


                    }else {
                        Intent intent = getPackageManager().getLaunchIntentForPackage(name.getPackName()); //spilt 2 是pakagename
                        startActivity(intent);
                        //            Msg("Opening " + spilt[0]);
                        hinttext.setText("Opening " + name.GetfiltedName());
                    }


                    break;


                case "timer":
                    TimerOpen(name.getPackName());
                    hinttext.setText(name.GetfiltedName());
                    break;


                case "call":
                    callOpen(name.getPackName());
                    hinttext.setText(name.GetfiltedName());
                    break;

            }

        }catch (Exception e){
            Msg("Fail to open "+name.GetfiltedName());
            return;
        }

          moveTaskToBack(true);//推出

    }



    public void TimerOpen(String method){
        switch (method) {
            case "Alarm":
                Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
//                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
//                }
                break;
            case "Timer":
                Intent intent2 = new Intent(AlarmClock.ACTION_SET_TIMER);
                intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
//                if (intent2.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent2);
//                }
                break;
            case "Stopwatch":
                Intent intent3 = new Intent("com.google.android.wearable.action.STOPWATCH" );
                intent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
//                if (intent3.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent3);
//                }
                break;
            case "Alarm List":
                Intent intent4 = new Intent("android.intent.action.SHOW_ALARMS");
                intent4.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);//
//                if (intent3.resolveActivity(getPackageManager()) != null) {
                startActivity(intent4);
//                }
                break;

        }
    }


    public void callOpen(String phonenumber){
        Uri number = Uri.parse("tel:" + phonenumber);
        Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
        startActivity(callIntent);
    }




    //==============================================================================================



//    //============================================================================================== 初始状态
//    public void refreashLayout(){
//
//            hinttext.setText(R.string.draw_your_pattern);
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
//        Msg("Killed");

//        if (!this.isFinishing()){
//            //TODODONE find the problem caused which after user click the home button, the launching speed will be slow(Possible solution: swipe to open)
////            Msg("This action would take longer to load next time, please use cancel button instead.");
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

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event)
//    {
//        if(keyCode == KeyEvent.KEYCODE_BACK)
//        {
//            Msg("back");
//            Log.d("Test", "Back button pressed!");
//        }
//        else if(keyCode == KeyEvent.KEYCODE_HOME)
//        {
//            Msg("home");
//            Log.d("Test", "Home button pressed!");
//        }
//        return super.onKeyDown(keyCode, event);
//    }
//----------------------------------------------------------------------------------------------------------------



    public void Msg(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
        Log.v(TAG,message);
    }





}
