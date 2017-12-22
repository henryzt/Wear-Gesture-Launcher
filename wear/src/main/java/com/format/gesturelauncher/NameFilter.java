package com.format.gesturelauncher;

/**
 * Created by 子恒 on 2017/10/10.
 * //自己声明的，用于去掉包名字##后的内容
 * Copied from mobile
 */

public class NameFilter {
    String OriginalName; //example: "Spotify##wearapp##com.spotify.spotify"
    String filteredName;//spotify
    String method;//wearapp
    String executionName;//com.xx.xx


    public NameFilter(String NameOriginal ) {
        this.OriginalName=NameOriginal;
        if(OriginalName.indexOf("##") != -1){
            String[] spilt =OriginalName.split("##");

            filteredName= spilt[0];
            method=spilt[1];
            executionName=spilt[2];
        }else {
            filteredName= NameOriginal;
            method="none";
            executionName=NameOriginal;
        }
    }


    public String GetfiltedName(){
        return filteredName;
    } //过滤掉名字里多余的信息

    public String getMethod(){
        return method;
    }

    public String getPackName(){
        return executionName;
    }


    public String getOriginalName(){
        return OriginalName;
    } //返回原名称

    public String changeFiltedName(String newName){
        return newName+"##"+method+"##"+executionName;
    }



}
