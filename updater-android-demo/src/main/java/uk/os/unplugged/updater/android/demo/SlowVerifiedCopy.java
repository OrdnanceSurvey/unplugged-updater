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

import java.io.File;
import java.io.IOException;

import uk.os.unplugged.updater.ProgressListener;
import uk.os.unplugged.updater.tasks.copy.VerifiedCopy;

public class SlowVerifiedCopy extends VerifiedCopy {

    private static final int DEFAULT_WAIT_IN_MS = 2000;

    @Override
    public void execute(final ProgressListener progressListener) throws IOException {
        super.execute(new ProgressListener() {
            @Override
            public void onProgress(float progress) {
                progressListener.onProgress(progress);
                try {
                    Thread.sleep(DEFAULT_WAIT_IN_MS);
                } catch (InterruptedException ignore) {}
            }
        });
    }

    public SlowVerifiedCopy(File source, File destination) {
        super(source, destination);
    }
}
