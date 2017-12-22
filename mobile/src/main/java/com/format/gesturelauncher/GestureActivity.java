package com.format.gesturelauncher;

import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureOverlayView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import static com.format.gesturelauncher.MobileConnectService.wearPackList;

public class GestureActivity extends AppCompatActivity {

    String MethodNameForReturn;
    String filteredName;


//    public Bitmap newBitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gesture);

        TextView indicator = (TextView)findViewById(R.id.textViewInd);


        MethodNameForReturn=getIntent().getStringExtra("method");
        filteredName=getIntent().getStringExtra("name");
        indicator.setText("Please draw the gesture for action '"+filteredName+"'");





        GestureOverlayView gesturer = (GestureOverlayView) findViewById(R.id.gestureOverlayView);

        gesturer.cancelClearAnimation();

        gesturer.addOnGesturePerformedListener(new GestureOverlayView.OnGesturePerformedListener() {
            @Override
            public void onGesturePerformed(GestureOverlayView gestureOverlayView, Gesture gesture) {
//                newBitmap = gesture.toBitmap(50,50,10, Color.BLUE);
//                ImageView viewtest =(ImageView) findViewById(R.id.imageViewTest);
//                viewtest.setImageBitmap(newBitmap);

//                sendConfirm(gesture.toBitmap(150,150,10, Color.BLUE));
                if(wearPackList==null){ //if not synced
                    finish();
                    Toast.makeText(getApplicationContext(), "Fail to fetch app list, please connect to wearable to sync and try again", Toast.LENGTH_LONG).show();
                }else {
                    sendConfirm(gesture);
                    finish();
                }
            }
        });


        findViewById(R.id.buttonBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


    }

    public void sendConfirm(Gesture gesture){
        Intent intent2 = new Intent(this, dialog_confirm.class);
        intent2.putExtra("gesture", gesture);
        intent2.putExtra("method",MethodNameForReturn);
        intent2.putExtra("name",filteredName);
        startActivity(intent2);
    }


}
