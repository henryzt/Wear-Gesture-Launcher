package com.format.gesturelauncher;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.graphics.Color;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
//import com.google.android.wearable.intent.RemoteIntent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.format.gesturelauncher.MainActivity.main;
import static com.format.gesturelauncher.MainActivity.mobileconnect;
import static com.format.gesturelauncher.MainActivity.welcomedialog;
import static com.format.gesturelauncher.MobileConnectService.Sync;
import static com.format.gesturelauncher.MobileConnectService.lib;
import static com.format.gesturelauncher.MobileConnectService.mGoogleApiClient;

public class WelcomeActivity extends AppCompatActivity {

//    GestureLibrary libfromFile; //手势库
//    GestureLibrary libInitial; //初始手势库
//    public final static String TAG = "fz"; //调试tag
//    String packNameWhichExists;
//
//
//    String WEARABLE_PATH = "/initiate"; //发送位置
//    String MOBILE_PATH = "/receive";//接收位置
//    public static String[] PackNamesFromWearable; //手表的程序包列表
//    int colorAccent=Color.rgb(225,171,0);

    Button buttonStart;
    Button buttonCant;


    boolean finished=false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);


        buttonStart=(Button)findViewById(R.id.buttonGetStart);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!finished){
                    buttonCant.setVisibility(View.VISIBLE);
//                    sendDataMapToDataLayer();
//                    MsgS(getString(R.string.intro_connecting),Snackbar.LENGTH_LONG);//"Connecting... Please make sure the app is opened on the watch"


                    connectAndSync();

                    // Initial request for devices with our capability, aka, our Wear app installed.
//                    findWearDevicesWithApp();

                    // Initial request for all Wear devices connected (with or without our capability).
                    // Additional Note: Because there isn't a listener for ALL Nodes added/removed from network
                    // that isn't deprecated, we simply update the full list when the Google API Client is
                    // connected and when capability changes come through in the onCapabilityChanged() method.
//                    findAllWearDevices();


                }else {
                     ;
//                    help.putExtra("image","help");
//                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                    welcomedialog();
                    finish();
                }

            }
        });



        buttonCant=(Button)findViewById(R.id.buttonCant);
        buttonCant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent help = new Intent(getApplicationContext(),HelpActivity.class);
                help.putExtra("image","connect");
                startActivity(help);
            }
        });




    }


    public void connectAndSync(){

        final ProgressDialog dialog = ProgressDialog.show(WelcomeActivity.this, "",
                getString(R.string.intro_connecting), true); //DIALOG

        mobileconnect.Overwrite=false;
        mobileconnect.sendDataMapToDataLayer("/gestures");
        Sync(mobileconnect,false);

        new CountDownTimer(10000,100){

            public void onTick(long l) {
                if(lib.load()){
                    dialog.dismiss();
                    checkFile();
                    cancel();
                }

            }

            public void onFinish() {
//                MsgS("Wearable no respond, please try again",Snackbar.LENGTH_SHORT);
                faildialog();
                dialog.dismiss();

            }
        }.start();
    }




    public void checkFile(){
        //基本复制from MainActivity
//        //-------------------------------------------手势
//
//        final File mStoreFile = new File(getFilesDir(), "gesturesNew");
//
//        if(!mStoreFile.exists()) { //如果文件不存在就创建一个
//            try {
//                FileOutputStream stream = new FileOutputStream(mStoreFile);
//                stream.write("".getBytes());
//                stream.close();
////                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(mStoreFile);
////                outputStreamWriter.write(data);
////                outputStreamWriter.close();
//            } catch (IOException e) {
//                Log.e("Exception", "File write failed: " + e.toString());
//            }
//        }
//
//
//        libfromFile = GestureLibraries.fromFile(mStoreFile);//从文件导入手势
////        if (!libfromFile.load()) {    //如果加载不成功（文件为空）
//
//            libInitial= GestureLibraries.fromRawResource(this,R.raw.gestureini );//从raw文件导入预录的手势
//
//            if(!libInitial.load()){ //如果预录手势加载不成功
//                Toast.makeText(this,"Fatal error (Initial gesture not found). Please contact the developer.",Toast.LENGTH_LONG).show();
//                finish();
//            }
//
//
//
//
//
//            //-------------------------------------------------------------------------------------------------------
//            Set<String> gestureNameSet = libInitial.getGestureEntries(); //获得所有手势的名称
//            for (String gestureName : gestureNameSet) { //每一个名称，做
//
//                ArrayList<Gesture> gesturesList = libInitial.getGestures(gestureName);//获得这个名称里的手势（可能会有多个）
//
//                Log.d(TAG, gestureName);
//
//                NameFilter filter = new NameFilter(gestureName);//自己声明的，用于去掉##后的内容
//
//                for (Gesture gesture : gesturesList) { //意义不明，可能有多个手势
//
//                    if(packExists(filter.getPackName())){
//                        libfromFile.addGesture(gestureName,gesture); //加入lib
//                        packNameWhichExists = packNameWhichExists + filter.GetfiltedName() +", ";
//                    }
//
//                }
//            }


            if(lib.load()) {  //当手势文件导入成功后（初始化完成），改变界面
//                TextView title = (TextView) findViewById(R.id.textViewtitle);
                ImageView logo = (ImageView) findViewById(R.id.imageViewlogo);

                TextView intro = (TextView) findViewById(R.id.textViewintro);
                ConstraintLayout mLayout = (ConstraintLayout) findViewById(R.id.mWelcome);
//                title.setText("You are ready!");
                intro.setText(R.string.intro_text2);
//                title.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary) );
//                mLayout.setBackgroundColor(ContextCompat.getColor(this,R.color.colorPrimary));
//                buttonStart.setBackgroundColor(ContextCompat.getColor(this,R.color.colorAccent));
//                buttonStart.setBackgroundColor(Color.rgb(255,145,0));
//                mLayout.setBackgroundColor(Color.rgb(204,255,144));

                buttonStart.setText("Go!");
                buttonCant.setVisibility(View.GONE);
                logo.setImageDrawable(getResources().getDrawable(R.drawable.ok));

                finished = true;
                MsgS("Connected!",Snackbar.LENGTH_LONG);//"Connecting... Please make sure the app is opened on the watch"
            }
//            }else{
//                MsgT("Error: fail to save gesture library");
//                finish();
//            }
//
//            if(!libfromFile.load()){   //再确定是否已经成功添加
//                MsgT("No gestures found, failsafe gesture is added.");
//                libfromFile.addGesture("Test",libInitial.getGestures("WearTest##wearapp##com.format.weartest").get(0));
//                libfromFile.save();
//            }
//        }



    }


    public void faildialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(WelcomeActivity.this);
        builder.setTitle("No respond");
        builder.setMessage("Wearable no respond, please make sure the wearable app is installed and opened on your watch, and try again.")
