package com.weareholidays.bia.database.model;

/**
 * Created by shankar on 12/5/17.
 */

public class FileLocal {
    private Object proxyClass;
    private String localUri;
    private boolean fileUploaded;
    private boolean fileLinked;
    private String proxyField;
    private String fileName;

    public FileLocal() {
    }

    public Object getProxyClass() {
        return proxyClass;
    }

    public void setProxyClass(Object proxyClass) {
        this.proxyClass = proxyClass;
    }

    public String getLocalUri() {
        return localUri;
    }

    public void setLocalUri(String localUri) {
        this.localUri = localUri;
    }

    public boolean isFileUploaded() {
        return fileUploaded;
    }

    public void setFileUploaded(boolean fileUploaded) {
        this.fileUploaded = fileUploaded;
    }

    public boolean isFileLinked() {
        return fileLinked;
    }

    public void setFileLinked(boolean fileLinked) {
        this.fileLinked = fileLinked;
    }

    public String getProxyField() {
        return proxyField;
    }

    public void setProxyField(String proxyField) {
        this.proxyField = proxyField;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
