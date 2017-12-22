package com.format.gesturelauncher;

import android.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import static com.format.gesturelauncher.MobileConnectService.MOBILE_VERSION;
import static com.format.gesturelauncher.MobileConnectService.Sync;
import static com.format.gesturelauncher.MobileConnectService.WEAR_VERSION;
import static com.format.gesturelauncher.MobileConnectService.alreadyCreated;
import static com.format.gesturelauncher.MobileConnectService.lib;
import static com.format.gesturelauncher.MobileConnectService.wearPackList;

//TODO change notsync to a thing with progress bar,and change all the set visible to a method
public class MainActivity extends AppCompatActivity{


    static MobileConnectService mobileconnect;
    static MainActivity main;
    LinearLayout notsync ;

//    Snackbar notsyncIndicator;

    public final static String TAG = "fz"; //tag
    ArrayList<String> titles = new ArrayList<String>(); //used for gridview, title of gestures unfiltered
    ArrayList<String> shortentitles = new ArrayList<String>(); //used for gridview, title of gestures filtered
    ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>(); //used for gridview

    int defColor = Color.BLACK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //-----------------------------------------------
        notsync=(LinearLayout)findViewById(R.id.notSync);
//        startSync();
        main=this;

        //-----------------------------------------initiate connection & load all gestures
        initiateConnection();

        //-----------------------------------------createToolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
//        myToolbar.setTitleTextColor(Color.WHITE);

        //-------------------------------------------load grid
//        refreshGrid();


        //-------------------------------------------fab activity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Click action

                if(wearPackList==null){ //if not synced
                    Toast.makeText(getApplicationContext(), R.string.waitSync, Toast.LENGTH_LONG).show();
                    startSync();//try to sync
//                    startSync();
                }else { //else is good to go
                    openCreateGesture();
                }

            }
        });



    }


//--------------------------------

    public void initiateConnection(){
        if(MobileConnectService.alreadyCreated==false ) {
//            Toast.makeText(getApplicationContext(),"Initiating Connection",Toast.LENGTH_SHORT).show();
            startService(new Intent(getApplicationContext(), MobileConnectService.class));

        }
    }



    public static void finishedSync(Boolean invisble){
        try { //TODO BUG FIXED
            main.refreshGrid();
            if (invisble) {
                main.notsync.setVisibility(View.GONE);
            } else {
                main.notsync.setVisibility(View.VISIBLE);
            }
            ;
        }catch(NullPointerException e){
            e.printStackTrace();

        }
    }




//======================================================================================================================================================================================



    //=============================================================================Toolbar menu
     @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }  //Toolbar


    //@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                // User chose the "Settings" item, show the app settings UI...
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("About");
                builder.setMessage("Thank you for using!\n" +
                        "If you have any issues or suggestions, please email me at\n" +
                        "henryzhang9802@gmail.com\n\n"
                        +"Special thanks to Thomas\n\n"+"Mobile version code: "+MOBILE_VERSION+"\nVersion name: "+ BuildConfig.VERSION_NAME+"\nWear version code: "+WEAR_VERSION);

                builder.setPositiveButton("Email Me", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        sendEmail();

//                        dialog.cancel();
                    }
                });

                builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

                builder.show();



                return true;




            case R.id.action_help:


                Intent help = new Intent(getApplicationContext(),HelpActivity.class);
                help.putExtra("image","help");
                startActivity(help);

                return true;




            case R.id.action_sync:
                refreshGrid();
                startSync();
//                sendDataMapToDataLayer();//-------------------------------------Sync
//                MsgT("Synced!");
//                MsgS("Syncing...",Snackbar.LENGTH_LONG);
                startSync();

                return true;




//            case R.id.action_reload:  //重新载入默认的apps，对话框提示
//
//                final AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
//                builder2.setTitle("Reload commonly used apps");
//                builder2.setMessage(R.string.main_reload_confirm);
//
//
//                builder2.setPositiveButton("Reload", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        startActivity(new Intent(getApplicationContext(),WelcomeActivity.class));
//                    }
//                });
//                builder2.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        dialog.cancel();
//                    }
//                });
//
//                builder2.show();
//
//                return true;
//

            case R.id.action_rate:
//                rateApp();

                rateAppGoogle();
                return true;


