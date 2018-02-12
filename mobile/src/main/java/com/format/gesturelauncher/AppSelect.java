package com.format.gesturelauncher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.format.gesturelauncher.MobileConnectService.lib;
import static com.format.gesturelauncher.MobileConnectService.wearAppList;
import static com.format.gesturelauncher.MobileConnectService.wearPackList;

public class AppSelect extends AppCompatActivity {



    ArrayList<String> packagename= new ArrayList<String>(); //To store pacakgenames
    String MethodNameForReturn;
    ListView mainListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_app_select);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        // Find the ListView resource.
        mainListView = (ListView) findViewById(R.id.ListViewApps);





        Intent intent = getIntent();
        String message = intent.getStringExtra("type"); //get the method
        switch (message){
            case "wear":
                LoadWearApps(getApplicationContext());
                break;
            case "mobile":
                LoadMobileApps(getApplicationContext());
                break;
            case "timer":
                LoadTimers();
                break;
            case "call":
                GenerateMethod("call",intent.getStringExtra("number"),intent.getStringExtra("name"));
            case "tasker":
                GenerateMethod("tasker",intent.getStringExtra("task"),intent.getStringExtra("task"));
//                finish();
                break;

        }

    try {
        if (mainListView.getAdapter().getCount() <= 0) {
            Toast.makeText(getApplicationContext(), "You have created all items in this section!", Toast.LENGTH_SHORT).show();
            finish();

        }
    }catch (NullPointerException e){
//            finish();
    }

    }




    public void LoadMobileApps(Context context){

//        Toast.makeText(getApplicationContext(),"This section is under development, stay tuned!",Toast.LENGTH_SHORT).show();

        ArrayList<String> listItems=new ArrayList<String>();
        final ArrayAdapter<String>  listAdapter;


        // Create ArrayAdapter
        listAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, listItems);


        //---------------------------------------------------------------------get application list
        final PackageManager pm = getPackageManager(); //packge manager
        final List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA); // get list of installed program package

        Collections.sort(packages, new ApplicationInfo.DisplayNameComparator(pm));//sort alphabetically


        for (ApplicationInfo packageInfo : packages) {

            if(checkForLaunchIntent(packageInfo)==true && checkAlreadyExist(packageInfo)==false){ //if this package is runnable
                listAdapter.add(pm.getApplicationLabel(packageInfo).toString());
                packagename.add(packageInfo.packageName);
            }


        }
        //----------------------------------------------------------------------


        // Set the ArrayAdapter as the ListView's adapter.
        mainListView.setAdapter( listAdapter );


        //-------------------------------------------------------------------------------列表框点击事件
        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override

            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                String packName = packagename.get(position);

//                Toast.makeText(getApplicationContext(),"This section is under development, currently is not available to add. Stay tuned!",Toast.LENGTH_SHORT).show();
                //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                GenerateMethod("mapp",packName,listAdapter.getItem(position));
            }

        });

    }
    private boolean checkForLaunchIntent(ApplicationInfo info) {
        //filter system apps
//load launchable list    src：https://github.com/StackTipsLab/Advance-Android-Tutorials/blob/master/ListInstalledApps/src/com/javatechig/listapps/AllAppsActivity.java
            try {
                if (null != getPackageManager().getLaunchIntentForPackage(info.packageName)) { //if runnable
                    return true;
                }else {
                    Log.v(MainActivity.TAG,"filtered apps can't run: " + info.packageName);
                }
            } catch (Exception e) {

                e.printStackTrace();

            }

            return false;
    }//filter sys apps
    private boolean checkAlreadyExist(ApplicationInfo info) {

          for(String methodName : lib.getGestureEntries()){
              NameFilter filter =new NameFilter(methodName);
              if(filter.getMethod().equals("mapp") && filter.getPackName().equals(info.packageName)){
                  return true;
              }
          }


        return false;
    }//check app exist or not



    public void LoadWearApps(Context context){



        ArrayList<String> listItems=new ArrayList<String>();
        ArrayAdapter<String>  listAdapter;


        // Create ArrayAdapter
        listAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, listItems);

        //----------------------------------------------------------------------
        String[] wearPacks =wearPackList;
        String[] wearPacksAppName = wearAppList;


        for (int i=0;i < wearPacks.length;  i++) {
//            try {
//                String appLabel = (String)getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(wearPacks[i],PackageManager.GET_META_DATA));
//                listAdapter.add(appLabel); //add app label to the list
//                packagename.add(wearPacks[i]); //add app packName to the list
//            } catch (PackageManager.NameNotFoundException e) {
//                listAdapter.add(wearPacks[i]); //add app label to the list！！！
//                packagename.add(wearPacks[i]); //add app packName to the list！！！
//                Log.v(MainActivity.TAG,e+ ", " + wearPacks[i]+ " Not Found");
//                e.printStackTrace();
//            }

            listAdapter.add(wearPacksAppName[i]); //add app label to the list
            packagename.add(wearPacks[i]); //add app packName to the list
        }

        //----------------------------------------------------------------------

        // Set the ArrayAdapter as the ListView's adapter.
        mainListView.setAdapter( listAdapter );


        //-------------------------------------------------------------------------------列表框点击事件
        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override

            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

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
                listAdapter.add(methodsIndicator[i]); //add app label to the list
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






public void GenerateMethod(String runType,String runMethod, String Label ){
    // Obtain the shared Tracker instance.




    try {


        MethodNameForReturn = Label + "##"+runType+"##"+ runMethod;  //eg: Shazam##wearapp##com.shazam.shazam

        Intent addgesture = new Intent(this,GestureActivity.class);
        addgesture.putExtra("method",MethodNameForReturn);
        addgesture.putExtra("name",new NameFilter(MethodNameForReturn).getFilteredName());

        //Analytics
        Tracker mTracker;
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Mobile")
                .setAction("newGestureToDraw")
                .setLabel(MethodNameForReturn)
                .build());
        //-----------------------

        startActivity(addgesture);
        finish();


//            String method = "Open " +  MethodNameForReturn;

//            String[] spilt =method.split("##");


        //TODO dialog_confirm.setActivity(MethodNameForReturn);

//            Toast.makeText(getApplicationContext(),MethodNameForReturn,Toast.LENGTH_LONG).show();

//            finish();


    }catch (Exception e){
        Toast.makeText(getApplicationContext(),"Fail to run " + runMethod+ "\n Error message: " +e.toString(),Toast.LENGTH_LONG).show();
//            Log.v(MainActivity.TAG,e+ ", Fail to run " + packageName);
    }




}


}