//                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //do things
                    }
                })
                .setNeutralButton("Install now", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent help = new Intent(getApplicationContext(),HelpActivity.class);
                        help.putExtra("image","connect");
                        startActivity(help);
                        //do things
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }




//    public boolean packExists(String packageName){
//        for(String packNameWear : PackNamesFromWearable){  //遍历手表程序表
//            if(packNameWear.equals(packageName)){  //如果这个packName在里面有的话
//                for(String packNameFile : libfromFile.getGestureEntries()){ //遍历已存在于文件里的手势
//                    if(packNameFile.equals(packageName)){ //如果手势已经存在于文件里
//                        return false;  //表示不需要再次添加
//                    }
//                }
//
//                return true;//没有，那么就添加
//            }
//        }
//        return false;//不存在于手表里，不需要添加
//    }


//    //=============================================================================发送数据给手表
//
//    public void sendDataMapToDataLayer() {
//        if (mGoogleApiClient.isConnected()) {
//            DataMap dataMap = createDatamap();
//            new SendDataMapToDataLayer(WEARABLE_PATH, dataMap).start();
//        }
//    } //传送数据给手表
//
//    private DataMap createDatamap() {
//        DataMap dataMap = new DataMap();
//
//        dataMap.putString("Time", Long.toString(System.currentTimeMillis()) ); //同步时间，改变数据以完成同步
//        return dataMap;
//    }
//
//
//    public class SendDataMapToDataLayer extends Thread {
//        String path;
//        DataMap dataMap;
//
//        public SendDataMapToDataLayer(String path, DataMap dataMap) {
//            this.path = path;
//            this.dataMap = dataMap;
//        }
//
//        public void run() {
//
//            PutDataMapRequest putDataMapReq = PutDataMapRequest.create(WEARABLE_PATH);
//            putDataMapReq.getDataMap().putAll(dataMap);
//            PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
//            PendingResult<DataApi.DataItemResult> pendingResult =
//                    Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
//
//            Log.v(TAG, "Sent!");
////
//        }
//    }


    //=============================================================================