//            case R.id.action_about:
//                final AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
//                builder.setTitle("About developer");
//                builder.setMessage("Thank you for using Wear Gesture Launcher!\nDeveloper Email: henryzhang9802@gmail.com");
//
//                builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        dialog.cancel();
//                    }
//                });
//
//                return true;


            case R.id.action_settings:
                if(wearPackList==null){ //if not synced
                    Toast.makeText(getApplicationContext(), R.string.waitSync, Toast.LENGTH_LONG).show();
                    startSync();//try to sync

                }else { //else is good to go
                    startActivity(new Intent(getApplicationContext(), Settings.class));
                }
                return true;



            case R.id.action_backup:

                    startActivity(new Intent(this,BackUp.class));

                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);


        }
    } //Toolbar actions

    public void sendEmail(){    
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setType("text/plain");
                        intent.setData(Uri.parse("mailto:henryzhang9802@gmail.com")); // only email apps should handle this
//        intent.putExtra(android.content.Intent.EXTRA_EMAIL,new String[] { "henryzhang9802@gmail.com" });
//                        intent.putExtra(Intent.EXTRA_CC,"henryzhang9802@gmail.com");
        intent.putExtra(Intent.EXTRA_SUBJECT, "A suggestion regarding the Wear Gesture Launcher");
//                        intent.putExtra(Intent.EXTRA_STREAM, attachment);
        if (intent.resolveActivity(getPackageManager()) != null) {
//            MsgT("Choose an Email app to continue");
            startActivity(intent);
        }
    }


    private void rateAppGoogle(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.rate_the_app);
        builder.setMessage(R.string.main_rate_message);
        builder.setCancelable(false);

        builder.setPositiveButton(R.string.action_sure, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
//                startActivity(new Intent(getApplicationContext(),WelcomeActivity.class));
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.format.gesturelauncher")));
                dialog.cancel();
            }
        });


        builder.setNegativeButton(R.string.action_send_feedback,
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        rateAppEmail();
                        dialog.cancel();
                    }
                });

        builder.setNeutralButton(R.string.action_no_thanks, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void rateAppEmail(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Send feedback");
        builder.setMessage("If you have any issues or suggestions, you can click the button to send an email for feedback. We will reply and adapt the change as soon as possible!");
        builder.setCancelable(false);

        builder.setPositiveButton("Send Email", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
//                startActivity(new Intent(getApplicationContext(),WelcomeActivity.class));
                sendEmail();
                dialog.cancel();
            }
        });
        builder.setNegativeButton("No, thanks", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        builder.show();
    }
//---------------------------------------------------


    public void startSync(){
        Sync(mobileconnect,false);
        startSyncIndicator();

    }

    public void startSyncIndicator(){


        notsync.setVisibility(View.VISIBLE);
//        notsyncIndicator = Snackbar.make(findViewById(R.id.fab), "Trying to sync...",Snackbar.LENGTH_SHORT);
//        notsyncIndicator.show();
//        notsyncIndicator.dismiss();
        findViewById(R.id.progressBar2).setVisibility(View.VISIBLE);
        final TextView text=(TextView)findViewById(R.id.text_sync);

        text.setText(R.string.main_syncing);


        new CountDownTimer(8000,1000){
            public void onTick(long l) {
                if(notsync.getVisibility()!=View.VISIBLE){
                    cancel();
                }
//
            }
            public void onFinish() {
//                notsyncIndicator  = Snackbar.make(findViewById(R.id.fab), "Gesture library not synced",Snackbar.LENGTH_INDEFINITE) //A indefinite snackbar indicate a unssccessful sync, with button direct to help
//                        .setAction("Action", null).setAction("Help", new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Intent help = new Intent(getApplicationContext(),HelpActivity.class);
//                        help.putExtra("image","connect");
//                        startActivity(help);
//                    }
//                });
//                notsyncIndicator.show();
                text.setText(R.string.main_not_sync);
                findViewById(R.id.progressBar2).setVisibility(View.GONE);
            }
        }.start();
    }

    public void openCreateGesture() {
        Intent intent = new Intent(this, ActionSelect.class);
        startActivity(intent);
    } 

    //--------------------------------------------------------------------------Grid View
    public void refreshGrid() {

    
        if (!lib.load()) {          //必须要这个
            Intent intent = new Intent(this, WelcomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
//            finish();
        }
        titles.clear();
        bitmaps.clear();
        shortentitles.clear();
        //----------------------------------------------------------------------------Gestures


        Set<String> gestureNameSet = lib.getGestureEntries(); //get all the unfiltered names of the gesture

        if(gestureNameSet.size()<=0){
            MsgS("Gesture library is empty, add one now!",Snackbar.LENGTH_INDEFINITE);
        }

        for (String gestureName : gestureNameSet) { //for each name

            ArrayList<Gesture> gesturesList = lib.getGestures(gestureName);//get the gesture from this name ( could have mutiple ones)

            Log.d(TAG, gestureName);

            NameFilter filter = new NameFilter(gestureName);//To delete things after ##


            for (Gesture gesture : gesturesList) {
                titles.add(gestureName);
                bitmaps.add(gesture.toBitmap(125, 125, 30, defColor));//generate bitmap and add to the list
                shortentitles.add(filter.GetfiltedName());//To delete things after ##

//                        setImageBitmap(gesture.toBitmap(100,100,10,defColor));
            }
        }

        //--------------------------------------------------------------------------Grid View

        final GridView gridview = (GridView) findViewById(R.id.gridview);
        final ImageAdapter adapter = new ImageAdapter(getApplicationContext(), shortentitles, bitmaps); //建立继承，adaper，放入bitmap和标题
        gridview.setAdapter(adapter);//adaper

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) { //when click

                delete(position);
            }
        });

