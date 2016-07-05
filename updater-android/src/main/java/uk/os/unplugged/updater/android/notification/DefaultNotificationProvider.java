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

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import uk.os.unplugged.updater.UpdateManager;
import uk.os.unplugged.updater.android.NotificationProvider;
import uk.os.unplugged.updater.android.R;

public class DefaultNotificationProvider implements NotificationProvider {

    private static final int MAX_PROGRESS = 100;

    private final Context mContext;
    private final TextProvider mTextProvider;

    public DefaultNotificationProvider(Context context, TextProvider textProvider) {
        mContext = context;
        mTextProvider = textProvider;
    }

    public DefaultNotificationProvider(Context context) {
        this(context, new DefaultTextProvider(context));
    }

    @Override
    public final Notification getNotification(Context context) {
        return getBaseNotification(context)
                .setContentText(mTextProvider.getContentText())
                .build();
    }

    @Override
    public final Notification getNotification(Context context, UpdateManager.Batch batch) {
        return getBaseNotification(context)
                .setContentText(mTextProvider.getContentText(batch))
                .setProgress(MAX_PROGRESS, (int) (batch.getProgress() * MAX_PROGRESS), false)
                .setOnlyAlertOnce(true).build();
    }

    private NotificationCompat.Builder getBaseNotification(Context context) {
        String packageName = mContext.getPackageName();
        Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(packageName);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(context)
                .setContentIntent(pendingIntent)
                .setContentTitle(mTextProvider.getContentTitle())
                .setSmallIcon(R.drawable.file_download)
                .setOnlyAlertOnce(true);
    }
}
