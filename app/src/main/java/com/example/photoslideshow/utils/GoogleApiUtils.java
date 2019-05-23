package com.example.photoslideshow.utils;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;

public class GoogleApiUtils {

    public static final String SCOPE_PHOTO_READONLY = "https://www.googleapis.com/auth/photoslibrary.readonly";

    public static GoogleApiClient initSignInApiClient(@NonNull FragmentActivity activity,
                                                      @NonNull GoogleApiClient.ConnectionCallbacks connectionCallbacks,
                                                      @NonNull GoogleApiClient.OnConnectionFailedListener connectionFailedListener,
                                                      @NonNull String[] scopes) {
        return new GoogleApiClient
                .Builder(activity.getApplicationContext())
                .addConnectionCallbacks(connectionCallbacks)
                .enableAutoManage(activity, connectionFailedListener)
                .addScope(getScope(scopes))
                .addApi(Auth.GOOGLE_SIGN_IN_API, getSignInOptions(scopes))
                .build();
    }

    public static GoogleSignInOptions getSignInOptions(@NonNull String[] scopes) {
        return new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(getScope(scopes))
                .requestEmail()
                .build();
    }

    public static Scope getScope(@NonNull String[] scpStrs) {
        String str = new String();
        for (int i=0; i<scpStrs.length; i++) {
            str += scpStrs[i];
            if (i < scpStrs.length-1) str += " ";
        }
        return new Scope(str);
    }

    public static void startSignInActivity(@NonNull FragmentActivity activity,
                                           @NonNull GoogleApiClient client,
                                           int requestCode) {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(client);
        activity.startActivityForResult(signInIntent, requestCode);
    }

    public static GoogleSignInAccount getSignInAccountFromResultIntent(Intent intent) {
        if (intent == null) return null;

        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(intent);
        return (result.isSuccess() ? result.getSignInAccount() : null);
    }
}
