package com.format.gesturelauncher;

import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureOverlayView;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddGesture extends WearableActivity {

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    private BoxInsetLayout mContainerView;

    String MethodNameForReturn;
    String filterdName;

    TextView indicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_gesture);
        setAmbientEnabled();

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);


        MethodNameForReturn= getIntent().getStringExtra("method");
        filterdName=getIntent().getStringExtra("name");

        indicator=(TextView) findViewById(R.id.textDraw);
        indicator.setText("Draw to set the pattern of action '"+filterdName+"'");


        //=---------------------------------------------手势
        GestureOverlayView gesturer = (GestureOverlayView) findViewById(R.id.gestureDraw);

        gesturer.cancelClearAnimation();
        gesturer.addOnGesturePerformedListener(new GestureOverlayView.OnGesturePerformedListener() {
            @Override
            public void onGesturePerformed(GestureOverlayView gestureOverlayView, Gesture gesture) {

                    sendConfirm(gesture);

            }
        });

        //---------------------------------------------按钮
        findViewById(R.id.buttonBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }



    public void sendConfirm(Gesture gesture){
        Intent confirm = new Intent(this, AddConfirmGesture.class);
        confirm.putExtra("gesture", gesture);
        confirm.putExtra("method",MethodNameForReturn);
        confirm.putExtra("name",filterdName);
        startActivity(confirm);
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
