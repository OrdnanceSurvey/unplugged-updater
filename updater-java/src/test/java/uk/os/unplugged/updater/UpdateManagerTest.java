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

import org.junit.Test;

import java.util.List;

import rx.observables.BlockingObservable;

import static org.junit.Assert.assertEquals;

public class UpdateManagerTest {

    @Test
    public void testEmissionsFromTask() throws Exception {
        BlockingObservable<Float> o = UpdateManager.create(new Task() {
            @Override
            public void execute(ProgressListener progressListener) throws Exception {
                progressListener.onProgress(0.1F);
                progressListener.onProgress(1F);
            }
        }).toBlocking();

        float expected = 0.1F;
        assertEquals(expected, o.first(), 0.0001F);

        expected = 1F;
        assertEquals(expected, o.last(), 0.0001F);
    }

    @Test
    public void testEmissionsFromTasks() throws Exception {

        // each step represents a task
        final float step1_progress1 = 0.1F;
        final float step1_progress2 = 0.6F;
        final float step1_progress3 = 1F;
        final float step2_progress1 = 0.3F;
        final float step2_progress2 = 0.7F;
        final float step2_progress3 = 0.9F;

        Task[] tasks = new Task[]{new Task() {
            @Override
            public void execute(ProgressListener progressListener) throws Exception {
                progressListener.onProgress(step1_progress1);
                progressListener.onProgress(step1_progress2);
                progressListener.onProgress(step1_progress3);
            }
        }, new Task() {
            @Override
            public void execute(ProgressListener progressListener) throws Exception {
                progressListener.onProgress(step2_progress1);
                progressListener.onProgress(step2_progress2);
                progressListener.onProgress(step2_progress3);
            }
        }};

        List<UpdateManager.Batch> results = UpdateManager
                .create(tasks)
                .buffer(6)
                .toBlocking()
                .first();

        int expectedTotal = 6;
        int actualTotal = results.size();
        assertEquals(expectedTotal, actualTotal);

        int firstStep = 1;
        int secondStep = 2;
        int totalSteps = tasks.length;

        checkBatch(firstStep, totalSteps, step1_progress1, results.get(0));
        checkBatch(firstStep, totalSteps, step1_progress2, results.get(1));
        checkBatch(firstStep, totalSteps, step1_progress3, results.get(2));
        checkBatch(secondStep, totalSteps, step2_progress1, results.get(3));
        checkBatch(secondStep, totalSteps, step2_progress2, results.get(4));
        checkBatch(secondStep, totalSteps, step2_progress3, results.get(5));
    }

    private void checkBatch(int expectedStep, int expectedTotal, float expectedProgress,
                            UpdateManager.Batch batch) {
        assertEquals(expectedProgress, batch.getProgress(), 0.0001F);
        assertEquals(expectedStep, batch.getStep());
        assertEquals(expectedTotal, batch.getTotalSteps());
    }
}
