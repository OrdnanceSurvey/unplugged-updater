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

import android.content.Context;
import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public final class TempFileProvider {

    private static FileRef mFile1 = null;
    private static FileRef mFile2 = null;

    private TempFileProvider() {}

    public static void createBrokenDemoSourceFiles(Context context) throws IOException {
        FileRef demoFile1 = getDemoFile1(context);
        writeString(demoFile1.getSource(), "Hello World");
        writeString(demoFile1.getSourceMd5(),
                String.format("MD5 (%s) = e59ff97941044f85df5297e1c302d260",
                        demoFile1.getSource().getName()));

        FileRef demoFile2 = getDemoFile2(context);
        writeString(demoFile2.getSource(), "bad times - corresponding md5 is invalid");
        writeString(demoFile2.getSourceMd5(),
                String.format("MD5 (%s) = 8becf5c1935013c4adf85dccb28ad3f4",
                        demoFile2.getSource().getName()));
    }

    public static void createDemoSourceFiles(Context context) throws IOException {
        FileRef demoFile1 = getDemoFile1(context);
        writeString(demoFile1.getSource(), "Hello World");
        writeString(demoFile1.getSourceMd5(),
                String.format("MD5 (%s) = e59ff97941044f85df5297e1c302d260",
                        demoFile1.getSource().getName()));

        FileRef demoFile2 = getDemoFile2(context);
        writeString(demoFile2.getSource(), "Los pingüinos están en el wáter");
        writeString(demoFile2.getSourceMd5(),
                String.format("MD5 (%s) = 4a971fca25a1c69eec99d82ced990bb1",
                        demoFile2.getSource().getName()));
    }

    public static FileRef getDemoFile1(Context context) {
        if (mFile1 == null) {
            mFile1 = getDemoFile(context, "file1.source", "file1.destination");
        }
        return mFile1;
    }

    public static FileRef getDemoFile2(Context context) {
        if (mFile2 == null) {
            mFile2 = getDemoFile(context, "file2.source", "file2.destination");
        }
        return mFile2;
    }

    private static FileRef getDemoFile(Context context, String sourceName, String destinationName) {
        File source = new File(Environment.getExternalStorageDirectory(), sourceName); //"file1.source"
        File destination = new File(context.getExternalFilesDir(null) + File.separator + destinationName); //"file1.destination"

        return new FileRef(source, destination);
    }

    private static void writeString(File file, String s) throws IOException {
        FileWriter writer = new FileWriter(file, false);

        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        bufferedWriter.write(s);
        bufferedWriter.newLine();
        bufferedWriter.close();
    }

    public static boolean deleteDemoSourceFiles(Context context) {
        FileRef demoFile1 = getDemoFile1(context);
        FileRef demoFile2 = getDemoFile2(context);

        boolean[] successes = new boolean[] {
                !demoFile1.getSource().exists() || demoFile1.getSource().delete(),
                !demoFile1.getSourceMd5().exists() || demoFile1.getSourceMd5().delete(),
                !demoFile1.getDestination().exists() || demoFile1.getDestination().delete(),
                !demoFile2.getSource().exists() || demoFile2.getSource().delete(),
                !demoFile2.getSourceMd5().exists() || demoFile2.getSourceMd5().delete(),
                !demoFile2.getDestination().exists() || demoFile2.getDestination().delete(),
        };

        for (int i = 0; i < successes.length; i++) {
            if (!successes[i]) {
                return false;
            }
        }
        return true;
    }
}
