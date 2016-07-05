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

package uk.os.unplugged.updater.android.demo;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Locale;

import rx.Subscriber;
import rx.Subscription;
import timber.log.Timber;
import uk.os.unplugged.updater.UpdateManager;
import uk.os.unplugged.updater.android.LoadingService;
import uk.os.unplugged.updater.android.demo.files.TempFileProvider;

public class LoadingActivity extends AppCompatActivity {

    private static final int DECIMAL_PERCENTAGE_MULTIPLIER = 100;

    private TextView mBusyNotification;
    private TextView mServiceStatus;
    private ProgressBar mProgressBar;
    private TextView mErrors;

    private static final int REQUEST_CODE_EXTERNAL_SDCARD_ACCESS = 1;

    private LoadingService mService;
    private Subscription mSubscription;

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className,
                                       IBinder binder) {
            LoadingService.LoadingServiceBinder b = (LoadingService.LoadingServiceBinder) binder;
            mService = b.getService();

            if (mServiceStatus != null) {
                mServiceStatus.setText("Connected");
            } else {
                Toast.makeText(LoadingActivity.this, "Connected", Toast.LENGTH_SHORT)
                        .show();
            }

            if (!isSubscription()) {
                mSubscription = getSubscription();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            if (mServiceStatus != null) {
                mServiceStatus.setText("Disconnected");
            } else {
                Toast.makeText(LoadingActivity.this, "Disconnected", Toast.LENGTH_SHORT)
                        .show();
            }

            mService = null;
            if (mSubscription != null) {
                mSubscription.unsubscribe();
                mSubscription = null;
            }
        }
    };

    public void onDeleteTestFiles(View view) {

        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... voids) {
                return TempFileProvider.deleteDemoSourceFiles(LoadingActivity.this);
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    Toast.makeText(LoadingActivity.this, "done", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoadingActivity.this, "cannot delete the files", Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    public void onMakeBrokenTestFiles(View view) {

        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... voids) {
                boolean success = false;
                try {
                    TempFileProvider.createBrokenDemoSourceFiles(LoadingActivity.this);
                    success = true;
                } catch (IOException e) {
                    Timber.e(e, "failure to make files");
                }
                return success;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    Toast.makeText(LoadingActivity.this, "done", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoadingActivity.this, "cannot make the files", Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    public void onMakeTestFiles(View view) {

        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... voids) {
                boolean success = false;
                try {
                    TempFileProvider.createDemoSourceFiles(LoadingActivity.this);
                    success = true;
                } catch (IOException e) {
                    Timber.e(e, "failure to make files");
                }
                return success;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    Toast.makeText(LoadingActivity.this, "done", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoadingActivity.this, "cannot make the files", Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_EXTERNAL_SDCARD_ACCESS) {

            int granted = 0;
            int denied = 0;

            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    granted++;
                } else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    denied++;
                }
            }

            Toast.makeText(getApplicationContext(),
                    String.format(Locale.ENGLISH,
                            "permission granted: %d.  Denied: %d.", granted, denied),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void onRequestReadPermissions(View view) {
        ActivityCompat.requestPermissions(
                LoadingActivity.this, new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE },
                REQUEST_CODE_EXTERNAL_SDCARD_ACCESS);
    }

    public void onUpdate(View view) {
        if (isPermissionGranted()) {

            // clear any errors - starting again
            mErrors.setText("");

            // raise service to foreground
            Intent intent = new Intent(LoadingActivity.this, DemoLoadingService.class);
            intent.setAction(DemoLoadingService.ACTION_START);
            startService(intent);
            update();
        } else {
            Toast.makeText(this, "Request Permission and try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        mBusyNotification = (TextView) findViewById(R.id.activity_loading_busy);
        mServiceStatus = (TextView) findViewById(R.id.activity_loading_service_status);
        mProgressBar = (ProgressBar) findViewById(R.id.activity_loading_progress_bar);
        mErrors = (TextView) findViewById(R.id.activity_loading_errors);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(LoadingActivity.this, DemoLoadingService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mConnection);

        if (mSubscription != null) {
            mSubscription.unsubscribe();
            mSubscription = null;
        }
    }


    private boolean isPermissionGranted() {
        return (ContextCompat.checkSelfPermission(LoadingActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(LoadingActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    private boolean isSubscription() {
        // cannot simply do: boolean isSubscription = mSubscription != null;
        // race condition - when we call 'mSubscription = getSubscription' it can onError _before_ assignment!  Thus mSubscription is not null.
        if (mSubscription != null && mSubscription.isUnsubscribed()) {
            mSubscription = null;
        }
        return mSubscription != null;
    }

    private void update() {
        if (mService == null) {
            Timber.e("service not connected");
            return;
        }

        if (isSubscription()) {
            Timber.w("update request ignored - already busy");
            return;
        }

        Timber.e("subscribe");
        mSubscription = getSubscription();
    }

    private Subscription getSubscription() {
        return mService.getObservable().subscribe(new Subscriber<UpdateManager.Batch>() {
            @Override
            public void onCompleted() {
                Log.e(LoadingActivity.class.getSimpleName(), "Task completed");
                mSubscription = null;
                startActivity(new Intent(LoadingActivity.this, MainActivity.class));
            }

            @Override
            public void onError(Throwable e) {
                mSubscription = null;
                Log.e(LoadingActivity.class.getSimpleName(), "Task errored", e);
                mErrors.setText(e.getMessage());
            }

            @Override
            public void onNext(UpdateManager.Batch batch) {
                String message = LoadingActivity.this.getString(
                        uk.os.unplugged.updater.android.R.string.loading_service_content_text,
                        batch.getStep(),
                        batch.getTotalSteps());
                mBusyNotification.setText(message);
                mProgressBar.setProgress((int) (batch.getProgress() *
                        DECIMAL_PERCENTAGE_MULTIPLIER));
            }
        });
    }
}