//        gridview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
//                delete(i);
//                return true;
//            }
//        });

//        Sync();//try to sync

    } //refresh gridview,use adapter to show image & text


    //--------------------------------------------------------------------------

    public void delete( final int position) {
//        MsgS("Updating gestures...",Snackbar.LENGTH_SHORT);
        startSync();
//        notsync.setVisibility(View.VISIBLE);


        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_title_delete);
        builder.setMessage(getString(R.string.dialog_delete_msg) + shortentitles.get(position)+"' ?");


        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                new CountDownTimer(4000,100){

                    public void onTick(long l) {
                        if(notsync.getVisibility()!=View.VISIBLE){
                            deleteItem(position);
                            cancel();
                        }
//                        Log.v(TAG,notsync.getVisibility());
                    }

                    public void onFinish() {
                        MsgS(getString(R.string.main_faildelete),Snackbar.LENGTH_SHORT);

                    }
                }.start();


                dialog.cancel();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        builder.show();

    } //Delete warning


    public void deleteItem(int position) {

//        int i=0;
//        do{
//            i++;
//            if(i>9200000){
//                if(notsync.getVisibility()==View.VISIBLE){
//                    MsgT("Fail to delete, waiting for wearable respond....");
//                    return;
//                }
//            }
//        }while(notsync.getVisibility()==View.VISIBLE);

        lib.removeEntry(titles.get(position));
        lib.save();
        MsgS("Item deleted",Snackbar.LENGTH_SHORT);
        refreshGrid();
        Sync(mobileconnect,true);

    }  //------------------------------------delete item


    @Override
    protected void onResume() {
        super.onResume();
        if(alreadyCreated) {
            if (!lib.load()) {
                Intent intent = new Intent(this, WelcomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
//            finish();
            }else {
                refreshGrid();
            }
        }

        if(notsync.getVisibility()==View.VISIBLE){
            startSyncIndicator();
        }
    }



    public static void welcomedialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(main);
        builder.setTitle(R.string.main_howto);
        builder.setMessage(R.string.main_howto_msg)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //do things
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }


    public static void warningdialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(main);
        builder.setTitle("Warning");
        builder.setMessage("Your wearable app is running an old version, this might because you just updated the mobile app. Normally you only need to wait for the auto-update on your watch. Please make sure you are using the same version to ensure sync running properly.")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //do things
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }//Version Warning


    public static void versionNote(){

        final SharedPreferences Pref =main.getSharedPreferences("update",MODE_PRIVATE);

        if(Pref.getInt("ignore",0)==MOBILE_VERSION){

        }else {

            AlertDialog.Builder builder = new AlertDialog.Builder(main);
            builder.setTitle("Update warning");
            builder.setMessage("Mobile app " + (MOBILE_VERSION -1000)+ "\nWear app " + (WEAR_VERSION -2000)+ "\nVersion not consistent, please wait for play store auto-update or update manually to make sure sync running properly.\n\nIf you are having trouble updating wearable app, please use the backup feature before and after reinstalling the new version to save your gestures. Please contact me if you have any problem.");
            builder.setCancelable(true);

            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            builder.setNegativeButton("Ignore", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Pref.edit().putInt("ignore", MOBILE_VERSION).apply();
                    Toast.makeText(main, "Ignored for this version", Toast.LENGTH_SHORT).show();
                    dialog.cancel();
                }
            });

            builder.show();
        }
    }



    //------------------------------------------------
    public void MsgT(String message) {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }

        public void MsgS(String message,int Duration){

            Snackbar.make(findViewById(R.id.fab), message, Duration)
                    .setAction("Action", null).show();
        }






    }


