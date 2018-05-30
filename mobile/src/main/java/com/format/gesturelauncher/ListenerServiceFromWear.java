package com.format.gesturelauncher;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.nio.charset.Charset;

import static com.format.gesturelauncher.MainActivity.main;

/**
 * Created by 子恒 on 2018/2/12.
 * https://gist.github.com/gabrielemariotti/117b05aad4db251f7534#file-mobile-listenerservicefromwear-java-L5
 */

public class ListenerServiceFromWear extends WearableListenerService {

    private static final String SYNC_PATH = "/sync";
    public Tracker mTracker;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        String path=messageEvent.getPath();

        /*
         * Receive the message from wear
         */



        if (path.equals(SYNC_PATH)) {

//            Toast.makeText(getApplicationContext(), "Connection", Toast.LENGTH_LONG).show();
            if (MobileConnectService.alreadyCreated == false) {
                startService(new Intent(getApplicationContext(), MobileConnectService.class));
//                Toast.makeText(getApplicationContext(), R.string.receiver_notice, Toast.LENGTH_LONG).show();
            }


                byte[] bytes= messageEvent.getData();
                String action = new String(bytes, Charset.forName("UTF-8"));



                openRequest(action);
        }


    }


    //-------------------------------------------------------------------------------Open apps and Tasker

            public void openRequest(String action){
                NameFilter filter= new NameFilter(action);
                if(filter.getMethod().equals("mapp")){
                    Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(filter.getPackName());

                    Log.v("mapp",filter.getPackName());
                    try {
                        startActivity(LaunchIntent);
                        MsgT(String.format(getString(R.string.receiver_open_m_app), filter.getFilteredName()));
                    }catch (Exception e){
                        MsgT(String.format(getString(R.string.receiver_open_app_failed), filter.getFilteredName()));
                        e.printStackTrace();
                    }


                }else if(filter.getMethod().equals("tasker")){
                    runTakser(filter.getPackName());
                    MsgT(String.format(getString(R.string.receiver_tasker_task), filter.getFilteredName()));
                }

//                //Analytics
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Mobile Sync")
                        .setAction("mobileAction")
                        .setLabel(action)
                        .build());

            }

            public void runTakser(String taskName){
                if ( TaskerIntent.testStatus( getApplicationContext() ).equals( TaskerIntent.Status.OK ) ) {
                    TaskerIntent i = new TaskerIntent( taskName);
                    sendBroadcast( i );
                }else {
                    Toast.makeText(getApplicationContext(),getString(R.string.receiver_tasker_error)+TaskerIntent.testStatus( getApplicationContext() ).toString() , Toast.LENGTH_LONG).show();//"Sorry, please check your Tasker preference to open external access"
                    //Analytics
                    mTracker.send(new HitBuilders.EventBuilder()
                            .setCategory("Mobile Tasker")
                            .setAction("taskerError")
                            .setLabel(TaskerIntent.testStatus( getApplicationContext() ).toString())
                            .build());
                }
            }

    //===============================================================



    //-----------------------------------------------------------------------


    public void MsgT(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }



}
