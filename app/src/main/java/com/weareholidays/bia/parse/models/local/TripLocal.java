package com.weareholidays.bia.parse.models.local;

import com.weareholidays.bia.activities.journal.people.models.PeopleContact;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Teja on 06/06/15.
 */
public class TripLocal implements Serializable {

    private String name;
    private String featureImage;
    private boolean accessCameraRoll;
    private boolean accessFacebook;
    private boolean accessTwitter;
    private boolean accessInstagram;
    private boolean accessLocation;
    private boolean accessCheckIn;
    private boolean accessSync;
    private ArrayList<PeopleContact> people;
    private boolean accessPublic;

    public TripLocal(){
        people = new ArrayList<>();
        accessCameraRoll = true;
        accessLocation = true;
        accessCheckIn = true;
        accessSync = true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFeatureImage() {
        return featureImage;
    }

    public void setFeatureImage(String featureImage) {
        this.featureImage = featureImage;
    }

    public boolean isAccessCameraRoll() {
        return accessCameraRoll;
    }

    public void setAccessCameraRoll(boolean accessCameraRoll) {
        this.accessCameraRoll = accessCameraRoll;
    }

    public boolean isAccessFacebook() {
        return accessFacebook;
    }

    public void setAccessFacebook(boolean accessFacebook) {
        this.accessFacebook = accessFacebook;
    }

    public boolean isAccessTwitter() {
        return accessTwitter;
    }

    public void setAccessTwitter(boolean accessTwitter) {
        this.accessTwitter = accessTwitter;
    }

    public boolean isAccessInstagram() {
        return accessInstagram;
    }

    public void setAccessInstagram(boolean accessInstagram) {
        this.accessInstagram = accessInstagram;
    }

    public boolean isAccessLocation() {
        return accessLocation;
    }

    public void setAccessLocation(boolean accessLocation) {
        this.accessLocation = accessLocation;
    }

    public boolean isAccessCheckIn() {
        return accessCheckIn;
    }

    public void setAccessCheckIn(boolean accessCheckIn) {
        this.accessCheckIn = accessCheckIn;
    }

    public boolean isAccessSync() {
        return accessSync;
    }

    public void setAccessSync(boolean accesssUpload) {
        this.accessSync = accesssUpload;
    }

    public ArrayList<PeopleContact> getPeople() {
        return people;
    }

    public void setPeople(ArrayList<PeopleContact> people) {
        this.people = people;
    }

    public boolean isAccessPublic() {
        return accessPublic;
    }

    public void setAccessPublic(boolean accessPublic) {
        this.accessPublic = accessPublic;
    }
//
//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeString(name);
//        dest.writeParcelable(featureImage, flags);
//        dest.writeByte((byte) (accessLocation ? 1 : 0));
//        dest.writeByte((byte) (accessCheckIn ? 1 : 0));
//        dest.writeByte((byte) (accessCameraRoll ? 1 : 0));
//        dest.writeByte((byte) (accessFacebook ? 1 : 0));
//        dest.writeByte((byte) (accessTwitter ? 1 : 0));
//        dest.writeByte((byte) (accessInstagram ? 1 : 0));
//        dest.writeByte((byte) (accessSync ? 1 : 0));
//        dest.writeStringList(people);
//        dest.writeByte((byte) (accessPublic ? 1 : 0));
//    }
//
//    public static final Parcelable.Creator<TripLocal> CREATOR
//            = new Parcelable.Creator<TripLocal>() {
//        public TripLocal createFromParcel(Parcel in) {
//            return new TripLocal(in);
//        }
//
//        public TripLocal[] newArray(int size) {
//            return new TripLocal[size];
//        }
//    };
//
//    private TripLocal(Parcel in){
//        name = in.readString();
//        featureImage = in.readParcelable(Uri.class.getClassLoader());
//        accessLocation = in.readByte() != 0;
//        accessCheckIn = in.readByte() != 0;
//        accessCameraRoll = in.readByte() != 0;
//        accessFacebook = in.readByte() != 0;
//        accessTwitter = in.readByte() != 0;
//        accessInstagram = in.readByte() != 0;
//        accessSync = in.readByte() != 0;
//        people = new ArrayList<>();
//        in.readStringList(people);
//        accessPublic = in.readByte() != 0;
//    }
}
