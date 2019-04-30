package com.example.photoslideshow.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

public class ConfirmDialogFragment extends DialogFragment {

    public interface OnClickListener {
        public void onPositiveClick(int id);
        public void onNegativeClick(int id);
    }

    private static final String TAG = ConfirmDialogFragment.class.getSimpleName();

    private static final String ARGS_ID = "id";
    private static final String ARGS_TITLE = "title";
    private static final String ARGS_MESSAGE = "message";
    private static final String ARGS_POSITIVE = "positive";
    private static final String ARGS_NEGATIVE = "negative";

    private int mId;
    private OnClickListener mListener = null;

    public static ConfirmDialogFragment newInstance(int id, int title, int message, int positive, int negative) {
        ConfirmDialogFragment fragment = new ConfirmDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARGS_ID, id);
        args.putInt(ARGS_TITLE, title);
        args.putInt(ARGS_MESSAGE, message);
        args.putInt(ARGS_POSITIVE, positive);
        args.putInt(ARGS_NEGATIVE, negative);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        mId = args.getInt(ARGS_ID);
        int title = args.getInt(ARGS_TITLE);
        int message = args.getInt(ARGS_MESSAGE);
        int positive = args.getInt(ARGS_POSITIVE);
        int negative = args.getInt(ARGS_NEGATIVE);

        Activity activity = getActivity();
        if (activity instanceof OnClickListener) {
            mListener = (OnClickListener) activity;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mListener != null) {
                    mListener.onPositiveClick(mId);
                }
                dialog.cancel();
            }
        });
        builder.setNegativeButton(negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mListener != null) {
                    mListener.onNegativeClick(mId);
                }
                dialog.cancel();
            }
        });
        return builder.create();
    }
}
