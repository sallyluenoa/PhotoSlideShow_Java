package com.example.photoslideshow.task;

import android.accounts.Account;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthUtil;

public class GetAccessTokenAsyncTask extends AsyncTask<Void, Void, String> {

    public interface ICallback {
        public void onAccessTokenResult(String token);
    }

    private static final String TAG = GetAccessTokenAsyncTask.class.getSimpleName();

    private final Context mContext;
    private final String mEmail;
    private final String mScopes;
    private final ICallback mCallback;

    public static void start(Context context, String email, String scope, ICallback callback) {
        start(context, email, new String[]{scope}, callback);
    }

    public static void start(Context context, String email, String[] scopes, ICallback callback) {
        GetAccessTokenAsyncTask task = new GetAccessTokenAsyncTask(context, email, scopes, callback);
        task.execute();
    }

    private GetAccessTokenAsyncTask(Context context, String email, String[] scopes, ICallback callback) {
        super();
        mContext = context;
        mEmail = email;
        String tmp = "oauth2:";
        for (int i=0; i<scopes.length; i++) {
            tmp += scopes[i];
        }
        mScopes = tmp;
        mCallback = callback;
    }

    @Override
    protected String doInBackground(Void... params) {
        Log.d(TAG, "Try to get access token.");
        try {
            return GoogleAuthUtil.getToken(mContext, new Account(mEmail, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE), mScopes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
    }

    @Override
    protected void onPostExecute(String token) {
        Log.d(TAG, "Result of getting access token: " + (token != null ? token : "NULL"));
        if (mCallback != null) {
            mCallback.onAccessTokenResult(token);
        }
    }
}
