/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.current;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

/**
 * An example of tab content that launches an activity via {@link android.widget.TabHost.TabSpec#setContent(android.content.Intent)}
 */
public class DOLNumbers extends TabActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final TabHost tabHost = getTabHost();

        tabHost.addTab(tabHost.newTabSpec("Current Numbers")
                .setIndicator("Current Numbers",getResources().getDrawable(R.drawable.numbersxml))                
                .setContent(new Intent(this, Numbers.class)));

        tabHost.addTab(tabHost.newTabSpec("News Releases")
                .setIndicator("News Releases",getResources().getDrawable(R.drawable.newsxml))
                .setContent(new Intent(this, NewsReleases.class)));        
        
    }
}
