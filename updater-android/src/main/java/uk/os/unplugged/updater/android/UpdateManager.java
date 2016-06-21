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
import java.io.IOException;

public final class UpdateManager {

    public static final class Builder {

        private Context applicationContext = null;
        private File destinationGazetteerFile = null;
        private File destinationMapFile = null;
        private File sourceGazetteerFile = null;
        private File sourceMapFile = null;

        public Builder setGazetteerDestination(File file) {
            destinationGazetteerFile = file;
            return this;
        }

        public Builder setMapDestination(File file) {
            destinationMapFile = file;
            return this;
        }

        public Builder setProvider(Provider provider) {
            sourceGazetteerFile = provider.getGazetteerData();
            sourceMapFile = provider.getMapData();
            return this;
        }

        public UpdateManager build() {
            return new UpdateManager(this);
        }
    }

    private final File mDestinationGazetteerFile;
    private final File mDestinationMapFile;
    private final File mSourceGazetteerFile;
    private final File mSourceMapFile;

    public void update() {

        try {
            boolean isGazetteerFile = mDestinationGazetteerFile != null && mSourceGazetteerFile != null;
            if (isGazetteerFile) {
                process(mSourceGazetteerFile, mDestinationGazetteerFile);
            }

            boolean isMapFile = mDestinationMapFile != null && mSourceMapFile != null;
            if (isMapFile) {
                process(mSourceMapFile, mDestinationMapFile);
            }
        } catch (Exception e) {
            Log.e(UpdateManager.class.getSimpleName(), "cannot update", e);
        }
    }

    private void process(File source, File destination) throws IOException {
        // process if source data
        boolean ignore = !source.exists();
        if (ignore) {
            Log.d(UpdateManager.class.getSimpleName(),
                    "ignoring update for " + source.getAbsolutePath());
            return;
        }

        final String expectedMd5 = Util.getMd5FromAdjacentFile(source);

        // verify source data
        boolean isSourceValid = FileValidator.isMd5Valid(source, expectedMd5);
        if (!isSourceValid) {
            throw new IOException("source data fails MD5 validation");
        }

        // remove destination files inc. temp files
        if (destination.exists()) {
            if (!destination.delete()) {
                throw new IOException("Cannot delete file " + destination.getAbsolutePath());
            }
        }

        File temp = new File(destination.getAbsolutePath() + ".tmp");
        if (temp.exists()) {
            if (!temp.delete()) {
                throw new IOException("Cannot delete file " + destination.getAbsolutePath());
            }
        }

        // copy to temp and validate
        Util.copy(source, temp);

        boolean isTempValid = FileValidator.isMd5Valid(temp, expectedMd5);
        if (!isTempValid) {
            Log.d(UpdateManager.class.getSimpleName(),
                    "copied file is invalid - aborting");
            if (!temp.delete()) {
                throw new IOException("Cannot delete file " + destination.getAbsolutePath());
            }
            return;
        }

        // rename verified file
        boolean success = temp.renameTo(destination);
        if (!success) {
            Log.d(UpdateManager.class.getSimpleName(),
                    "cannot rename file to " + destination.getAbsolutePath());
            if (!temp.delete()) {
                throw new IOException("Cannot delete file " + destination.getAbsolutePath());
            }
            return;
        }

        Log.d(UpdateManager.class.getSimpleName(),
                "successfully updated " + destination.getAbsolutePath());
    }

    private UpdateManager(Builder builder) {
        mDestinationGazetteerFile = builder.destinationGazetteerFile;
        mDestinationMapFile = builder.destinationMapFile;
        mSourceGazetteerFile = builder.sourceGazetteerFile;
        mSourceMapFile = builder.sourceMapFile;
    }
}
