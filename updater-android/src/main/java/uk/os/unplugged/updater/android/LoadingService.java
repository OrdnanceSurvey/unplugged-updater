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

package uk.os.unplugged.updater.android;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import timber.log.Timber;
import uk.os.unplugged.updater.Task;
import uk.os.unplugged.updater.UpdateManager;
import uk.os.unplugged.updater.android.notification.DefaultNotificationProvider;

public abstract class LoadingService extends Service {

    public static final String ACTION_START = "uk.os.unplugged.updater.android.loading.start";

    private static final String TAG = LoadingService.class.getSimpleName();

    private final IBinder mBinder = new LoadingServiceBinder();

    private BehaviorSubject<UpdateManager.Batch> mSubject = BehaviorSubject.create();
    private Subscription mSubscription;

    private NotificationManager mNotificationManager;
    private NotificationProvider mNotificationProvider;

    public final Observable<UpdateManager.Batch> getObservable() {
        return mSubject;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        Log.v(TAG, "onCreate()");
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationProvider = getNotificationProvider();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "onStartCommand()");

        if (null != intent) {
            String action = intent.getAction();
            if (LoadingService.ACTION_START.equals(action)) {
                boolean isReady = mSubscription == null;

                final Context appContext = getApplicationContext();

                if (isReady) {
                    final int id = getNotificationId();
                    mSubscription = UpdateManager
                            .create(getTasks())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnSubscribe(new Action0() {
                                @Override
                                public void call() {
                                    Notification notification = mNotificationProvider.getNotification(appContext);
                                    startForeground(id, notification);
                                }
                            }).doOnNext(new Action1<UpdateManager.Batch>() {
                                @Override
                                public void call(UpdateManager.Batch batch) {
                                    Notification progressNotification =
                                            mNotificationProvider
                                                    .getNotification(getApplicationContext(),
                                                            batch);
                                    mNotificationManager.notify(id, progressNotification);
                                }
                            }).doOnError(new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                    Timber.e(throwable, "problem executing task(s)");
                                }
                            })
                            .doOnTerminate(new Action0() {
                                @Override
                                public void call() {
                                    stopForeground(true);
                                    if (mNotificationManager != null) {
                                        mNotificationManager.cancel(id);
                                    }
                                    mSubscription = null;
                                    // we are done - subject cannot be reused
                                    mSubject = BehaviorSubject.create();
                                }
                            }).subscribe(mSubject);
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    protected abstract List<Task> getTasks();

    /**
     * @return a notification id for the notifications for this service
     */
    protected abstract int getNotificationId();

    protected NotificationProvider getNotificationProvider() {
        return new DefaultNotificationProvider(this);
    }

    public class LoadingServiceBinder extends Binder {
        public final LoadingService getService() {
            return LoadingService.this;
        }
    }
}
