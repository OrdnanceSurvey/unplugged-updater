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
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.io.File;

import uk.os.unplugged.updater.android.AsyncCopyTask;
import uk.os.unplugged.updater.android.CopyTaskCompletionListener;

public class MainActivity extends AppCompatActivity implements CopyTaskCompletionListener {

    private static final int REQUEST_CODE_EXTERNAL_COPY = 0;

    public void onCopyClicked(View v) {
        int readPermission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);

        boolean hasPermission = readPermission == PackageManager.PERMISSION_GRANTED;
        if (hasPermission) {
            copy();
        } else {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this, new String[]{"android.permission.READ_EXTERNAL_STORAGE"},
                    REQUEST_CODE_EXTERNAL_COPY);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_EXTERNAL_COPY) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                copy();
            } else {
                Toast.makeText(getApplicationContext(), "permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private void copy() {
        final File gazetteer = new File(MainActivity.this.getExternalFilesDir(null) + File.separator
                + "gazetteer.done");

        final File map = new File(MainActivity.this.getExternalFilesDir(null) + File.separator
                + "map.done");

        /*
        new Thread(new Runnable() {
            @Override
            public void run() {
                UpdateManager updateManager = new UpdateManager.Builder()
                        .setGazetteerDestination(gazetteer)
                        .setMapDestination(map)
                        .setProvider(new ProviderImpl())
                        .build();

                updateManager.update();
            }
        }).start();
        */

        new AsyncCopyTask(this, this, gazetteer, map).execute();
    }

    @Override
    public void copyTaskCompleted(boolean success, String message) {
        Toast.makeText(this,
                "Copy completed: " + (success
                        ? "successfully."
                        : "with errors. " + message),
                Toast.LENGTH_LONG)
                .show();
    }
}
