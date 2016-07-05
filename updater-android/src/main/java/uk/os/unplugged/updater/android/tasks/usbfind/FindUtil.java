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

package uk.os.unplugged.updater.android.tasks.usbfind;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

public final class FindUtil {

    private FindUtil() {}

    private static final File ROOT_STORAGE_LOCATION = new File(File.separator + "storage");

    /**
     * @return a list of directories pointing to USB storage
     */
    protected static List<File> getAccessibleUsbStorageLocations() {
        List<File> result = new ArrayList<>();

        File files[] = ROOT_STORAGE_LOCATION.listFiles();

        boolean hasFiles = files != null && files.length > 0;
        if (hasFiles) {
            for (File file : files) {
                boolean isAccessibleUsbDirectory = file.isDirectory() &&
                        file.getName().toLowerCase().contains("usb") && file.canRead();
                if (isAccessibleUsbDirectory) {
                    Timber.d("Readable USB storage path: " + file.getPath());
                    result.add(file);
                } else {
                    Timber.d("Storage path ignored: " + file.getPath());
                }
            }
        }
        return result;
    }

    /**
     * Note: this method recursively searches storage locations and does not consider the depth
     * or number of files to consider.
     * @param filename name of file to find
     * @param locations source locations to search
     * @return the first matching file
     */
    protected static File findFirst(String filename, List<File> locations) {
        for (File location : locations) {
            File result = findFirst(filename, location);

            boolean isResult = result != null;
            if (isResult) {
                Timber.d("Found! " + result.getAbsolutePath());
                return result;
            }
        }
        return null;
    }

    protected static File findFirst(String filename, File location) {
        if (location.isFile()) {
            return location.canRead() && location.getName().equals(filename) ? location : null;
        } else {
            Timber.d("Directory: " + location.getAbsolutePath());
            return findFirst(filename, Arrays.asList(location.listFiles()));
        }
    }
}
