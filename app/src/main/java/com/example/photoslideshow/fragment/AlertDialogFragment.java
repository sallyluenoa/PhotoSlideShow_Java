package com.example.photoslideshow.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

public class AlertDialogFragment extends DialogFragment {

    public interface OnClickListener {
        public void onClick(int id);
    }

    public static final String TAG = AlertDialogFragment.class.getSimpleName();

    private static final String ARGS_ID = "id";
    private static final String ARGS_TITLE = "title";
    private static final String ARGS_MESSAGE = "message";
    private static final String ARGS_BUTTON = "button";
    private static final String ARGS_PARENT_TAG = "parent_tag";

    private int mId;
    private OnClickListener mListener = null;

    public static AlertDialogFragment newInstance(int id, int title, int message,
                                                  int button, String parentTag) {
        AlertDialogFragment fragment = new AlertDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARGS_ID, id);
        args.putInt(ARGS_TITLE, title);
        args.putInt(ARGS_MESSAGE, message);
        args.putInt(ARGS_BUTTON, button);
        args.putString(ARGS_PARENT_TAG, parentTag);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        mId = args.getInt(ARGS_ID, 0);
        int title = args.getInt(ARGS_TITLE, 0);
        int message = args.getInt(ARGS_MESSAGE, 0);
        int button = args.getInt(ARGS_BUTTON, 0);
        String parentTag = args.getString(ARGS_PARENT_TAG, "");

        if (parentTag.contains("Activity")) {
            Activity activity = getActivity();
            if (activity instanceof OnClickListener) {
                mListener = (OnClickListener) activity;
            }
        } else if (parentTag.contains("Fragment")) {
            Fragment fragment = getParentFragment();
            if (fragment instanceof OnClickListener) {
                mListener = (OnClickListener) fragment;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(button, (dialog, which) -> {
            dialog.cancel();
            if (mListener != null) {
                mListener.onClick(mId);
            }
        });
        return builder.create();
    }
}
