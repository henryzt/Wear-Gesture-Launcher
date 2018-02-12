package com.format.gesturelauncher;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


public class WearConnectService extends Service implements
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{


    Node mNode; // the connected device to send the message to

    private GoogleApiClient mGoogleApiClient;
    String path;
    String mobile_received = "/receive";


//    GestureLibrary libfromFile; //glib
    GestureLibrary libInitial; //ini glib


    public static GestureLibrary lib; //glib

    static boolean alreadyCreated ;


    static final String TAG ="fzg";
    byte[] fileInBytes;
    static String[] packNameList;//package name
    static String[] appNameList;//package name corresponding app label

    static int WEAR_VERSION;
    static int MOBILE_VERSION;


    public static String locationAction = "/action";



    static boolean showQuickLauncher;
    static boolean vibratorOn;
    static String location ;
    static int accuracy ;


    public Tracker mTracker;


    public WearConnectService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public void onCreate() {
        super.onCreate();


        mGoogleApiClient = new GoogleApiClient.Builder(this)  //Google api
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        alreadyCreated = true;



        MainActivity.wearConnect =this;//A static method cannot call a non-static method, but we can use a reference, which include a non-static method to the static method.


        WEAR_VERSION=BuildConfig.VERSION_CODE;
        MOBILE_VERSION=0;

        loadLibrary();


        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();;

    }


    //=====================================================================================================================================================================================

    //---------------------------------------------------load lib
    public void loadLibrary(){
        final File mStoreFile = new File(getFilesDir(), "gestureNew");
        lib= GestureLibraries.fromFile(mStoreFile);

        getApplicationPacklist();//取包名'


        if (!lib.load()) {          //必须要这个

//            Toast.makeText(getApplicationContext(), R.string.empty_lib,Toast.LENGTH_LONG).show();
            firstInitiate();
        }

        loadPref();

    }

    //---------------------------------------------------load preference
    public void loadPref() {
        SharedPreferences sharedPref = getSharedPreferences("main", MODE_PRIVATE);
        showQuickLauncher = sharedPref.getBoolean("show", true);
        vibratorOn = sharedPref.getBoolean("vibrate", true);
        location = sharedPref.getString("location", "r");
        accuracy = sharedPref.getInt("accuracy", 2);
    }


    //----------------------------------------------------------------------------------------------------------First time run
    public static void  reload(WearConnectService connect){

        connect.firstInitiate();

        connect.sendDataMapToDataLayerForMobile("/receive");//send confirm to mobile

    }


    public void firstInitiate(){
        //-------------------------------------------gesture

        final File mStoreFile = new File(getFilesDir(), "gestureNew");

        if(!mStoreFile.exists()) { //如果文件不存在就创建一个
            try {
                FileOutputStream stream = new FileOutputStream(mStoreFile);
                stream.write("".getBytes());
                stream.close();
//                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(mStoreFile);
//                outputStreamWriter.write(data);
//                outputStreamWriter.close();
            } catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }
        }


        lib = GestureLibraries.fromFile(mStoreFile);//load gestures from file


        libInitial= GestureLibraries.fromRawResource(this,R.raw.gestureini );//load preloaded gestures from raw

        if(!libInitial.load()){ //load preloaded gestures fail
            Toast.makeText(this,"Fatal error (Initial gesture not found). Please contact the developer.",Toast.LENGTH_LONG).show();

        }


        //-------------------------------------------------------------------------------------------------------
        Set<String> gestureNameSet = libInitial.getGestureEntries();
        for (String gestureName : gestureNameSet) {

            ArrayList<Gesture> gesturesList = libInitial.getGestures(gestureName);

            Log.d(TAG, gestureName);

            NameFilter filter = new NameFilter(gestureName);

            for (Gesture gesture : gesturesList) {

                if(packExists(filter.getPackName())){



                    //TODO-------------------------------------to reload app name (based on the language), only apps are allowed
                    String finalName;
                    PackageManager packageManager= getApplicationContext().getPackageManager();
                    try {
                        String appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(filter.getPackName(), PackageManager.GET_META_DATA));
                        finalName = filter.changeFilteredName(appName);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                        finalName = gestureName;
                    }
                    //--------------------------


                    lib.addGesture(finalName,gesture); //add to lib
//                    packNameWhichExists = packNameWhichExists + filter.getFilteredName() +", ";
                }

            }
        }


        if(lib.save()){
//            msg("Gesture Library initiated!");

        }else{
            msg("Error: fail to save gesture library");
//            finish();
        }

        if(!lib.load()){   //to make sure loaded again
            msg("No gestures found, failsafe gesture is added.");
            lib.addGesture("Test",libInitial.getGestures("WearTest##wearapp##com.format.weartest").get(0));
            lib.save();
        }


    }

    public boolean packExists(String packageName){
        for(String packNameWear : packNameList){  //遍历手表程序表
            if(packNameWear.equals(packageName)){  //如果这个packName在里面有的话
                for(String packNameFile : lib.getGestureEntries()){ //遍历已存在于文件里的手势
                    if(packNameFile.equals(packageName)){ //如果手势已经存在于文件里
                        return false;  //表示不需要再次添加
                    }
                }
                return true;//没有，那么就添加
            }
        }
        return false;//不存在于手表里，不需要添加
    }


    //============================================================================================== RECEIVE from mobile

    public void byteToFile(){
        String strFilePath = getFilesDir()+"/gestureNew";
        try {
            FileOutputStream fos = new FileOutputStream(strFilePath);
            //String strContent = "Write File using Java ";

            fos.write(fileInBytes);
            fos.close();
        }
        catch(FileNotFoundException ex)   {
            System.out.println("FileNotFoundException : " + ex);
        }
        catch(IOException ioe)  {
            System.out.println("IOException : " + ioe);
        }

    }  //将收到的byte转换为文件



    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        resolveNode();