//
//    //-----------------------------------------------------------------------RECEIVE 接收手表的确认数据
//    @Override
//    public void onDataChanged(DataEventBuffer dataEvents) {  //数据改变时
//        for (DataEvent event : dataEvents) {
//
//            PutDataMapRequest putDataMapRequest =
//                    PutDataMapRequest.createFromDataMapItem(DataMapItem.fromDataItem(event.getDataItem()));//!!!!!!!!!!!!!!!!https://medium.com/@manuelvicnt/android-wear-accessing-the-data-layer-api-d64fd55982e3
//
//            if (event.getType() == DataEvent.TYPE_CHANGED) {
//                // DataItem changed
//                DataItem item = event.getDataItem();
//
//                DataMap map = putDataMapRequest.getDataMap();
//                PackNamesFromWearable = map.getStringArray("packList");
//
//
//                PATH=item.getUri().getPath();
//                if(PATH.equals( MOBILE_PATH )){   //如果路径等于receive（确保收到的不是自己的而是手表发过来的）
//                    MsgS("Wearable connected!",Snackbar.LENGTH_SHORT);
//                    checkFile();  //检查文件
//                }
//
//
//            } else if (event.getType() == DataEvent.TYPE_DELETED) {
//                // DataItem deleted
//            }
//        }
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        mGoogleApiClient.connect();
//    }
//
//    @Override
//    public void onConnected(Bundle bundle) {
//        Wearable.DataApi.addListener(mGoogleApiClient, this);
//
//    }
//
//    @Override
//    public void onConnectionSuspended(int i) {
//        MsgT("Connection suspended");
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        Wearable.DataApi.removeListener(mGoogleApiClient, this);
//        mGoogleApiClient.disconnect();
//    }
//
//    @Override
//    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//        MsgT("Connection failed");
//    }

    //-----------------------------------------------------------------------


    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        moveTaskToBack(true);
    }




