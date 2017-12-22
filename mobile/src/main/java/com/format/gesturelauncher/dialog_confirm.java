package com.format.gesturelauncher;

import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.Prediction;
import android.os.CountDownTimer;
import android.provider.AlarmClock;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import static com.format.gesturelauncher.MainActivity.main;
import static com.format.gesturelauncher.MainActivity.mobileconnect;
import static com.format.gesturelauncher.MobileConnectService.Sync;
import static com.format.gesturelauncher.MobileConnectService.lib;

public class dialog_confirm extends AppCompatActivity {

//    GestureLibrary lib; //手势库
    static TextView nameEd;
//    static NameFilter action;
    Button next;


    String MethodNameForReturn;
    String filteredName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_confirm);


        Button redraw =(Button) findViewById(R.id.buttonRedraw);
         next =(Button) findViewById(R.id.buttonNext);
        nameEd = (TextView) findViewById(R.id.textAction);

//        final File mStoreFile = new File(getFilesDir(), "gesturesNew");
//
////        lib= GestureLibraries.fromRawResource(this,R.raw.gesturesm);//导入手势
//        lib= GestureLibraries.fromFile(mStoreFile);//导入手势
//
//        if (!lib.load()) {          //必须要这
//            Toast.makeText(this,"Warning: Gesture Library unload", Toast.LENGTH_SHORT).show();
//            finish();
//        }

//        action=null;


        //---------------------------接受信息+图片
        Intent intent = getIntent();
        final Gesture gesture =  intent.getParcelableExtra("gesture");
        MethodNameForReturn=intent.getStringExtra("method");
        filteredName=intent.getStringExtra("name");

        ImageView image =(ImageView) findViewById(R.id.imageView);
        image.setImageBitmap(gesture.toBitmap(200,200,10, ContextCompat.getColor(this,R.color.colorAccent)));
        nameEd.setText(filteredName);

        collisionCheck(gesture);



        //--------------------------------按钮事件
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MethodNameForReturn != null){
                    Toast.makeText(getApplicationContext(),"Saving...",Toast.LENGTH_SHORT).show();
                    next.setEnabled(false);
                    Sync(mobileconnect,false);

                    new CountDownTimer(7000,100){

                        public void onTick(long l) {
                            if(main.notsync.getVisibility()!=View.VISIBLE){
                                Toast.makeText(getApplicationContext(),"Saved!",Toast.LENGTH_SHORT).show();
                                lib.addGesture(MethodNameForReturn,gesture);
                                lib.save();
                                Sync(mobileconnect,true);
                                cancel();
//                                finish();
                                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP );
                                startActivity(intent);

                            }
//                            Log.v(TAG,"count"+notsync.getVisibility());
                        }

                        public void onFinish() {
                            Toast.makeText(getApplicationContext(),"Gesture didn't save, wearable sync failed, please try again",Toast.LENGTH_SHORT).show();
                            next.setEnabled(true);

                        }
                    }.start();





                }else {
                    Toast.makeText(getApplicationContext(),"You must choose an action",Toast.LENGTH_SHORT).show();

                }
            }
        });

        redraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(),GestureActivity.class);
                intent.putExtra("method",MethodNameForReturn);
                intent.putExtra("name",filteredName);
                startActivity(intent);
                finish();
            }
        });




    }



    public void collisionCheck(Gesture gesture){
        TextView text = (TextView) findViewById(R.id.textViewCollision);

        ArrayList<Prediction> predictionArrayList = lib.recognize(gesture);  //手势识别到列表

        double maxfound = 0.0;
        String maxName = "";


        for(Prediction prediction:predictionArrayList){

            Log.v("fzg",prediction.toString() + "---" + Double.toString(prediction.score));


            if(prediction.score > maxfound){
                maxfound=prediction.score;
                maxName=prediction.name;
            }

        }

        if(maxfound > 2.0){

            String gesturename=new NameFilter(maxName).GetfiltedName();
            int smilarity = (int) ((maxfound/4)*100);

            text.setText("Possible gesture collision:\n"+ gesturename +" - Similarity "+smilarity +"%");


        }else {
            text.setText("Possible gesture collision:\nGreat! No similar gestures found");


        }
    }


//    public static void setActivity(String activity){
//        action= new NameFilter(activity); //设置一个新的Namefilter（自建）
//        nameEd.setText(action.GetfiltedName());
//    }//用于外部访问，设置activity to use




}
