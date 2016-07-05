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

package uk.os.unplugged.updater.android.demo.files;

import java.io.File;

public class FileRef {

    private final File mSource;
    private final File mDestination;

    public FileRef(File source, File destination) {
        mSource = source;
        mDestination = destination;
    }

    public File getSource() {
        return mSource;
    }

    public File getSourceMd5() {
        return new File(mSource.getAbsolutePath() + ".md5");
    }

    public File getDestination() {
        return mDestination;
    }
}
