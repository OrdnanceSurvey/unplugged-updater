/*
 * Copyright (C) 2016 Ordnance Survey
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

package uk.os.unplugged.updater.android.notification;

import android.content.Context;

import uk.os.unplugged.updater.UpdateManager;
import uk.os.unplugged.updater.android.R;

public class DefaultTextProvider implements TextProvider {

    private final Context mContext;
    private final String mContentTitle;
    private final String mContentText;

    public DefaultTextProvider(Context context) {
        mContext = context;

        String applicationName = getApplicationName(context);
        if (applicationName.isEmpty()) {
            mContentTitle = context.getString(R.string.loading_service_content_title_default);
        } else {
            mContentTitle = applicationName;
        }
        mContentText = context.getString(R.string.loading_service_content_text_initial);
    }

    @Override
    public final String getContentTitle() {
        return mContentTitle;
    }

    @Override
    public final String getContentText() {
        return mContentText;
    }

    @Override
    public final String getContentText(UpdateManager.Batch batch) {
        return mContext.getString(R.string.loading_service_content_text, batch.getStep(),
                batch.getTotalSteps());
    }

    private static String getApplicationName(Context context) {
        try {
            int stringId = context.getApplicationInfo().labelRes;
            return context.getString(stringId);
        } catch (Exception ignore) {}
        return "";
    }
}
