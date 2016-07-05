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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.os.unplugged.updater.ProgressListener;
import uk.os.unplugged.updater.Task;

/**
 * Task to find the given files on USB devices.
 * Note: on modern versions of android it may be necessary to use the Storage Access Framework
 */
public class FindFilesTask implements Task {

    private final String[] mFilenames;
    private final Map<String, File> mResults = new HashMap<>();

    public FindFilesTask(String... filenames) {
        mFilenames = filenames;
    }

    @Override
    public final void execute(ProgressListener progressListener) throws Exception {
        List<File> usbStorageLocations = FindUtil.getAccessibleUsbStorageLocations();

        for (String filename : mFilenames) {
            File found = FindUtil.findFirst(filename, usbStorageLocations);

            boolean isFound = found != null;
            if (isFound) {
                mResults.put(filename, found);
            }
        }
    }

    public final Map<String, File> getResults() {
        return mResults;
    }
}
