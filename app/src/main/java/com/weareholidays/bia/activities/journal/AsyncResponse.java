package com.weareholidays.bia.activities.journal;

/**
 * Created by challa on 23/6/15.
 */
public interface AsyncResponse {

    void processFinish(ReturnLocation returnLocation);

    int getType();
}
