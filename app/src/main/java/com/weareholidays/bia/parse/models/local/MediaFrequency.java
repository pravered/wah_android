package com.weareholidays.bia.parse.models.local;

import com.weareholidays.bia.parse.models.Media;

/**
 * Created by Teja on 23/07/15.
 */
public class MediaFrequency {

    private Media media;
    private int frequency;

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        this.media = media;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }
}
