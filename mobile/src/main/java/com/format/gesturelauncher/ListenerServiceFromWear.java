package com.format.gesturelauncher;

import android.content.Intent;
import android.widget.Toast;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by 子恒 on 2018/2/12.
 * https://gist.github.com/gabrielemariotti/117b05aad4db251f7534#file-mobile-listenerservicefromwear-java-L5
 */

public class ListenerServiceFromWear extends WearableListenerService {
    private static final String SYNC_PATH = "/sync";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        /*
         * Receive the message from wear
         */
        if (messageEvent.getPath().equals(SYNC_PATH)) {

//            Toast.makeText(getApplicationContext(), "Connection", Toast.LENGTH_LONG).show();
            if (MobileConnectService.alreadyCreated == false) {
                startService(new Intent(getApplicationContext(), MobileConnectService.class));
                Toast.makeText(getApplicationContext(), R.string.receiver_notice, Toast.LENGTH_LONG).show();
            }
        }


    }
}
