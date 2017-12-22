package com.format.gesturelauncher;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.api.Api;

import static com.format.gesturelauncher.MainActivity.APIcompatibleMode;

public class FloaterService extends Service {

    static FrameLayout frameLayoutfloater;

    public FloaterService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        MainActivity.floaterService=this;
        if(!APIcompatibleMode) {
            startFloater();
        }
    }


    //============================================================================================== 悬浮窗


    private void startFloater(){
        //Check if the application has draw over other apps permission or not?
        //This permission is by default available for API<23. But for API > 23
        //you have to ask for the permission in runtime.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&!Settings.canDrawOverlays(this)) {

            //If the draw over permission is not available open the settings screen
            //to grant the permission.

            Msg("To display the quick launcher, please enable the draw over permission");




            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
//           startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
            Msg("Fail to open quick launcher: not allowed");
//            startActivity(intent);

            createFloater();
        } else {
            createFloater();
        }

//        @Override
//        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//            if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {
//
//                //Check if the permission is granted or not.
//                if (resultCode == RESULT_OK) {
//                    initializeView();
//                } else { //Permission is not available
//                    Toast.makeText(this,
//                            "Draw over other app permission not available. Closing the application",
//                            Toast.LENGTH_SHORT).show();
//
//                    finish();
//                }
//            } else {
//                super.onActivityResult(requestCode, resultCode, data);
//            }
//        }
    }


    private void createFloater(){
        if(frameLayoutfloater==null) {
//            Msg("Quick Launcher enabled");//TODO GONE

//            try{
            newFloater();
//            }catch (Exception e){
//        }

        }else {
            Msg("Floater already exits");
        }
    }

    private int getAlertType(){ //For OREO
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_SYSTEM_ALERT : WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
    }

    private void newFloater() {
        //---------------------------------
        SharedPreferences sharedPref = getSharedPreferences("main", MODE_PRIVATE);
        String location = sharedPref.getString("location", "r");
        boolean small=sharedPref.getBoolean("small",false);
        boolean wider=sharedPref.getBoolean("wider",false);
        //----------------------------------


        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                getAlertType(),
//                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);


        int default_width = 35;

        //启动器位置
        int LayoutID =R.layout.layout_quick_vertical;
        switch (location) {
            case "r":
                params.gravity = Gravity.CENTER | Gravity.RIGHT;

                LayoutID=R.layout.layout_quick_vertical;
                params.width=default_width;
                break;
            case "l":
                params.gravity = Gravity.CENTER | Gravity.LEFT;
                LayoutID=R.layout.layout_quick_vertical;
                params.width=default_width;
                break;
            case "t":
                params.gravity = Gravity.TOP | Gravity.CENTER;
                LayoutID=R.layout.layout_quick_horizon;
                params.height = default_width;
                break;
            case "b":
                params.gravity = Gravity.BOTTOM | Gravity.CENTER;
                LayoutID=R.layout.layout_quick_horizon;
                params.height = default_width;
                break;
            default:
                params.gravity = Gravity.CENTER | Gravity.RIGHT;
                break;
        }
        //---------------------

//TODO

        if(small) {
            if (location.equals("r") || location.equals("l")) {
            params.height=180;
//            frameLayoutfloater.setTop(10);
//            frameLayoutfloater.setY(10);
            params.gravity = params.gravity|Gravity.BOTTOM;
            } else {
                params.width = 130;
//                params.gravity = Gravity.RIGHT;
            }
        }

        if(wider){
            if (location.equals("r") || location.equals("l")) {
                params.width=50;
            } else {
                params.height = 50;
            }
        }



        final Context context = getApplicationContext();

        frameLayoutfloater = new FrameLayout(context);

        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(frameLayoutfloater, params);

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // Here is the place where you can inject whatever layout you want. 这个办法是用于添加xml文件
        layoutInflater.inflate(LayoutID, frameLayoutfloater);



//        ImageView sideOpen = new ImageView(this);  //用程序建立一个图片框，用于点击打开
//        sideOpen.setLayoutParams( new FrameLayout.LayoutParams(30,FrameLayout.LayoutParams.MATCH_PARENT));
//        sideOpen.setBackgroundColor(Color.BLUE);
//        frameLayout.addView(sideOpen);

        try {


            final ImageView sideOpen = (ImageView) frameLayoutfloater.findViewById(R.id.sideOpenLayout);   //!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            sideOpen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

//                findViewById(R.id.watch_view).setVisibility(View.GONE);
//                Msg("You clicked!");
//                Intent intent = new Intent(getApplicationContext(),GesturePerformActivity.class);
//                Msg("Launching...");

//                intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
//                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//确保不会被打开两次

//                ActivityOptions options = ActivityOptions.makeCustomAnimation(context, R., R.anim.generic_confirmation_icon_animation);//动画效果

//                refreashLayout();

                    //                startService(new Intent(getApplicationContext(),GesturePerformService.class));

//                Intent intent = new Intent(getApplicationContext(),GesturePerformActivity.class);
//                startActivity(intent);


                    //TODO https://stackoverflow.com/questions/5600084/starting-an-activity-from-a-service-after-home-button-pressed-without-the-5-seco


//                if(active==true){
//                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
//
//                }

                    Intent intent = new Intent(context, GesturePerformActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//TODO|Intent.FLAG_ACTIVITY_CLEAR_TASK
                    PendingIntent pendingIntent =
                            PendingIntent.getActivity(context, 0, intent, 0);
                    try {
                        pendingIntent.send();
                    } catch (PendingIntent.CanceledException e) {
                        Msg("Canceled");
                        e.printStackTrace();
                    }



                }

            });


            sideOpen.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    SharedPreferences sharedPref = getSharedPreferences("main", MODE_PRIVATE);
                    boolean longpress=sharedPref.getBoolean("longpress",true);
                    if(longpress) {
                        stopMe();
                    }
                    return true;
                }
            });




        }catch (Exception e){
//            Msg(e.toString());
            Msg("Floater create failed, retrying...");
            stopMe();
        }




        new CountDownTimer(5000, 20) {


            public void onTick(long millisUntilFinished) {
//                mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);

                if(frameLayoutfloater!=null) {
                    if (millisUntilFinished <= 2000) {
                        frameLayoutfloater.setAlpha((float) (millisUntilFinished / 20) / 100);//设置透明度(/50)/100
                    } else {
                        frameLayoutfloater.setAlpha((float) 1);
                    }
                }
            }

            public void onFinish() {
//                mTextField.setText("done!");
                if(frameLayoutfloater!=null) {
                    frameLayoutfloater.setAlpha((float) 0);
                }
            }
        }.start();




    }

    public void stopMe(){


        Msg("Quick Launcher disabled");
        frameLayoutfloater.removeAllViews();
        frameLayoutfloater=null;
        stopSelf();
    }



    //==============================================================================================

    public void Msg(String message){

            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

        Log.v("fzg",message);
    }
}