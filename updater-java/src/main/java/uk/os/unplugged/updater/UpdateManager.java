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

import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

public class UpdateManager {

    public static class Batch {

        private final int mStep;
        private final float mProgress;
        private final int mTotalSteps;

        public Batch(int step, float progress, int totalSteps) {
            mStep = step;
            mProgress = progress;
            mTotalSteps = totalSteps;
        }

        public int getStep() {
            return mStep;
        }

        public float getProgress() {
            return mProgress;
        }

        public int getTotalSteps() {
            return mTotalSteps;
        }
    }

    public static Observable<Float> create(final Task task) {
        return Observable.create(new Observable.OnSubscribe<Float>() {
            @Override
            public void call(final Subscriber<? super Float> subscriber) {
                try {
                    task.execute(new ProgressListener() {
                        @Override
                        public void onProgress(float progress) {
                            if (!subscriber.isUnsubscribed()) {
                                subscriber.onNext(progress);
                            }
                        }
                    });
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onCompleted();
                    }
                } catch (Exception e) {
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onError(e);
                    }
                }
            }
        });
    }

    public static Observable<Batch> create(final Task... tasks) {
        return create(Arrays.asList(tasks));
    }

    public static Observable<Batch> create(final List<Task> tasks) {
        boolean isEmpty = tasks.size() == 0;
        if (isEmpty) {
            return Observable.empty();
        }

        boolean hasTasks = tasks.size() > 0;
        if (!hasTasks) {
            throw new IllegalArgumentException("please supply tasks");
        }

        Observable<Batch> result = null;

        for (int i = 0; i < tasks.size(); i++) {
            final int step = i + 1;
            Observable<Batch> current = create(tasks.get(i)).map(new Func1<Float, Batch>() {
                @Override
                public Batch call(Float progress) {
                    return new Batch(step, progress, tasks.size());
                }
            });

            boolean isExisting = result != null;
            if (isExisting) {
                result = result.concatWith(current);
            } else {
                result = current;
            }
        }

        return result;
    }
}
