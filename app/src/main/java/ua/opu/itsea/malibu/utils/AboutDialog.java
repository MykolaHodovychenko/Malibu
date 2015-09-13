package ua.opu.itsea.malibu.utils;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.dd.CircularProgressButton;

import ua.opu.itsea.malibu.R;

public class AboutDialog extends DialogFragment implements View.OnClickListener {

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_about, null);

        getDialog().setCanceledOnTouchOutside(true);
        getDialog().getWindow().setWindowAnimations(R.style.DialogAnimation);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        v.findViewById(R.id.button).setOnClickListener(this);

        return v;
    }

    public void onClick(View v) {
        dismiss();
    }

    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }
}
