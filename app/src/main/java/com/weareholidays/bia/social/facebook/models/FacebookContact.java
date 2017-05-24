package com.weareholidays.bia.social.facebook.models;

import android.util.Log;

import com.weareholidays.bia.activities.journal.people.models.PeopleContact;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Teja on 23/06/15.
 */
public class FacebookContact implements PeopleContact {

    private final static String TAG = "FacebookContact";

    private String name;
    private String imageUri;
    private boolean selected;
    private String id;
    private boolean addPeoplePlaceholder;

    //implemented for adding dummy image at the end of recycler view
    public FacebookContact(boolean addPeoplePlaceholder) {
        this.addPeoplePlaceholder = addPeoplePlaceholder;
    }

    @Override
    public boolean isAddPeoplePlaceholder() {
        return addPeoplePlaceholder;
    }

    public FacebookContact() {
        this.addPeoplePlaceholder = false;
    }
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    @Override
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    @Override
    public String getImageUri() {
        return imageUri;
    }

    @Override
    public Type getType() {
        return Type.FB;
    }

    @Override
    public String getIdentifier() {
        return id;
    }

    @Override
    public String toString(){
        return getName();
    }

    public static FacebookContact parseContact(JSONObject jsonObject){
        FacebookContact contact = new FacebookContact();
        try {
            contact.setId(jsonObject.getString("id"));
            contact.setName(jsonObject.getString("name"));
            contact.setImageUri("");
            if(!jsonObject.isNull("picture")){
                JSONObject picture = jsonObject.getJSONObject("picture");
                if(!jsonObject.isNull("data")){
                    JSONObject data = jsonObject.getJSONObject("data");
                    contact.setImageUri(data.getString("url"));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while parsing contacts", e);
        }
        return contact;
    }

    public static List<FacebookContact> parsePosts(JSONArray postsArray){
        List<FacebookContact> posts = new ArrayList<>();
        try {
            for(int j = 0; j < postsArray.length(); j++){
                JSONObject pj = postsArray.getJSONObject(j);
                posts.add(parseContact(pj));
            }
        }
        catch (Exception e){
            Log.e(TAG,"Error while parsing contacts",e);
        }
        return posts;
    }

    public static List<FacebookContact> parseContacts(JSONObject postsData){
        List<FacebookContact> posts = new ArrayList<>();
        try {
            return parsePosts(postsData.getJSONArray("data"));
        }
        catch (Exception e){
            Log.e(TAG,"Error while parsing contacts",e);
        }
        return posts;
    }

    @Override
    public int hashCode(){
        return getIdentifier().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if( o == null)
            return false;
        if(o instanceof PeopleContact){
            return getIdentifier().equals(((PeopleContact)o).getIdentifier());
        }
        return super.equals(o);
    }
}
