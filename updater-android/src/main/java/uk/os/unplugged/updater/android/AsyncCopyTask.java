package uk.os.unplugged.updater.android;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;

/**
 * AsyncTask to wrap the UpdateManager class and keep user notified of progress.
 */
public class AsyncCopyTask extends AsyncTask<Object, String, String>
{
    private String TAG = "AsyncCopyTask";

    private Activity activity;
    private CopyTaskCompletionListener listener;

    private ProgressDialog progress;
    private File gazetteer;
    private File map;

    private boolean succeeded;
    private String error;

    private UpdateManager updateManager;

    public AsyncCopyTask(Activity activity, CopyTaskCompletionListener listener, File gazetteer, File map) {
        this.listener = listener;
        this.activity = activity;
        this.gazetteer = gazetteer;
        this.map = map;
    }

    @Override
    protected String doInBackground(Object... objects) {
        try {
            updateManager.update();
            succeeded = true;
            return "Files imported.";
        } catch (Exception e) {
            succeeded = false;
            Log.e(TAG, e.toString());
            error = e.getMessage();
            return "An error occurred: " + e.getMessage();
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        updateManager = new UpdateManager.Builder()
                .setGazetteerDestination(gazetteer)
                .setMapDestination(map)
                .setProvider(new ProviderImpl(activity.getApplicationContext()))
                .build();

        progress = ProgressDialog.show(
                activity,
                "Importing data...",
                "Please wait...",
                false);

        succeeded = false;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        Log.d(TAG, "Update: " + values[0]);
        progress.setMessage(values[0]);
    }

    @Override
    protected void onPostExecute(String o) {
        super.onPostExecute(o);
        progress.dismiss();
        listener.copyTaskCompleted(succeeded, error);
    }

}
