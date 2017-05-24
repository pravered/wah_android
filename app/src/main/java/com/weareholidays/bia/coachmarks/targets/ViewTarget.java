/*
 * Copyright 2014 Alex Curran
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.weareholidays.bia.coachmarks.targets;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * Target a view on the screen. This will centre the target on the view.
 */
public class ViewTarget implements Target {
    private final View mView;


    public ViewTarget(View view) {
        mView = view;
    }

    public ViewTarget(int viewId, Activity activity) {
        this.mView = activity.findViewById(viewId);
    }

    public Point getPoint() {
        int[] location = new int[2];
        int x = 0;
        int y = 0;
        if(this.mView != null){
            this.mView.getLocationInWindow(location);
            x = location[0] + this.mView.getWidth() / 2;
            y = location[1] + this.mView.getHeight() / 2;
        }
        return new Point(x, y);
    }
}