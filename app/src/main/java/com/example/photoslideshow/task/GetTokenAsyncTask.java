package com.example.photoslideshow.task;

import android.accounts.Account;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthUtil;

public class GetTokenAsyncTask extends AsyncTask<Void, Void, String> {

    public interface ICallback {
        public void succeedAccessToken(String token);
        public void failedAccessToken();
    }

    private static final String TAG = GetTokenAsyncTask.class.getSimpleName();

    final Context mContext;
    final String mEmail;
    final String mScopes;
    final ICallback mCallback;

    public GetTokenAsyncTask(Context context, String email, String[] scopes, ICallback callback) {
        super();
        mCallback = callback;
        mContext = context;
        mEmail = email;
        String tmp = "oauth2:";
        for (int i=0; i<scopes.length; i++) {
            tmp += scopes[i];
        }
        mScopes = tmp;
    }

    @Override
    protected String doInBackground(Void... params) {
        String token = null;
        try {
            token = GoogleAuthUtil.getToken(mContext,
                    new Account(mEmail, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE), mScopes);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return token;
        }
    }

    @Override
    protected void onProgressUpdate(Void... values) {
    }

    @Override
    protected void onPostExecute(String token) {
        if (token != null) {
            Log.d(TAG, "Succeeded getting token: " + token);
            if (mCallback != null) {
                mCallback.succeedAccessToken(token);
            }
        } else {
            Log.d(TAG, "Failed getting token.");
            if (mCallback != null) {
                mCallback.failedAccessToken();
            }
        }
    }
}
