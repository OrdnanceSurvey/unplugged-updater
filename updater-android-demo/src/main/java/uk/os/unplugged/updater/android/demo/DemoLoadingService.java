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

import java.util.ArrayList;
import java.util.List;

import uk.os.unplugged.updater.Task;
import uk.os.unplugged.updater.android.notification.DefaultNotificationProvider;
import uk.os.unplugged.updater.android.LoadingService;
import uk.os.unplugged.updater.android.NotificationProvider;
import uk.os.unplugged.updater.android.demo.files.FileRef;
import uk.os.unplugged.updater.android.demo.files.TempFileProvider;
import uk.os.unplugged.updater.tasks.copy.VerifiedCopy;

public class DemoLoadingService extends LoadingService {

    @Override
    protected NotificationProvider getNotificationProvider() {
        return new DefaultNotificationProvider(this);
    }

    @Override
    protected int getNotificationId() {
        return AppConfig.LOADING_NOTIFICATION_ID;
    }

    @Override
    protected List<Task> getTasks() {
        FileRef demoFile1 = TempFileProvider.getDemoFile1(this);
        FileRef demoFile2 = TempFileProvider.getDemoFile2(this);

        VerifiedCopy verifiedFile1Copy = new SlowVerifiedCopy(demoFile1.getSource(),
                demoFile1.getDestination());
        VerifiedCopy verifiedFile2Copy = new SlowVerifiedCopy(demoFile2.getSource(),
                demoFile2.getDestination());


        List<Task> tasks = new ArrayList<>();
        tasks.add(verifiedFile1Copy);
        tasks.add(verifiedFile2Copy);
        return tasks;
    }
}
