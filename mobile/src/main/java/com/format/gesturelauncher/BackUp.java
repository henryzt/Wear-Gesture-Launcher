package com.format.gesturelauncher;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.gesture.GestureLibraries;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.format.gesturelauncher.MainActivity.finishedSync;
import static com.format.gesturelauncher.MainActivity.mobileconnect;
import static com.format.gesturelauncher.MobileConnectService.Sync;
import static com.format.gesturelauncher.MobileConnectService.lib;
import static com.format.gesturelauncher.MobileConnectService.wearPackList;

public class BackUp extends AppCompatActivity {

    String LocationBackup ="gestureBackup";
    String LocationCurrent ="gesturesNew";

    SharedPreferences sharedPref;
    String lastbackup;

    Button backup;
    Button restore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_back_up);
        sharedPref = getSharedPreferences("backup", MODE_PRIVATE);

        backup=(Button)findViewById(R.id.buttonBackup);
        restore=(Button)findViewById(R.id.buttonRestore);

        getInfo();

        //-----------------------------------------------------------Buttons
        backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(sharedPref.contains("lastdate")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(BackUp.this);
                    builder.setTitle("Backup");
                    builder.setMessage("You are going to overwrite an existing backup, with current gesture library containing "+lib.getGestureEntries().size()+" gestures. Continue?");
                    builder.setCancelable(false);

                    builder.setPositiveButton("Backup", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            backup();
                            dialog.cancel();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

                    builder.show();

                }else {
                    backup();
                }
            }
        });


        restore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(BackUp.this);
                builder.setTitle("Restore");
                builder.setMessage("You are going to overwrite all the current gestures, and restore the backup gesture library copy on "+sharedPref.getString("lastdate","unknown")+". The wearable gesture library will also be overwrote. Continue?");
                builder.setCancelable(false);

                builder.setPositiveButton("Restore", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        if(wearPackList==null){ //如果没有同步得到列表的话
                            Toast.makeText(getApplicationContext(), R.string.waitSync, Toast.LENGTH_LONG).show();
                            Sync(mobileconnect,false);
                        }else {
                            restore();
                        }
                        dialog.cancel();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

                builder.show();

            }
        });

        findViewById(R.id.buttonClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }



    public void getInfo(){

        File file = new File(getFilesDir(), LocationBackup );
        if(file.exists() ){
            findViewById(R.id.LastBackup).setVisibility(View.VISIBLE);
            TextView text =(TextView)findViewById(R.id.textViewDetail);
//            text.setText("Time: " + sharedPref.getString("lastdate","Not found")+
//                    "\nQuantity: " + sharedPref.getInt("quantity",0));

            text.setText(String.format("Time: %1$s\nQuantity: %2$s",  sharedPref.getString("lastdate","Not found"),sharedPref.getInt("quantity",0)));

        }else{
            findViewById(R.id.LastBackup).setVisibility(View.GONE);
        }

    }


    public void backup(){
        byte2FileAndWrite(file2byte(LocationCurrent),LocationBackup);

        Calendar c = Calendar.getInstance();
//        System.out.println("Current time => "+c.getTime());
//        Toast.makeText(getApplicationContext(),""+c.getTime(),Toast.LENGTH_SHORT).show();


//        DateFormat df = new SimpleDateFormat("yyyy/MMM/dd EE", Locale.getDefault());
//        Date today = Calendar.getInstance().getTime();
//        String reportDate = df.format(today);
//        System.out.println(reportDate);
//        Toast.makeText(getApplicationContext(),reportDate,Toast.LENGTH_SHORT).show();

        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy EE HH:mm",Locale.getDefault());
        String formattedDate = df.format(c.getTime());

         sharedPref.edit().putString("lastdate",formattedDate).apply();
         sharedPref.edit().putInt("quantity",lib.getGestureEntries().size()).apply();

        Toast.makeText(getApplicationContext(), R.string.backup_backup_success,Toast.LENGTH_SHORT).show();
        getInfo();

    }


    public void restore(){

            byte2FileAndWrite(file2byte(LocationBackup),LocationCurrent);
            final File mStoreFile = new File(getFilesDir(), LocationCurrent);
            MobileConnectService.lib = GestureLibraries.fromFile(mStoreFile);//导入手势
            if(lib.load()){
                Sync(mobileconnect,true);
                Toast.makeText(getApplicationContext(), R.string.backup_restore_success,Toast.LENGTH_SHORT).show();
                Intent i = new Intent(this,MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            }else {
                Toast.makeText(getApplicationContext(),"Restore failed: Broken file",Toast.LENGTH_SHORT).show();
            }

    }




    public void byte2FileAndWrite(byte[] fileInBytes, String directory){
        String strFilePath = getFilesDir()+"/"+directory; //here, add"/"

        try {
            FileOutputStream fos = new FileOutputStream(strFilePath);
            //String strContent = "Write File using Java ";

            fos.write(fileInBytes);
            fos.close();
        }
        catch(FileNotFoundException ex)   {
            System.out.println("FileNotFoundException : " + ex);
            Toast.makeText(getApplicationContext(),"Backup failed: code 1",Toast.LENGTH_SHORT).show();
        }
        catch(IOException ioe)  {
            System.out.println("IOException : " + ioe);
            Toast.makeText(getApplicationContext(),"Backup failed: code 2",Toast.LENGTH_SHORT).show();
        }

    }  //将收到的byte转换为文件


    public byte[] file2byte(String directory){
        File file = new File(getFilesDir(), directory );//here, don't add"/"

        byte[] b = new byte[(int) file.length()];
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(b);
            for (int i = 0; i < b.length; i++) {
                System.out.print((char)b[i]);
            }
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found.");
            Toast.makeText(getApplicationContext(),"Backup file not found",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        catch (IOException e1) {
            System.out.println("Error Reading The File.");
            Toast.makeText(getApplicationContext(),"Error reading the file",Toast.LENGTH_SHORT).show();
            e1.printStackTrace();
        }
        return b;
    }   //将文件转化成byte以发送到手表


}
