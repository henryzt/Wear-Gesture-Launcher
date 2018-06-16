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

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;

import static com.format.gesturelauncher.MainActivity.wearConnect;
import static com.format.gesturelauncher.WearConnectService.lib;
import static com.format.gesturelauncher.WearConnectService.sendMobile;

public class AddConfirmGesture extends WearableActivity {


    private BoxInsetLayout mContainerView;
    String methodNameForReturn;
    String filterdName;
    Gesture gesture;

    TextView action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_confirm_gesture);
        setAmbientEnabled();

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);

        methodNameForReturn = getIntent().getStringExtra("method");
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

                lib.addGesture(methodNameForReturn,gesture);
                lib.save();


                //Analytics
                Tracker mTracker;
                AnalyticsApplication application = (AnalyticsApplication) getApplication();
                mTracker = application.getDefaultTracker();
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Wearable Action")
                        .setAction("newGestureAdded")
                        .setLabel(methodNameForReturn)
                        .build());

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
                        sendMobile(wearConnect);

                    }
                })
                        .setMessage(getString(R.string.gesture_saved))
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

        ArrayList<Prediction> predictionArrayList = lib.recognize(gesture);

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

            String gesturename=new NameFilter(maxName).getFilteredName();
            int smilarity = (int) ((maxfound/5)*100);

            text.setText(String.format(getString(R.string.confirm_collision_similar), gesturename, smilarity));//" - Collision check - \n"+ gesturename +" - Similarity "+smilarity +"%");


        }else {
            text.setText(getString(R.string.confirm_collision_clear));


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
