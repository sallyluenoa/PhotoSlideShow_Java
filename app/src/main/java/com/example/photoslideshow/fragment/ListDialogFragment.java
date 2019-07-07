package com.example.photoslideshow.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

public class ListDialogFragment extends DialogFragment {

    public interface OnClickListener {
        public void onItemClick(int id, int which);
    }

    public static final String TAG = ListDialogFragment.class.getSimpleName();

    private static final String ARGS_ID = "id";
    private static final String ARGS_TITLE = "title";
    private static final String ARGS_LIST = "list";
    private static final String ARGS_PARENT_TAG = "parent_tag";

    private int mId;
    private OnClickListener mListener = null;

    public static ListDialogFragment newInstance(int id, int title,
                                                 ArrayList<String> list, String parentTag) {
        ListDialogFragment fragment = new ListDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARGS_ID, id);
        args.putInt(ARGS_TITLE, title);
        args.putStringArrayList(ARGS_LIST, list);
        args.putString(ARGS_PARENT_TAG, parentTag);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        mId = args.getInt(ARGS_ID, 0);
        int title = args.getInt(ARGS_TITLE, 0);
        ArrayList<String> list = args.getStringArrayList(ARGS_LIST);
        String[] array = list.toArray(new String[list.size()]);
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
        builder.setItems(array, (dialog, which) -> {
            Log.d(TAG, "Select item: " + which);
            dialog.cancel();
            if (mListener != null) {
                mListener.onItemClick(mId, which);
            }
        });
        return builder.create();
    }

}