//
//    //====================================================
//    private static final String TAG = "fzg";
//
//    private static final String CAPABILITY_WEAR_APP = "verify_remote_example_wear_app";
//
//    private Set<Node> mWearNodesWithApp;
//    private List<Node> mAllConnectedNodes;
//
//
//    private static final String WELCOME_MESSAGE = "Welcome to our Mobile app!\n\n";
//
//    private static final String CHECKING_MESSAGE =
//            WELCOME_MESSAGE + "Checking for Wear Devices for app...\n";
//
//    private static final String NO_DEVICES =
//            WELCOME_MESSAGE
//                    + "You have no Wear devices linked to your phone at this time.\n";
//
//    private static final String MISSING_ALL_MESSAGE =
//            WELCOME_MESSAGE
//                    + "You are missing the Wear app on all your Wear Devices, please click on the "
//                    + "button below to install it on those device(s).\n";
//
//    private static final String INSTALLED_SOME_DEVICES_MESSAGE =
//            WELCOME_MESSAGE
//                    + "Wear app installed on some your device(s) (%s)!\n\nYou can now use the "
//                    + "MessageApi, DataApi, etc.\n\n"
//                    + "To install the Wear app on the other devices, please click on the button "
//                    + "below.\n";
//
//    private static final String INSTALLED_ALL_DEVICES_MESSAGE =
//            WELCOME_MESSAGE
//                    + "Wear app installed on all your devices (%s)!\n\nYou can now use the "
//                    + "MessageApi, DataApi, etc.";
//
//
//
//    private static final String PLAY_STORE_APP_URI =
//            "market://details?id=com.format.gesturelauncher";
//
//
//
//
//
//
//    private void findAllWearDevices() {
//        Log.d(TAG, "findAllWearDevices()");
//
//        PendingResult<NodeApi.GetConnectedNodesResult> pendingResult =
//                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient);
//
//        pendingResult.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
//            @Override
//            public void onResult(@NonNull NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
//
//                if (getConnectedNodesResult.getStatus().isSuccess()) {
//                    mAllConnectedNodes = getConnectedNodesResult.getNodes();
//                    verifyNodeAndUpdateUI();
//
//                } else {
//                    Log.d(TAG, "Failed CapabilityApi: " + getConnectedNodesResult.getStatus());
//                }
//            }
//        });
//    }
//
//
//
//
//
//    private void findWearDevicesWithApp() {
//        final ProgressDialog dialog = ProgressDialog.show(WelcomeActivity.this, "",
//                getString(R.string.intro_checkAPI), true); //DIALOG
//
//
//        Log.d(TAG, "findWearDevicesWithApp()");
//
//        // You can filter this by FILTER_REACHABLE if you only want to open Nodes (Wear Devices)
//        // directly connect to your phone.
//        PendingResult<CapabilityApi.GetCapabilityResult> pendingResult =
//                Wearable.CapabilityApi.getCapability(
//                        mGoogleApiClient,
//                        CAPABILITY_WEAR_APP,
//                        CapabilityApi.FILTER_ALL);
//
//        pendingResult.setResultCallback(new ResultCallback<CapabilityApi.GetCapabilityResult>() {
//            @Override
//            public void onResult(@NonNull CapabilityApi.GetCapabilityResult getCapabilityResult) {
//                Log.d(TAG, "onResult(): " + getCapabilityResult);
//
//                if (getCapabilityResult.getStatus().isSuccess()) {
//                    CapabilityInfo capabilityInfo = getCapabilityResult.getCapability();
//                    mWearNodesWithApp = capabilityInfo.getNodes();
//                    verifyNodeAndUpdateUI();
////                    return true;
//                    dialog.dismiss();
//
//                } else {
//                    Log.d(TAG, "Failed CapabilityApi: " + getCapabilityResult.getStatus());
//                    dialog.dismiss();
//                }
//            }
//        });
//    }
//
//
//
//    private void verifyNodeAndUpdateUI() {
//        Log.d(TAG, "verifyNodeAndUpdateUI()");
//
//        if ((mWearNodesWithApp == null) || (mAllConnectedNodes == null)) {
//            Log.d(TAG, "Waiting on Results for both connected nodes and nodes with app");
//
//        } else if (mAllConnectedNodes.isEmpty()) {
//            Log.d(TAG, NO_DEVICES);
////            mInformationTextView.setText(NO_DEVICES);
////            mRemoteOpenButton.setVisibility(View.INVISIBLE);
//
//        } else if (mWearNodesWithApp.isEmpty()) {
//            Log.d(TAG, MISSING_ALL_MESSAGE);
////            mInformationTextView.setText(MISSING_ALL_MESSAGE);
////            mRemoteOpenButton.setVisibility(View.VISIBLE);
//
//            openPlayStoreOnWearDevicesWithoutApp();
//
//        } else if (mWearNodesWithApp.size() < mAllConnectedNodes.size()) {
//            // TODO: Add your code to communicate with the wear app(s) via
//            // Wear APIs (MessageApi, DataApi, etc.)
//
//            String installMessage =
//                    String.format(INSTALLED_SOME_DEVICES_MESSAGE, mWearNodesWithApp);
//            Log.d(TAG, installMessage);
////            mInformationTextView.setText(installMessage);
////            mRemoteOpenButton.setVisibility(View.VISIBLE);
//
//            connectAndSync();
//
//        } else {
//            // TODO: Add your code to communicate with the wear app(s) via
//            // Wear APIs (MessageApi, DataApi, etc.)
//
//            String installMessage =
//                    String.format(INSTALLED_ALL_DEVICES_MESSAGE, mWearNodesWithApp);
//            Log.d(TAG, installMessage);
////            mInformationTextView.setText(installMessage);
////            mRemoteOpenButton.setVisibility(View.INVISIBLE);
//
//            connectAndSync();
//
//        }
//    }
//
//    private void openPlayStoreOnWearDevicesWithoutApp() {
//        Log.d(TAG, "openPlayStoreOnWearDevicesWithoutApp()");
//
//        // Create a List of Nodes (Wear devices) without your app.
//        ArrayList<Node> nodesWithoutApp = new ArrayList<>();
//
//        for (Node node : mAllConnectedNodes) {
//            if (!mWearNodesWithApp.contains(node)) {
//                nodesWithoutApp.add(node);
//            }
//        }
//
//        if (!nodesWithoutApp.isEmpty()) {
//            Log.d(TAG, "Number of nodes without app: " + nodesWithoutApp.size());
//
//            Intent intent =
//                    new Intent(Intent.ACTION_VIEW)
//                            .addCategory(Intent.CATEGORY_BROWSABLE)
//                            .setData(Uri.parse(PLAY_STORE_APP_URI));
//
//            for (Node node : nodesWithoutApp) {
//                RemoteIntent.startRemoteActivity(
//                        getApplicationContext(),
//                        intent,
//                        mResultReceiver,
//                        node.getId());
//            }
//        }
//    }
//
//
//
//    private final ResultReceiver mResultReceiver = new ResultReceiver(new Handler()) {
//        @Override
//        protected void onReceiveResult(int resultCode, Bundle resultData) {
//            Log.d(TAG, "onReceiveResult: " + resultCode);
//
//            if (resultCode == RemoteIntent.RESULT_OK) {
//                Toast toast = Toast.makeText(
//                        getApplicationContext(),
//                        "Play Store Request to Wear device successful.",
//                        Toast.LENGTH_SHORT);
//                toast.show();
//
//            } else if (resultCode == RemoteIntent.RESULT_FAILED) {
//                Toast toast = Toast.makeText(
//                        getApplicationContext(),
//                        "Play Store Request Failed. Wear device(s) may not support Play Store, "
//                                + " that is, the Wear device may be version 1.0.",
//                        Toast.LENGTH_LONG);
//                toast.show();
//
//            } else {
//                throw new IllegalStateException("Unexpected result " + resultCode);
//            }
//        }
//    };



    public void MsgS(String message, int Duration){

        Snackbar.make(findViewById(R.id.buttonGetStart), message, Duration)
                .setAction("Action", null).show();
    }

}
