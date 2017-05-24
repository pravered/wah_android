package com.weareholidays.bia.activities.journal.people.models;

import android.text.TextUtils;

/**
 * Created by kapil on 12/6/15.
 */
public class PhoneBookContact implements PeopleContact {

    private String contactName;
    private String contactImagePath;
    private String number;
    private boolean selected;
    private boolean addPeoplePlaceholder;
    private String email;

    //implemented for adding dummy image at the end of recycler view
    public PhoneBookContact(boolean addPeoplePlaceholder) {
        this.addPeoplePlaceholder = addPeoplePlaceholder;
    }

    @Override
    public boolean isAddPeoplePlaceholder() {
        return addPeoplePlaceholder;
    }

    public PhoneBookContact() {
        this.setSelected(false);
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        if(TextUtils.isEmpty(contactName))
            contactName = "";
        this.contactName = contactName;
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
        return getContactName();
    }

    @Override
    public String getImageUri() {
        return getContactImagePath();
    }

    @Override
    public Type getType() {
        return Type.PHONE;
    }

    @Override
    public String getIdentifier() {
        return number;
    }

    public String getContactImagePath() {
        return contactImagePath;
    }

    public void setContactImagePath(String contactImagePath) {
        this.contactImagePath = contactImagePath;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString(){
        return contactName;
    }

    @Override
    public int hashCode(){
        if(getIdentifier() == null)
            return 0;
        return getIdentifier().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if( o == null)
            return false;
        if(getIdentifier() == null){
            return false;
        }
        if(o instanceof PeopleContact){
            return getIdentifier().equals(((PeopleContact)o).getIdentifier());
        }
        return super.equals(o);
    }
}
