package com.weareholidays.bia.adapters;

import android.os.Parcel;
import android.support.annotation.NonNull;

import com.felipecsl.asymmetricgridview.library.model.AsymmetricItem;
import com.weareholidays.bia.parse.models.Media;

public class DemoItem implements AsymmetricItem {

    private int columnSpan;
    private int rowSpan;
    private int position;
    private Media media;

    public DemoItem() {
        this(1, 1, 0);
    }

    public DemoItem(int columnSpan, int rowSpan, int position) {
        this.columnSpan = columnSpan;
        this.rowSpan = rowSpan;
        this.position = position;
    }

    public DemoItem(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public int getColumnSpan() {
        return columnSpan;
    }

    @Override
    public int getRowSpan() {
        return rowSpan;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return String.format("%s: %sx%s", position, rowSpan, columnSpan);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    private void readFromParcel(Parcel in) {
        columnSpan = in.readInt();
        rowSpan = in.readInt();
        position = in.readInt();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(columnSpan);
        dest.writeInt(rowSpan);
        dest.writeInt(position);
    }

    /* Parcelable interface implementation */
    public static final Creator<DemoItem> CREATOR = new Creator<DemoItem>() {

        @Override
        public DemoItem createFromParcel(@NonNull Parcel in) {
            return new DemoItem(in);
        }

        @Override
        @NonNull
        public DemoItem[] newArray(int size) {
            return new DemoItem[size];
        }
    };

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        this.media = media;
    }
}
