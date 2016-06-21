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

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class FileValidator {

    private static final int KIBIBYTE_IN_BYTES = 1024;

    private FileValidator(){}

    private static final char[] HEX_DIGITS =
            { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    public static boolean isMd5Valid(File file, String md5) throws IOException {
        boolean result = false;

        // argument validation
        if (file == null) {
            throw new IllegalArgumentException("Null file passed!");
        }
        if (md5 == null || md5.isEmpty()) {
            throw new IllegalArgumentException("Invalid MD5 value");
        }

        // commence checks
        if (!file.exists()) {
            throw new IOException("File " + file.getName() + " does not exist!");
        }

        try {
            String fileMd5 = generateMd5(file);
            if (fileMd5.equals(md5)) {
                result = true;
            } else {
                Log.e(FileValidator.class.getName(), "Error validating " + file.getName()
                        + ".  Expected MD5: " + md5 + " but calculated was: " + fileMd5);
            }
        } catch (IOException ioe) {
            Log.e(FileValidator.class.getSimpleName(), "Error generating MD5", ioe);
        }

        Log.d(FileValidator.class.getSimpleName(),
                "File Validation was complete for: " + file.getName());
        return result;
    }


    private static String generateMd5(File file) throws IOException {
        String result;

        FileInputStream fileInputStream = null;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");

            Log.d(Util.class.getSimpleName(), "Reading: " + file.getAbsolutePath());
            fileInputStream = new FileInputStream(file);
            byte[] bytes = new byte[KIBIBYTE_IN_BYTES];

            int count;
            while ((count = fileInputStream.read(bytes)) != -1) {
                messageDigest.update(bytes, 0, count);
            }
            result = hex(messageDigest.digest());
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new AssertionError("Problem generating MD5", e);
        } finally {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        }

        return result;
    }

    /**
     * Copyright 2014 Square Inc.
     *
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     *
     * http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     *
     * Returns this byte string encoded in hexadecimal. */
    private static String hex(byte[] bytes) {
        Log.d(Util.class.getSimpleName(), "Hexing length: " + bytes.length);
        char[] result = new char[bytes.length * 2];
        int c = 0;
        for (byte b : bytes) {
            result[c++] = HEX_DIGITS[(b >> 4) & 0xf];
            result[c++] = HEX_DIGITS[b & 0xf];
        }
        return new String(result);
    }
}
