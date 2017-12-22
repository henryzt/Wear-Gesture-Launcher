package com.format.gesturelauncher;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.gesture.Gesture;
import android.net.Uri;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.provider.ContactsContract;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WatchViewStub;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;

import static com.format.gesturelauncher.WearConnectService.appNameList;
import static com.format.gesturelauncher.WearConnectService.lib;
import static com.format.gesturelauncher.WearConnectService.packNameList;

//TODO You can omly get the app list after sync
//TODO Mobile delete/ add not instantly sync
//gestures


public class AppSelector extends Activity {

    ArrayList<String> packagename= new ArrayList<String>(); //用于存放应用包名
    String MethodNameForReturn;
    ListView mainListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_app_selector);


            mainListView = (ListView) findViewById(R.id.listviewApp);

//            try{

                switch (getIntent().getStringExtra("method")){
                    case "wearapp":
                        LoadWearApps();
                        break;
                    case "timer":
                        LoadTimers();
                        break;
                    case "call":
//                        selectContact();
//                         finish();
                        break;

                }

//            }catch (Exception e){
//
//            }


        try {
            if (mainListView.getAdapter().getCount() <= 0) {
                Toast.makeText(getApplicationContext(), "You have created all items in this section!", Toast.LENGTH_SHORT).show();
                finish();

            }
        }catch (NullPointerException e){
//            finish();
        }


        }




    public void LoadWearApps(){


        //------------------------------------------Message
//        Toast t = Toast.makeText(this,"\n\n\n\nPlease select an App you would like to create a gesture with",Toast.LENGTH_SHORT);
//        t.setGravity(Gravity.FILL_HORIZONTAL|Gravity.FILL_VERTICAL, 0, 0);
//        t.show();

        //------------------------------------------List Adapter

        ArrayList<String> listItems=new ArrayList<String>();
        ArrayAdapter<String> listAdapter;


        // Create ArrayAdapter
        listAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, listItems);

        //----------------------------------------------------------------------
        String[] wearPacks = packNameList;
        String[] wearPacksAppName = appNameList;

        for (int i=0;i < wearPacks.length;  i++) {
//            try {
//                String appLabel = (String)getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(wearPacks[i],PackageManager.GET_META_DATA));
//                listAdapter.add(appLabel); //列表加入程序名
//                packagename.add(wearPacks[i]); //包名加入当前包名
//            } catch (PackageManager.NameNotFoundException e) {
//                listAdapter.add(wearPacks[i]); //列表加入程序名！！！
//                packagename.add(wearPacks[i]); //包名加入当前包名！！！
//                Log.v(MainActivity.TAG,e+ ", " + wearPacks[i]+ " Not Found");
//                e.printStackTrace();
//            }

            listAdapter.add(wearPacksAppName[i]); //列表加入程序名
            packagename.add(wearPacks[i]); //包名加入当前包名
        }
//
//        //----------------------------------------------------------------------
//
//        // Set the ArrayAdapter as the ListView's adapter.
        mainListView.setAdapter( listAdapter );

        //------------------------------------------Listener

        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override

            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
//               String packName = mainListView.getItemAtPosition(position).toString();//Get the item user clicked
                String packName = packagename.get(position);

                GenerateMethod("wearapp",packName,mainListView.getItemAtPosition(position).toString());
            }

        });


    }



    public void LoadTimers(){



        //------------------------------------------List Adapter

        ArrayList<String> listItems=new ArrayList<String>();
        ArrayAdapter<String> listAdapter;


        // Create ArrayAdapter
        listAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, listItems);

        //----------------------------------------------------------------------
        final String[] methods = {"Alarm","Alarm List","Timer","Stopwatch"};
        final String[] methodsIndicator = {"New Alarm","Manage Alarms","Open Timer","Open Stopwatch"};


        final ArrayList<String> nonExistMethods=new ArrayList<>();



        for (int i=0;i < methods.length;  i++) {

            if (!TimerCheckExist(methods[i])) { //If not exist then add
                listAdapter.add(methodsIndicator[i]); //列表加入程序名
                nonExistMethods.add(methods[i]);

            }
        }



        mainListView.setAdapter( listAdapter );

        //------------------------------------------Listener

        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override

            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                String packName = nonExistMethods.get(position);

                GenerateMethod("timer", packName, mainListView.getItemAtPosition(position).toString());


            }

        });


    }



    public boolean TimerCheckExist(String method){
        for(String name : lib.getGestureEntries()){
            NameFilter filter= new NameFilter(name);
            if(filter.getMethod().equals("timer")&&filter.getPackName().equals(method)){
                return true;
            }
        }
        return false;

    }




//    static final int REQUEST_SELECT_PHONE_NUMBER = 1;
//
//    public void selectContact() {
//        // Start an activity for the user to pick a phone number from contacts
//        Intent intent = new Intent(Intent.ACTION_PICK);
//        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
//        if (intent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(intent, REQUEST_SELECT_PHONE_NUMBER);
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == REQUEST_SELECT_PHONE_NUMBER && resultCode == RESULT_OK) {
//            // Get the URI and query the content provider for the phone number
//            Uri contactUri = data.getData();
//            String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
//            Cursor cursor = getContentResolver().query(contactUri, projection,
//                    null, null, null);
//            // If the cursor returned is valid, get the phone number
//            if (cursor != null && cursor.moveToFirst()) {
//                int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
//                String number = cursor.getString(numberIndex);
////                String peoplename = cursor.getString
//                // Do something with the phone number
////            ...
//                GenerateMethod("call",number,"Call "+number );
//            }
//        }
//    }




    public void GenerateMethod(String runType,String runMethod, String Label ){
        try {


            MethodNameForReturn = Label + "##"+runType+"##"+ runMethod;  //eg: Shazam##wearapp##com.shazam.shazam

            Intent addgesture = new Intent(this,AddGesture.class);
            addgesture.putExtra("method",MethodNameForReturn);
            addgesture.putExtra("name",new NameFilter(MethodNameForReturn).GetfiltedName());
            startActivity(addgesture);


//            String method = "Open " +  MethodNameForReturn;

//            String[] spilt =method.split("##");


            //TODO dialog_confirm.setActivity(MethodNameForReturn);

//            Toast.makeText(getApplicationContext(),MethodNameForReturn,Toast.LENGTH_LONG).show();

//            finish();


        }catch (Exception e){
            Toast.makeText(getApplicationContext(),"Fail to run " + runMethod+ "\n Error message: " +e.toString(),Toast.LENGTH_LONG).show();
//            Log.v(MainActivity.TAG,e+ ", Fail to run " + packageName);
        }




    }//启动指定的应用程序

//







}

