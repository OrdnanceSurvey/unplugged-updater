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

package uk.os.unplugged.updater.tasks.copy;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.os.unplugged.updater.Task;
import uk.os.unplugged.updater.ProgressListener;

public class VerifiedCopy implements Task {

    private static final Logger LOGGER = Logger.getLogger(VerifiedCopy.class.getSimpleName());

    private final File mSource;
    private final File mDestination;

    public VerifiedCopy(File source, File destination) {
        mSource = source;
        mDestination = destination;
    }

    @Override
    public void execute(ProgressListener progressListener) throws IOException {
        process(mSource, mDestination, progressListener);
    }

    private void process(File source, File destination, ProgressListener progressListener)
            throws IOException {
        progressListener.onProgress(0F);

        // process if source data
        boolean ignore = !source.exists();
        if (ignore) {
            LOGGER.log(Level.WARNING, "ignoring update for " + source.getAbsolutePath());
            progressListener.onProgress(1F);
            return;
        }

        progressListener.onProgress(0.1F);

        final String expectedMd5 = Util.getMd5FromAdjacentFile(source);

        // verify source data
        boolean isSourceValid = FileValidator.isMd5Valid(source, expectedMd5);
        if (!isSourceValid) {
            throw new IOException("source data fails MD5 validation");
        }

        progressListener.onProgress(0.3F);

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
        progressListener.onProgress(0.4F);

        // copy to temp and validate
        Util.copy(source, temp);

        boolean isTempValid = FileValidator.isMd5Valid(temp, expectedMd5);
        if (!isTempValid) {
            LOGGER.log(Level.WARNING, "copied file is invalid - aborting");
            if (!temp.delete()) {
                throw new IOException("Cannot delete file " + destination.getAbsolutePath());
            }
            return;
        }
        progressListener.onProgress(0.8F);

        // rename verified file
        boolean success = temp.renameTo(destination);
        if (!success) {
            LOGGER.log(Level.WARNING, "cannot rename file to " + destination.getAbsolutePath());
            if (!temp.delete()) {
                throw new IOException("Cannot delete file " + destination.getAbsolutePath());
            }
            return;
        }

        progressListener.onProgress(1F);

        LOGGER.log(Level.INFO, "successfully updated " + destination.getAbsolutePath());
    }
}
