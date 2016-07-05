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

package uk.os.unplugged.updater;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import uk.os.unplugged.updater.tasks.copy.VerifiedCopy;

public class VerifiedCopyTest {

    @Rule
    public TemporaryFolder folder= new TemporaryFolder();

    @Test
    public void testsomething() throws Exception {
        File source = folder.newFile("source.txt");
        writeString(source, "Hello World");

        // the verifier will assume this file is there or else blow up
        File sourceMd5 = folder.newFile("source.txt.md5");
        writeString(sourceMd5, "MD5 (source.txt) = e59ff97941044f85df5297e1c302d260");

        File destination = folder.newFile("destination.txt");

        VerifiedCopy verifiedCopy = new VerifiedCopy(source, destination);
        verifiedCopy.execute(new ProgressListener() {
            @Override
            public void onProgress(float progress) {
                System.out.println("progress: " + progress);
            }
        });
    }

    private void writeString(File file, String s) throws IOException {
        FileWriter writer = new FileWriter(file, false);

        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        bufferedWriter.write(s);
        bufferedWriter.newLine();
        bufferedWriter.close();
    }
}
