package com.weareholidays.bia.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.weareholidays.bia.R;
import com.weareholidays.bia.activities.journal.people.models.PeopleContact;

import java.util.List;

import wahCustomViews.view.WahImageView;

/**
 * Created by kapil on 10/6/15.
 */
public class PhoneBookAdapter extends ArrayAdapter {
    private List<? extends PeopleContact> contacts;
    private LayoutInflater mInflater;
    private OnContactChangedListener mListener;

    public PhoneBookAdapter(Context context, List<? extends PeopleContact> contacts, OnContactChangedListener listener) {
        super(context, R.layout.phonebook_row,contacts.toArray());
        this.contacts = contacts;
        this.mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mListener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, R.layout.phonebook_row);
    }

    private View createViewFromResource(int position, View convertView, ViewGroup parent,
                                        int resource) {
        View view;

        if (convertView == null) {
            view = mInflater.inflate(resource, parent, false);
        } else {
            view = convertView;
        }

        final PeopleContact contact = (PeopleContact)getItem(position);

        // Find fields to populate in inflated template
        TextView contactName = (TextView) view.findViewById(R.id.contact_name);
        WahImageView contactImage = (WahImageView) view.findViewById(R.id.contact_image);
        ImageView contactTypePhone = (ImageView) view.findViewById(R.id.contact_type_phone);
        ImageView contactTypeFb = (ImageView) view.findViewById(R.id.contact_type_fb);
        ImageView contactTypeTwitter = (ImageView) view.findViewById(R.id.contact_type_twitter);
        // Extract properties from cursor
        String name = contact.getName();
        String image = contact.getImageUri();
        PeopleContact.Type type = contact.getType();
        // Populate fields with extracted properties
        /*Glide.with(getContext())
                .load(image)
                .centerCrop()
                .placeholder(R.drawable.user_placeholder)
                .crossFade()
                .into(contactImage);*/
        contactImage.setImageUrl(image);

        contactName.setText(String.valueOf(name));

        if (type == PeopleContact.Type.FB) {
            contactTypePhone.setVisibility(View.GONE);
            contactTypeTwitter.setVisibility(View.GONE);
            contactTypeFb.setVisibility(View.VISIBLE);

        } else if (type == PeopleContact.Type.TWITTER) {
            contactTypePhone.setVisibility(View.GONE);
            contactTypeTwitter.setVisibility(View.VISIBLE);
            contactTypeFb.setVisibility(View.GONE);
        }


        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox_contact);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                contact.setSelected(isChecked);
                if(mListener != null)
                    mListener.onChanged(contact);
            }
        });

        checkBox.setChecked(contact.isSelected());

        return view;
    }

    public interface OnContactChangedListener{
        void onChanged(PeopleContact contact);
    }
}
