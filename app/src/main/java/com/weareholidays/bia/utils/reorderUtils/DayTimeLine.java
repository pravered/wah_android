package com.weareholidays.bia.utils.reorderUtils;

import com.weareholidays.bia.parse.models.Timeline;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Teja on 06-07-2015.
 */
public class DayTimeLine {
    private List<Timeline> timelines;
    private int skip;
    private int limit;
    private boolean reordered;
    private boolean deleted;

    private List<Timeline> deletedTimeLines = new ArrayList<>();

    public DayTimeLine(){
        timelines = new ArrayList<>();
        skip = 0;
        limit = 15;
    }

    public List<Timeline> getTimelines() {
        return timelines;
    }

    public void setTimelines(List<Timeline> timelines) {
        this.timelines = timelines;
    }

    public int getSkip() {
        return skip;
    }

    public void setSkip(int skip) {
        this.skip = skip;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public boolean isReordered() {
        return reordered;
    }

    public void setReordered(boolean reordered) {
        this.reordered = reordered;
    }

    public List<Timeline> getDeletedTimeLines() {
        return deletedTimeLines;
    }

    public void setDeletedTimeLines(List<Timeline> deletedTimeLines) {
        this.deletedTimeLines = deletedTimeLines;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