//        msg("Phone connected");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    /*
       * Resolve the node = the connected device to send the message to
       */
    private void resolveNode() {

        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult nodes) {
                for (Node node : nodes.getNodes()) {
                    Log.v(TAG,node.toString());
                    mNode = node;
                }
            }
        });
    }



    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {

            PutDataMapRequest putDataMapRequest =
                    PutDataMapRequest.createFromDataMapItem(DataMapItem.fromDataItem(event.getDataItem()));//!!!!!!!!!!!!!!!!https://medium.com/@manuelvicnt/android-wear-accessing-the-data-layer-api-d64fd55982e3

            if (event.getType() == DataEvent.TYPE_CHANGED) {


                DataItem item = event.getDataItem();
                path =item.getUri().getPath();

                if(path.equals("/gestures")) { //1.make sure it is from mobile, to overwrote lib
                    // DataItem changed


                    DataMap map = putDataMapRequest.getDataMap();


                    //----------------------------------------------------------------------------------------------------------------Version check

                    MOBILE_VERSION=map.getInt("version");

                    if(MOBILE_VERSION!=WEAR_VERSION-1000){ //如果版本不同则提醒更新
                        if(map.getInt("version")<1012){ //如果版本过小

                            Toast t = Toast.makeText(this,  "\n\n\nWarning: Gestures overwrote from phone, Mobile app version too old, please update the app on your phone.", Toast.LENGTH_LONG);
                            t.setGravity(Gravity.FILL_HORIZONTAL | Gravity.FILL_VERTICAL, 0, 0);
                            t.show();

                        }else {
                            Toast t = Toast.makeText(this, "\n\nWear gesture launcher\nMobile app " + MOBILE_VERSION + "\nWear app " + WEAR_VERSION + "\nVersion not consistent, please update your apps to make sure sync running properly.", Toast.LENGTH_LONG);
                            t.setGravity(Gravity.FILL_HORIZONTAL | Gravity.FILL_VERTICAL, 0, 0);
                            t.show();

                        }
                    }else {

                    }
                    //----------------------------------------------------------------------------------------------------------------


                    if(map.getBoolean("overwrite")||map.getInt("version")<1012){  //如果手机说要覆盖则覆盖，如果说false的话证明手机只是在检查连接和版本号,或者如果版本号过老也覆盖
                        fileInBytes = map.getByteArray("File"); //!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                        byteToFile();//保存
                        loadLibrary();
                        msg("Gesture Library Synced!");
                    }


                    //-------------------------------------------------------------------------------------------------------------Receive pref from phone

                    boolean safe =false;
                    try{

                            if(map.getString("location").length()>0){
                                safe=true;
                            }
                    }catch (NullPointerException e){
                        e.printStackTrace();
                        msg("No pref received");
                    }

                    if(safe) {
                        SharedPreferences sharedPref = getSharedPreferences("main", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putBoolean("show", map.getBoolean("show"));
                        editor.putBoolean("vibrate", map.getBoolean("vibrate"));
                        editor.putString("location", map.getString("location"));
                        editor.putInt("accuracy", map.getInt("accuracy"));
                        editor.apply();

                        //----------------------------------------------------refresh floater
                        if(FloaterService.frameLayoutfloater!=null) {

                            FloaterService.frameLayoutfloater.removeAllViews();
                            FloaterService.frameLayoutfloater=null;
                            stopService(new Intent(WearConnectService.this, FloaterService.class));
//                            msg("service stopped");

                            if(sharedPref.getBoolean("show",true)){
                                startService(new Intent(WearConnectService.this, FloaterService.class));
                            }
                        }else{
                            if(sharedPref.getBoolean("show",true)){
                                startService(new Intent(WearConnectService.this, FloaterService.class));
                            }
                        }

                    }


                    loadPref();
                    //--------------------------------------------------------------



                    sendDataMapToDataLayerForMobile("/receive");//send confirm to mobile

                    //(for ref)
//                    byte[] data = item.getData();
//                    msg(data.toString());
//                    msg(path);
//                    String s = new String(data);
//                    TextView text = findViewById(R.id.text);
////                    text.setText("path: " + path + s);
//                    text.setText("Gesture Library updated!");

                    //---------------------------------------

                }else if(path.equals("/initiate")){
                    sendDataMapToDataLayerForMobile("/receive");//send confirm to mobile!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    msg("Connection Initiated!");

                }else if(path.equals("/needupdate")) {
                    sendDataMapToDataLayerForMobile("/update");
//                    msg("Library updated to mobile");
                }



            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    //==============================================================================================


    public static void sendMobileAction(WearConnectService connect,String action){
//        connect.sendDataMapToDataLayerForMobile(locationAction);
        connect.resolveNode();
        connect.sendMobileMessage(action);

    }

    private void sendMobileMessage(final String action) {

        if (mNode != null && mGoogleApiClient!=null && mGoogleApiClient.isConnected()) {
            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, mNode.getId(), "/sync", null).setResultCallback(

                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {

                            if (!sendMessageResult.getStatus().isSuccess()) {
                                Log.e("TAG", "Failed to send message with status code: " + sendMessageResult.getStatus().getStatusCode());
                                msg("Failed to send action: " + sendMessageResult.getStatus().getStatusMessage());
                                sendMobileActionDatamap(action);


                                //Analytics
                                mTracker.send(new HitBuilders.EventBuilder().setCategory("MobileConnection").setAction("failToSendAction").setLabel( sendMessageResult.getStatus().getStatusMessage()).build());

                            }else {
                                sendMobileActionDatamap(action);
                                msg("Action Sent!");

                                //Analytics
                                mTracker.send(new HitBuilders.EventBuilder().setCategory("MobileConnection").setAction("actionSent").setLabel( action).build());
                            }
                        }
                    }
            );
        }else{
            msg("Connection failed");

            sendMobileActionDatamap(action);


            //Analytics
            mTracker.send(new HitBuilders.EventBuilder().setCategory("MobileConnection").setAction("failToSendAction").setLabel( "Connection failed").build());
        }

    } //To Listener on mobile phone


    public static void  sendMobile(WearConnectService connect){

        connect.sendDataMapToDataLayerForMobile("/update");//TODO
    } //用于外部使用，外部call以进行同步


    //------------------------------------------------------------------------------------------------------------------SEND 发送回执给手机

    private void getApplicationPacklist(){ //取手表程序包名列表String[]
        ArrayList<String> packagename= new ArrayList<String>(); //用于存放包名
        ArrayList<String> appname= new ArrayList<String>(); //用于存放包对应程序名
        //---------------------------------------------------------------------取程序列表
        final PackageManager pm = getPackageManager(); //packge manager
        final List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA); // get list of installed program package
        Collections.sort(packages, new ApplicationInfo.DisplayNameComparator(pm));//sort alphabetically

        for (ApplicationInfo packageInfo : packages) {

            if(checkForLaunchIntent(packageInfo)==true && checkForAlreadyExist(packageInfo)==false) { //如果这个包是可以运行的且不存在相应手势
                packagename.add(packageInfo.packageName);//添加包名
                appname.add(pm.getApplicationLabel(packageInfo).toString());//添加程序名
            }

        }
        //----------------------------------------------------------------------
        packNameList = packagename.toArray(new String[packagename.size()]);
        appNameList = appname.toArray(new String[appname.size()]);
//        return packNameList;
    }//取手表程序列表


    private boolean checkForLaunchIntent(ApplicationInfo info) {
        //load launchable list 原：https://github.com/StackTipsLab/Advance-Android-Tutorials/blob/master/ListInstalledApps/src/com/javatechig/listapps/AllAppsActivity.java
        try {
            if (null != getPackageManager().getLaunchIntentForPackage(info.packageName)) { //如果可以打开这个程序
                return true;
            }
        } catch (Exception e) {

            e.printStackTrace();

        }

        return false;
    }//检查这个程序确保可以运行 而不是系统程序（过滤掉不可以运行的，添加列表）


    public static boolean checkForAlreadyExist(ApplicationInfo info){
        if(!lib.load()){
            return false;
        }


        for(String gestureName :lib.getGestureEntries()){
            if(new NameFilter(gestureName).getPackName().equals(info.packageName)){   //名称过滤：取包名，对比
                return true; //存在
            }
        }
        return false;//不存在
    }//检查包名是否已经存在相应的手势





    public byte[] file2byte(){
        File file = new File(getFilesDir(), "gestureNew"); //手表没有s，手机有s

        byte[] b = new byte[(int) file.length()];
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(b);
            for (int i = 0; i < b.length; i++) {
                System.out.print((char)b[i]);
            }
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found.");
            e.printStackTrace();
        }
        catch (IOException e1) {
            System.out.println("Error Reading The File.");
            e1.printStackTrace();
        }
        return b;
    }   //将要update的文件转化成byte以发送到手机



    public void sendDataMapToDataLayerForMobile(String location){
        mobile_received = location;
        if(mGoogleApiClient.isConnected()){
            DataMap dataMap = createSyncDatamap();
            new SendDataMapToDataLayer(mobile_received,dataMap).start();
        }
    } //发送

    private DataMap createSyncDatamap(){
        DataMap dataMap = new DataMap();
        dataMap.putString("Received!","From wear");
        dataMap.putString("Time", Long.toString(System.currentTimeMillis()) );

        getApplicationPacklist();
        dataMap.putStringArray("packList", packNameList); //手表程序包名列表发回手机
        dataMap.putStringArray("appList", appNameList); //手表程序名列表发回手机
        dataMap.putByteArray("updatedlib",file2byte()); //发送updated手势
        dataMap.putInt("version",BuildConfig.VERSION_CODE);//发送版本号，手表比手机大1000


        //---------------------------------------------------------------Sync SharedPreference
        SharedPreferences sharedPref = getSharedPreferences("main",MODE_PRIVATE);
        boolean showq = sharedPref.getBoolean("show",true);
        boolean vib = sharedPref.getBoolean("vibrate",true);
        String loca=sharedPref.getString("location","r");
        int accuracy=sharedPref.getInt("accuracy",2);

        dataMap.putBoolean("show",showq);
        dataMap.putBoolean("vibrate",vib);
        dataMap.putString("location",loca);
        dataMap.putInt("accuracy",accuracy);
        //--------------------------------------------------


        return dataMap;
    } //建立数据包准备发送

    private void sendMobileActionDatamap(String action){
        DataMap dataMap = new DataMap();
        dataMap.putString("action",action);
        dataMap.putString("Time", Long.toString(System.currentTimeMillis()) );


        mobile_received = locationAction;
        if(mGoogleApiClient.isConnected()){
            new SendDataMapToDataLayer(mobile_received,dataMap).start();
        }
    }//datamap for mobile action




    private class SendDataMapToDataLayer extends Thread{
        String path;
        DataMap dataMap;

        public SendDataMapToDataLayer(String path, DataMap dataMap){
            this.path=path;
            this.dataMap=dataMap;
        }

        public void run(){
//            NodeApi.GetConnectedNodesResult nodeList = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();

            PutDataMapRequest putDataMapReq = PutDataMapRequest.create(mobile_received);
            putDataMapReq.getDataMap().putAll(dataMap);
            PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
            PendingResult<DataApi.DataItemResult> pendingResult =
                    Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);

//            pendingResult.setResultCallback、;

//            for(Node node : nodeList.getNodes()){
//            for(DataApi.DataItemResult result : pendingResult){
////                MessageApi.SendMessageResult messageResult = Wearable.MessageApi.sendMessage(mGoogleApiClient,node.getId(),path,me)
//                if()
            Log.v(TAG,"Sent!");
//
//            }
        }


    }//发送类

//----------------------------------------------------------------------------------------------------------------



    public void msg(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
        Log.v(TAG,message);
    }
}
