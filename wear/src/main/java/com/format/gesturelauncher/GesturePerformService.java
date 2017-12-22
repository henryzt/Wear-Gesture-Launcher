package com.format.gesturelauncher;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.wearable.view.BoxInsetLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

//TODO Not in use
public class GesturePerformService extends Service {

    static FrameLayout gestureperfromview;




    public GesturePerformService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createView();
    }



    public void createView(){
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
//                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.width= FrameLayout.LayoutParams.MATCH_PARENT;
        params.height= FrameLayout.LayoutParams.MATCH_PARENT;
        final Context context = getApplicationContext();

        gestureperfromview = new BoxInsetLayout(context);

        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(gestureperfromview, params);

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // Here is the place where you can inject whatever layout you want. 这个办法是用于添加xml文件
        layoutInflater.inflate(R.layout.gesture_main, gestureperfromview);

//        gestureperfromview.setAlpha((float) 0.8);//设置透明度


        final Button close = (Button) gestureperfromview.findViewById(R.id.buttonClose);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gestureperfromview.removeAllViews();
                gestureperfromview=null;
                stopSelf();

            }

        });


    }
}
