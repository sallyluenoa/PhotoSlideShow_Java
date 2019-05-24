package com.example.photoslideshow.task;

import android.os.AsyncTask;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadFileAsyncTask extends AsyncTask<Void, Void, Boolean> {

    public interface ICallback {
        public void onDownloadFileResult(File file);
    }

    private static final String TAG = DownloadFileAsyncTask.class.getSimpleName();

    private final URL mInputUrl;
    private final File mOutputFile;
    private final ICallback mCallback;

    public static void start(String inputUrl, String outputFilePath, ICallback callback) throws MalformedURLException {
        start(inputUrl, new File(outputFilePath), callback);
    }

    public static void start(String inputUrl, File outputFile, ICallback callback) throws MalformedURLException {
        DownloadFileAsyncTask task = new DownloadFileAsyncTask(inputUrl, outputFile, callback);
        task.execute();
    }

    private DownloadFileAsyncTask(String inputUrl, File outputFile, ICallback callback) throws MalformedURLException {
        super();
        mInputUrl = new URL(inputUrl);
        mOutputFile = outputFile;
        mCallback = callback;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        Log.d(TAG, "doInBackground");

        HttpURLConnection con = null;
        InputStream inputStream = null;
        DataInputStream dataInputStream = null;
        FileOutputStream fileOutputStream = null;
        DataOutputStream dataOutputStream = null;

        try {
            con = (HttpURLConnection) mInputUrl.openConnection();
            con.connect();

            final int status = con.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                inputStream = con.getInputStream();
                dataInputStream = new DataInputStream(inputStream);

                fileOutputStream = new FileOutputStream(mOutputFile);
                dataOutputStream = new DataOutputStream(fileOutputStream);

                final byte[] buffer = new byte[4 * 1024];
                int readByte = 0;
                while((readByte = dataInputStream.read(buffer)) != -1) {
                    dataOutputStream.write(buffer, 0, readByte);
                }
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (con != null) con.disconnect();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (dataInputStream != null) {
                try {
                    dataInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (dataOutputStream != null) {
                try {
                    dataOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (mCallback != null) {
            mCallback.onDownloadFileResult(result ? mOutputFile : null);
        }
    }
}
