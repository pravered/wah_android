package com.weareholidays.bia.parse.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.List;

/**
 * Created by Teja on 03/06/15.
 */
@ParseClassName("FileLocal")
public class FileLocal  extends ParseObject{

    public static String PROXY_CLASS = "proxyClass";
    private static String LOCAL_URI = "localUri";
    private static String FILE_UPLOADED = "fileUploaded";
    private static String FILE_LINKED = "fileLinked";
    public static String PROXY_FIELD = "proxyField";
    public static String FILE_NAME = "fileName";

    public String getLocalUri(){
        return getString(LOCAL_URI);
    }

    public void setLocalUri(String localUri){
        put(LOCAL_URI,localUri);
    }

    public boolean isFileUploaded(){
        return  getBoolean(FILE_UPLOADED);
    }

    public void setFileUploaded(boolean uploaded){
        put(FILE_UPLOADED,uploaded);
    }

    public boolean isFileLinked(){
        return  getBoolean(FILE_LINKED);
    }

    public void setFileLinked(boolean uploaded){
        put(FILE_LINKED,uploaded);
    }

    public String getFileName(){
        return getString(FILE_NAME);
    }

    public void setFileName(String fileName){
        put(FILE_NAME,fileName);
    }

    public String getProxyField(){
        return getString(PROXY_FIELD);
    }

    public void setProxyField(String field){
        put(PROXY_FIELD,field);
    }

    public ParseObject getProxyClass(){
        List<ParseObject> list = getList(PROXY_CLASS);
        if(list != null && list.size() > 0){
            return list.get(0);
        }
        return null;
    }

    public void setProxyClass(ParseObject parseObject){
        addUnique(PROXY_CLASS,parseObject);
    }
}
