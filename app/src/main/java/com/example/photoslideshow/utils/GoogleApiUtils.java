package com.example.photoslideshow.utils;

import android.content.Context;
import android.content.Intent;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.OAuth2Credentials;
import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.library.v1.PhotosLibrarySettings;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

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

    public static PhotosLibraryClient initPhotosLibraryClient(@NonNull String token) throws IOException {
        return PhotosLibraryClient.initialize(getPhotosLibrarySettings(token));
    }

    public static PhotosLibrarySettings getPhotosLibrarySettings(@NonNull String token) throws IOException {
        return PhotosLibrarySettings
                .newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(getOAuth2Credentials(token)))
                .build();
    }

    public static Credentials getOAuth2Credentials(@NonNull String token) {
        return OAuth2Credentials.create(new AccessToken(token, null));
    }

    public static void startSignInActivity(@NonNull FragmentActivity activity,
                                           @NonNull GoogleApiClient client,
                                           int requestCode) {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(client);
        activity.startActivityForResult(signInIntent, requestCode);
    }

    public static GoogleSignInAccount getSignInAccountFromResultIntent(@Nullable Intent intent) {
        if (intent == null) return null;

        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(intent);
        return (result.isSuccess() ? result.getSignInAccount() : null);
    }

    public static GoogleSignInAccount getCurrentSignInAccount(@NonNull Context context) {
        return GoogleSignIn.getLastSignedInAccount(context);
    }
}
