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

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.HashMap;

public class ProviderImpl implements Provider {
    private String TAG = "ProviderImpl";

    protected Context applicationContext;
    protected HashMap<String,File> rootPaths;
    protected File gazetteerFile;
    protected File mapFile;

    public ProviderImpl(Context applicationContext) {
        setApplicationContext(applicationContext);
    }

    private void setApplicationContext(Context applicationContext) {
        this.applicationContext = applicationContext;
        this.rootPaths = enumerateStorage();
        this.gazetteerFile = null;
        this.mapFile = null;
    }

    @Override
    public final File getGazetteerData() {
        //return new File(Environment.getExternalStorageDirectory(), "gazetteer.db");
        if (gazetteerFile == null) { gazetteerFile = seekFile("gazetteer.db"); }
        return gazetteerFile;
    }

    @Override
    public final File getMapData() {
        //return new File(Environment.getExternalStorageDirectory(), "mbgl-offline.db");
        if (mapFile == null) { mapFile = seekFile("mbgl-offline.db"); }
        return mapFile;
    }

    private HashMap<String, File> enumerateStorage() {
        Log.d(TAG, "Enumerating storage...");
        HashMap<String,File> found = new HashMap<String,File>();
        File storageDir = new File("/storage");
        File files[] = storageDir.listFiles();
        if (files != null && files.length > 0) {
            for (File f : files) {
                if (f.isDirectory() && f.getPath().toLowerCase().contains("usb") && f.canRead()) {
                    Log.d(TAG, "Readable USB storage path: " + f.getPath());
                    found.put(f.getName(), f);
                } else {
                    Log.v(TAG, "Storage path ignored: " + f.getPath());
                }
            }
        }
        Log.d(TAG, "Enumeration complete.");
        return found;
    }

    private File seekFile(String filename) {
        if (rootPaths == null) {
            Log.w(TAG, "seekFile called before setApplicationContext");
            return null;
        }

        for (File root : rootPaths.values()) {
            Log.d(TAG, "Serching: " + root.getAbsolutePath() + ", for: " + filename);
            File found = seekFile(root, filename);
            if (found != null) { return found; }
        }

        Log.d(TAG, "File not found: " + filename);
        return null;
    }

    private File seekFile(File folder, String filename) {
        if (!folder.exists() || !folder.canRead() || !folder.isDirectory()) { return null; }
        Log.d(TAG, "Directory: " + folder.getAbsolutePath());

        // search through files in this folder
        for (File file : folder.listFiles()) {
            if (!file.exists() || !file.canRead()) { continue; }

            if (file.getName().equals(filename)) {
                Log.d(TAG, "Found! " + file.getAbsolutePath());
                return file;
            } else if (file.isDirectory()) {
                // search inside folder
                File searchDeeper = seekFile(file, filename);
                if (searchDeeper != null) { return searchDeeper; }
            }
        }

        return null; // not found
    }

}
