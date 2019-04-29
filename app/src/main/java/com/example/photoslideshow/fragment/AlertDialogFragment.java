package com.example.photoslideshow.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

public class AlertDialogFragment extends DialogFragment {

    public interface OnClickListener {
        public void onClick(int id);
    }

    private static final String TAG = AlertDialogFragment.class.getSimpleName();

    private static final String ARGS_ID = "id";
    private static final String ARGS_TITLE = "title";
    private static final String ARGS_MESSAGE = "message";
    private static final String ARGS_BUTTON = "button";

    private int mId;
    private OnClickListener mListener = null;

    public static AlertDialogFragment newInstance(int id, String title, String message, String button) {
        AlertDialogFragment fragment = new AlertDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARGS_ID, id);
        args.putString(ARGS_TITLE, title);
        args.putString(ARGS_MESSAGE, message);
        args.putString(ARGS_BUTTON, button);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        mId = args.getInt(ARGS_ID);
        String title = args.getString(ARGS_TITLE);
        String message = args.getString(ARGS_MESSAGE);
        String button = args.getString(ARGS_BUTTON);

        Activity activity = getActivity();
        if (activity instanceof OnClickListener) {
            mListener = (OnClickListener) activity;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mListener != null) {
                    mListener.onClick(mId);
                }
                dialog.cancel();
            }
        });
        return builder.create();
    }
}
