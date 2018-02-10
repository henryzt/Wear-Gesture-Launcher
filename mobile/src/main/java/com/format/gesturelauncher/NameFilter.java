package com.format.gesturelauncher;

/**
 * Created by Henry on 2017/10/10.
 * //To delete and filter between ##
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


    public String getFilteredName(){
        if(method.equals("mapp")){
            String mobilePhonePrefix="\uD83D\uDCF1";
            return mobilePhonePrefix+filteredName;
        }

        return filteredName;
    }

    public String getMethod(){
        return method;
    }

    public String getPackName(){
        return executionName;
    }


    public String getOriginalName(){
        return OriginalName;
    }



}
