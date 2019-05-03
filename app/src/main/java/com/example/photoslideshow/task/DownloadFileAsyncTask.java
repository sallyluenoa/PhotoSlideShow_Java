package com.example.photoslideshow.task;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadFileAsyncTask extends AsyncTask<Void, Void, Boolean> {

    public interface ICallback {
        public void succeedDownloadFile(File filePath);
        public void failedDownloadFile();
    }

    private static final String TAG = DownloadFileAsyncTask.class.getSimpleName();

    private final URL mInputUrl;
    private final File mOutputFilePath;
    final ICallback mCallback;

    public DownloadFileAsyncTask(String inputUrl, String outputFilePath, ICallback callback) throws MalformedURLException {
        mCallback = callback;
        mInputUrl = new URL(inputUrl);
        mOutputFilePath = new File(outputFilePath);
    }

    public DownloadFileAsyncTask(String inputUrl, File outputFile, ICallback callback) throws MalformedURLException {
        mCallback = callback;
        mInputUrl = new URL(inputUrl);
        mOutputFilePath = outputFile;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        Log.d(TAG, "doInBackground");
        HttpURLConnection con = null;

        try {
            con = (HttpURLConnection) mInputUrl.openConnection();
            con.connect();

            final int status = con.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                final InputStream inputStream = con.getInputStream();
                final DataInputStream dataInputStream = new DataInputStream(inputStream);

                final FileOutputStream fileOutputStream = new FileOutputStream(mOutputFilePath);
                final DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);

                final byte[] buffer = new byte[4096];
                int readByte = 0;
                while((readByte = dataInputStream.read(buffer)) != -1) {
                    dataOutputStream.write(buffer, 0, readByte);
                }

                inputStream.close();
                dataInputStream.close();
                fileOutputStream.close();
                dataOutputStream.close();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }

        return false;
    }


    @Override
    protected void onProgressUpdate(Void... values) {
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            Log.d(TAG, "Succeeded!");
            if (mCallback != null) {
                mCallback.succeedDownloadFile(mOutputFilePath);
            }
        } else {
            Log.d(TAG, "Failed.");
            if (mCallback != null) {
                mCallback.failedDownloadFile();
            }
        }
    }
}
