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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Util {

    private static final Logger LOGGER = Logger.getLogger(Util.class.getSimpleName());
    private static final int KIBIBYTE_IN_BYTES = 1024;

    private Util() {}

    /**
     * source: http://stackoverflow.com/questions/106770/standard-concise-way-to-copy-a-file-in-java
     * @param src file
     * @param dst file
     * @throws IOException
     */
    public static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                // Transfer bytes from in to out
                byte[] buf = new byte[KIBIBYTE_IN_BYTES];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    public static String getMd5FromAdjacentFile(File file) {
        String result = "";
        BufferedReader br = null;
        try {
            File adjacentFile = new File(file.getAbsolutePath() + ".md5");
            if (!adjacentFile.exists()) {
                throw new IOException("missing MD5 file - " + adjacentFile.getAbsolutePath());
            }
            br = new BufferedReader(new FileReader(adjacentFile));
            String rawMd5Value = br.readLine();
            // TODO - could be tightened up
            Pattern pattern = Pattern.compile(".*([a-f0-9]{32}).*");
            Matcher matcher = pattern.matcher(rawMd5Value);
            if (matcher.find()) {
                result = matcher.group(1);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "cannot get MD5 value for " + file.getAbsolutePath());
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e){
                LOGGER.log(Level.WARNING, "cannot close file reader for " + file.getAbsolutePath(),
                        e);
            }
        }

        return result;
    }
}
