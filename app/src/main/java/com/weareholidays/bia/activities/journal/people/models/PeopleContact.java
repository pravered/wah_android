package com.weareholidays.bia.activities.journal.people.models;

import java.io.Serializable;

/**
 * Created by Teja on 23/06/15.
 */
public interface PeopleContact extends Serializable {

    boolean isSelected();

    boolean isAddPeoplePlaceholder();

    void setSelected(boolean selected);

    String getName();

    String getImageUri();

    Type getType();

    String getIdentifier();

    public enum Type{
        PHONE,FB, TWITTER
    }
}
