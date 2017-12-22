package com.format.gesturelauncher;

import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.Prediction;
import android.graphics.Color;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.ConfirmationOverlay;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static com.format.gesturelauncher.MainActivity.wearconnect;
import static com.format.gesturelauncher.WearConnectService.lib;
import static com.format.gesturelauncher.WearConnectService.sendMobile;

public class AddConfirmGesture extends WearableActivity {


    private BoxInsetLayout mContainerView;
    String MethodNameForReturn;
    String filterdName;
    Gesture gesture;

    TextView action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_confirm_gesture);
        setAmbientEnabled();

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);

        MethodNameForReturn= getIntent().getStringExtra("method");
        filterdName=getIntent().getStringExtra("name");
        gesture=getIntent().getParcelableExtra("gesture");

        action=(TextView) findViewById(R.id.textViewAction);
        action.setText(filterdName);

        ImageView image =findViewById(R.id.imageViewConfirm);
        image.setImageBitmap(gesture.toBitmap(150,150,10, Color.YELLOW));

        collisionCheck(gesture);


        findViewById(R.id.buttonConfirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                lib.addGesture(MethodNameForReturn,gesture);
                lib.save();

//                Toast.makeText(getApplicationContext(),"Gesture saved!",Toast.LENGTH_SHORT).show();


                final Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent.putExtra("message","Gesture saved");
                intent.putExtra("extra","notini");
//                intent.putExtra("main","main");


                new ConfirmationOverlay().setFinishedAnimationListener(new ConfirmationOverlay.FinishedAnimationListener() {
                    @Override
                    public void onAnimationFinished() {
                        startActivity(intent);
                        finish();
                        sendMobile(wearconnect);

                    }
                })
                        .setMessage("Gesture saved")
                        .setType(ConfirmationOverlay.SUCCESS_ANIMATION)
                        .showOn(AddConfirmGesture.this);



            }
        });

        findViewById(R.id.buttonConfirmCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               finish();
            }
        });
    }


    public void collisionCheck(Gesture gesture){
        TextView text = (TextView) findViewById(R.id.textCollision);

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
            int smilarity = (int) ((maxfound/5)*100);

            text.setText(" - Collision check - \n"+ gesturename +" - Similarity "+smilarity +"%");


        }else {
            text.setText(" - Collision check - \nGreat! No similar gestures found");


        }
    }














    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }

    private void updateDisplay() {
        if (isAmbient()) {
            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));

        } else {
            mContainerView.setBackgroundColor(getResources().getColor(R.color.dark_grey));

        }
    }
}
