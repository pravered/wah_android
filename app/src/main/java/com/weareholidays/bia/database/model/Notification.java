package com.weareholidays.bia.database.model;

/**
 * Created by shankar on 12/5/17.
 */

public class Notification {
    private long contentTime;
    private boolean isRead;
    private String content;
    private String userName;
    private String actionType;
    private String actionParams;
    private User notifier;
    private boolean isDeleted;

    private String tripOpenAction;
    private String albumImageOpenAction;
    private String articleOpenAction;

    public Notification() {
    }

    public long getContentTime() {
        return contentTime;
    }

    public void setContentTime(long contentTime) {
        this.contentTime = contentTime;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getActionParams() {
        return actionParams;
    }

    public void setActionParams(String actionParams) {
        this.actionParams = actionParams;
    }

    public User getNotifier() {
        return notifier;
    }

    public void setNotifier(User notifier) {
        this.notifier = notifier;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public String getTripOpenAction() {
        return tripOpenAction;
    }

    public void setTripOpenAction(String tripOpenAction) {
        this.tripOpenAction = tripOpenAction;
    }

    public String getAlbumImageOpenAction() {
        return albumImageOpenAction;
    }

    public void setAlbumImageOpenAction(String albumImageOpenAction) {
        this.albumImageOpenAction = albumImageOpenAction;
    }

    public String getArticleOpenAction() {
        return articleOpenAction;
    }

    public void setArticleOpenAction(String articleOpenAction) {
        this.articleOpenAction = articleOpenAction;
    }
}
