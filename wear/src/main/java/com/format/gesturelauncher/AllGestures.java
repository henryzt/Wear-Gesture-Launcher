package com.format.gesturelauncher;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.gesture.Gesture;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.ConfirmationOverlay;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

import static com.format.gesturelauncher.MainActivity.wearConnect;
import static com.format.gesturelauncher.WearConnectService.lib;
import static com.format.gesturelauncher.WearConnectService.sendMobile;

public class AllGestures extends WearableActivity {



    private BoxInsetLayout mContainerView;
    ArrayList<String> titles = new ArrayList<String>(); //for listview,original title
    ArrayList<String> shortenTitles = new ArrayList<String>(); //for listview,shorten title
    ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>(); //for listview

    Boolean openMain=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_gestures);
        setAmbientEnabled();

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mContainerView.setBackground(null);

        try {
            if (getIntent().getStringExtra("open").equals("y")) {
                openMain = true;
            }
        }catch (Exception e){

        }

        refreshList();

    }



    public void refreshList() {


        titles.clear();
        bitmaps.clear();
        shortenTitles.clear();
        //----------------------------------------------------------------------------gesture


        Set<String> gestureNameSet = lib.getGestureEntries(); //get all gesture's orig name(lib is in wearConnectService）

        if(gestureNameSet.size()<=0){

            new ConfirmationOverlay().setDuration(99000)
                    .setMessage("Gesture library is empty, add a gesture now!")
                    .setType(ConfirmationOverlay.FAILURE_ANIMATION)
                    .showOn(AllGestures.this);
        }


        for (String gestureName : gestureNameSet) { //for each name

            ArrayList<Gesture> gesturesList = lib.getGestures(gestureName);//for each gesture in each name

//            Log.d(tag, gestureName);

            NameFilter filter = new NameFilter(gestureName);//NameFilter


            for (Gesture gesture : gesturesList) {
                titles.add(gestureName);
                bitmaps.add(gesture.toBitmap(125, 125, 30, Color.YELLOW));//generate bitmap and add to listView
                shortenTitles.add(filter.getFilteredName());

//                        setImageBitmap(gesture.toBitmap(100,100,10,defColor));
            }
        }

        //--------------------------------------------------------------------------Grid View

        final ListView listview = (ListView) findViewById(R.id.listview);
        final ImageAdapter adapter = new ImageAdapter(getApplicationContext(), shortenTitles, bitmaps); //adaper，put bitmap&title in
        listview.setAdapter(adapter);//use adaper

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {//when click
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                delete(position);
            }
        });

        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                delete(i);
                return true;
            }
        });



    } //this code is copy from mobile

    public void delete( final int position) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Delete gesture");
        builder.setMessage(getString(R.string.dialog_delete_msg) + shortenTitles.get(position)+"' ?");


        builder.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteItem(position);
                dialog.cancel();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        builder.show();

    } //提示Delete


    public void deleteItem(int position) {

        lib.removeEntry(titles.get(position));
        lib.save();
//        msg("Item deleted");
        refreshList();
        sendMobile(wearConnect);//TODO A static method cannot call a non-static method, but we can use a reference, which include a non-static method to the static method. https://stackoverflow.com/questions/31661110/calling-a-non-static-method-in-an-android-onpreferenceclicklistener
        new ConfirmationOverlay()
                .setMessage(getString(R.string.main_deleted))
                .setType(ConfirmationOverlay.SUCCESS_ANIMATION)
                .showOn(AllGestures.this);


//        Sync();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!this.isFinishing()){ //Home pressed
            finish();
        }

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(openMain) {
//            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            Intent intent=new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("extra","notini");
//            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }
    }

    public void msg(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }


//    //_____________________________________________________________________Ambient
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
            mContainerView.setBackground(null);

        }
    }
}
