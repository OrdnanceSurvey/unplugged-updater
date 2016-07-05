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

/**
 * This package contains the {@link uk.os.unplugged.updater.android.tasks.usbfind.FindFilesTask}
 * that can be used to search for files.
 *
 * Outside the scope of that task and this package is task chaining.  It is, however, anticipated
 * that found files may be used as input to other tasks.  For example:
 *
 * <pre>
 *     {@code
 *
 *      ...
 *      FindFilesTask findFilesTask = new FindFilesTask(new String[]{"search.db",
 *                                                                          "map.db"});
 *      Task searchTask = new SubsequentVerifiedCopy(
 *                  new FilePromise("search.db", findFilesTask), demoFile1.getDestination());
 *      Task mapTask = new SubsequentVerifiedCopy(
 *                  new FilePromise("map.db", findFilesTask), demoFile2.getDestination());
 *
 *      List<Task> tasks = new ArrayList<>();
 *      tasks.add(findFilesTask);
 *      tasks.add(searchTask);
 *      tasks.add(mapTask);
 *      return tasks;
 *      ...
 *
 *     public class FilePromise {
 *          private final String mFilename;
 *          private final FindFilesTask mFindFilesTask;
 *
 *          public FilePromise(String filename, FindFilesTask findFilesTask) {
 *              mFilename = filename;
 *              mFindFilesTask = findFilesTask;
 *          }
 *
 *          public File getFile() {
 *              return mFindFilesTask.getResults().get(mFilename);
 *          }
 *    }
 *
 *    public class SubsequentVerifiedCopy implements Task {
 *          private final FilePromise mSourcePromise;
 *          private final File mDestination;
 *
 *          public SubsequentVerifiedCopy(FilePromise sourcePromise, File destination) {
 *              mSourcePromise = sourcePromise;
 *              mDestination = destination;
 *          }
 *
 *          @Override
 *          public void execute(ProgressListener progressListener) throws Exception {
 *              File source = mSourcePromise.getFile();
 *
 *              boolean hasSource = source != null;
 *              if (hasSource) {
 *                  VerifiedCopy verifiedCopy = new VerifiedCopy(source, mDestination);
 *                  verifiedCopy.execute(progressListener);
 *              } else {
 *                  progressListener.onProgress(1);
 *              }
 *          }
 *    }
 *    }
 * </pre>
 *
 */

package uk.os.unplugged.updater.android.tasks.usbfind;


