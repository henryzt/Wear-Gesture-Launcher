package com.format.gesturelauncher;

import android.app.ProgressDialog;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.Prediction;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;

import static com.format.gesturelauncher.MainActivity.main;
import static com.format.gesturelauncher.MainActivity.mobileconnect;
import static com.format.gesturelauncher.MobileConnectService.Sync;
import static com.format.gesturelauncher.MobileConnectService.lib;

public class dialog_confirm extends AppCompatActivity {

//    GestureLibrary lib;
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




        //---------------------------get extra & image
        Intent intent = getIntent();
        final Gesture gesture =  intent.getParcelableExtra("gesture");
        MethodNameForReturn=intent.getStringExtra("method");
        filteredName=intent.getStringExtra("name");

        ImageView image =(ImageView) findViewById(R.id.imageView);
        image.setImageBitmap(gesture.toBitmap(200,200,10, ContextCompat.getColor(this,R.color.colorAccent)));
        nameEd.setText(filteredName);

        collisionCheck(gesture);



        //--------------------------------button action
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Analytics
                final Tracker mTracker;
                AnalyticsApplication application = (AnalyticsApplication) getApplication();
                 mTracker = application.getDefaultTracker();

                if(MethodNameForReturn != null){
                    final ProgressDialog dialog = ProgressDialog.show(dialog_confirm.this, "",
                            getString(R.string.gesture_saving), true); //DIALOG saving...

                    next.setEnabled(false);
                    Sync(mobileconnect,false);

                    new CountDownTimer(7000,100){

                        public void onTick(long l) {
                            if(main.notsync.getVisibility()!=View.VISIBLE){


                                mTracker.send(new HitBuilders.EventBuilder()
                                        .setCategory("Mobile Action")
                                        .setAction("newGestureAdded")
                                        .setLabel(MethodNameForReturn)
                                        .build());
                                //-----------------------

                                dialog.dismiss();
                                Toast.makeText(getApplicationContext(), R.string.gesture_saved,Toast.LENGTH_SHORT).show();
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
                            //Analytics
                            mTracker.send(new HitBuilders.EventBuilder()
                                    .setCategory("Mobile Error")
                                    .setAction("gestureAddFail")
                                    .setLabel(MethodNameForReturn)
                                    .build());

                            dialog.dismiss();
                            Toast.makeText(getApplicationContext(), R.string.confirm_gesture_save_fail,Toast.LENGTH_SHORT).show();
                            next.setEnabled(true);

                        }
                    }.start();





                }else {
                    Toast.makeText(getApplicationContext(), R.string.confirm_must_choose,Toast.LENGTH_SHORT).show();

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

        ArrayList<Prediction> predictionArrayList = lib.recognize(gesture);  //gesture recognized

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
            int smilarity = (int) ((maxfound/4)*100);

            text.setText(String.format(getString(R.string.confirm_collision_similar), gesturename, smilarity));


        }else {
            text.setText(R.string.confirm_collision_clear);


        }
    }






}
