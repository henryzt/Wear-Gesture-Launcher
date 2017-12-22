package com.format.gesturelauncher;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.service.carrier.CarrierMessagingService;
import android.support.annotation.NonNull;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.ConfirmationOverlay;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Wearable;
import com.google.android.wearable.intent.RemoteIntent;
import com.google.android.wearable.playstore.PlayStoreAvailability;

import static com.format.gesturelauncher.FloaterService.frameLayoutfloater;
import static com.format.gesturelauncher.MainActivity.wearconnect;
import static com.format.gesturelauncher.WearConnectService.MOBILE_VERSION;
import static com.format.gesturelauncher.WearConnectService.WEAR_VERSION;
import static com.format.gesturelauncher.WearConnectService.reload;

public class Help extends WearableActivity {


    private BoxInsetLayout mContainerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        setAmbientEnabled();

        mContainerView = (BoxInsetLayout) findViewById(R.id.watch_view);




        TextView version =findViewById(R.id.textViewVesion);

        Button mButton = (Button) findViewById(R.id.buttonPhone);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAppInStoreOnPhone();
            }
        });

        findViewById(R.id.buttonRate2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_APP_URI)));
            }
        });

        version.setText("Wear version code: "+WEAR_VERSION+"\nVersion name: "+ BuildConfig.VERSION_NAME+"\nMobile version code: "+MOBILE_VERSION);


        if(frameLayoutfloater!=null) {
            frameLayoutfloater.setAlpha((float) 0.5);
        }
    }








//=======================================================================================
    private static final String TAG = "MainWearActivity";

    private static final String PLAY_STORE_APP_URI =
            "market://details?id=com.format.gesturelauncher";

    // TODO: Replace with your links/packages.
    private static final String APP_STORE_APP_URI =
            "https://itunes.apple.com/us/app/android-wear/id986496028?mt=8";


//---------------------------------------------
    private void openAppInStoreOnPhone() {
        Log.d(TAG, "openAppInStoreOnPhone()");

        int playStoreAvailabilityOnPhone =
                PlayStoreAvailability.getPlayStoreAvailabilityOnPhone(getApplicationContext());

        switch (playStoreAvailabilityOnPhone) {

            // Android phone with the Play Store.
            case PlayStoreAvailability.PLAY_STORE_ON_PHONE_AVAILABLE:
                Log.d(TAG, "\tPLAY_STORE_ON_PHONE_AVAILABLE");

                // Create Remote Intent to open Play Store listing of app on remote device.
                Intent intentAndroid =
                        new Intent(Intent.ACTION_VIEW)
                                .addCategory(Intent.CATEGORY_BROWSABLE)
                                .setData(Uri.parse(PLAY_STORE_APP_URI));

                RemoteIntent.startRemoteActivity(
                        getApplicationContext(),
                        intentAndroid,
                        mResultReceiver);
                break;

            // Assume iPhone (iOS device) or Android without Play Store (not supported right now).
            case PlayStoreAvailability.PLAY_STORE_ON_PHONE_UNAVAILABLE:
                Log.d(TAG, "\tPLAY_STORE_ON_PHONE_UNAVAILABLE");

                // Create Remote Intent to open App Store listing of app on iPhone.
                Intent intentIOS =
                        new Intent(Intent.ACTION_VIEW)
                                .addCategory(Intent.CATEGORY_BROWSABLE)
                                .setData(Uri.parse(APP_STORE_APP_URI));

                RemoteIntent.startRemoteActivity(
                        getApplicationContext(),
                        intentIOS,
                        mResultReceiver);
                break;

            case PlayStoreAvailability.PLAY_STORE_ON_PHONE_ERROR_UNKNOWN:
                Log.d(TAG, "\tPLAY_STORE_ON_PHONE_ERROR_UNKNOWN");
                break;
        }
    }


    // Result from sending RemoteIntent to phone to open app in play/app store.
    private final ResultReceiver mResultReceiver = new ResultReceiver(new Handler()) {
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if (resultCode == RemoteIntent.RESULT_OK) {
                new ConfirmationOverlay()
                        .setType(ConfirmationOverlay.OPEN_ON_PHONE_ANIMATION)
                        .showOn(Help.this);

            } else if (resultCode == RemoteIntent.RESULT_FAILED) {
                new ConfirmationOverlay()
                        .setMessage("Phone disconnected")
                        .setType(ConfirmationOverlay.FAILURE_ANIMATION)
                        .showOn(Help.this);

            } else {
                throw new IllegalStateException("Unexpected result " + resultCode);
            }
        }
    };





//
//    private void checkIfPhoneHasApp() {
//        Log.d(TAG, "checkIfPhoneHasApp()");
//
//        PendingResult<CapabilityApi.GetCapabilityResult> pendingResult =
//                Wearable.CapabilityApi.getCapability(
//                        mGoogleApiClient,
//                        CAPABILITY_PHONE_APP,
//                        CapabilityApi.FILTER_ALL);
//
//        pendingResult.setResultCallback(new CarrierMessagingService.ResultCallback<CapabilityApi.GetCapabilityResult>() {
//
//            @Override
//            public void onResult(@NonNull CapabilityApi.GetCapabilityResult getCapabilityResult) {
//                Log.d(TAG, "onResult(): " + getCapabilityResult);
//
//                if (getCapabilityResult.getStatus().isSuccess()) {
//                    CapabilityInfo capabilityInfo = getCapabilityResult.getCapability();
//                    mAndroidPhoneNodeWithApp = pickBestNodeId(capabilityInfo.getNodes());
//                    verifyNodeAndUpdateUI();
//
//                } else {
//                    Log.d(TAG, "Failed CapabilityApi: " + getCapabilityResult.getStatus());
//                }
//            }
//        });
//    }





//=======================================================================================

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


    @Override
    protected void onDestroy() {
        super.onDestroy();

//            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        Intent intent=new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("extra","notini");
//        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(frameLayoutfloater!=null) {
            frameLayoutfloater.setAlpha((float) 0);
        }
    }
}
