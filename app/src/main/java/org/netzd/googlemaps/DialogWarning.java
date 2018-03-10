package org.netzd.googlemaps;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Alumno12 on 10/03/18.
 */

public class DialogWarning extends DialogFragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_MESSAGE = "message";
    private static final String ARG_TYPE = "type";
    private static final String ARG_CANCELABLE = "cancelable";
    private static final String ARG_TITLE_BUTTTON_LEFT = "titleButtonLeft";
    private static final String ARG_TITLE_BUTTTON_RIGHT = "titleButtonRight";

    private TextView messageDialogTextView = null;

    private OnDialogWarningListener onDialogWarningListener = null;

    public static DialogWarning newInstance(String title, String message) {
        DialogWarning dialogo = new DialogWarning();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
        dialogo.setArguments(args);
        return dialogo;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(getArguments().getBoolean(ARG_CANCELABLE));
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString(ARG_TITLE);
        String message = getArguments().getString(ARG_MESSAGE);
        boolean isCancelable = getArguments().getBoolean(ARG_CANCELABLE);
        String titleLeftButton = getArguments().getString(ARG_TITLE_BUTTTON_LEFT);
        String titleRightButton = getArguments().getString(ARG_TITLE_BUTTTON_RIGHT);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.dialog_warning, null);

        dialogBuilder.setTitle(title);
        dialogBuilder.setView(dialogView);

        if (onDialogWarningListener != null) {
            dialogBuilder.setPositiveButton(titleRightButton, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    onDialogWarningListener.onAccept(getDialog());
                }
            });

            dialogBuilder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    onDialogWarningListener.onCancel(getDialog());
                }
            });
        }

        setCancelable(true);
        return dialogBuilder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (onDialogWarningListener != null)
            onDialogWarningListener.onCancel(getDialog());
    }

    public void setOnDialogWarningListener(OnDialogWarningListener onDialogWarningListener) {
        this.onDialogWarningListener = onDialogWarningListener;
    }
}


