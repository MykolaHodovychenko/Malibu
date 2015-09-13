package ua.opu.itsea.malibu;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.dd.CircularProgressButton;

import ua.opu.itsea.malibu.mqtt.ServerDiscovery;
import ua.opu.itsea.malibu.utils.Callable;

public class ConnectionDialog extends DialogFragment implements View.OnClickListener {

    public interface ConnectionDialogListener {
        void onFinishConnectionDialog(String discoveredIp);
    }

    CircularProgressButton connectButton;
    Handler mHandler;

    String mDiscoveredIp;

    Handler mDiscoveredIpHandler = new Handler() {
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String message = bundle.getString("discovered_ip");

            if (message == null) {
                connectButton.setProgress(-1);
            } else {
                mDiscoveredIp = message;
                connectButton.setProgress(100);
            }
        } // handle message
    }; // handler

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_connection, null);

        mHandler = new Handler();

        getDialog().setCanceledOnTouchOutside(false);
        getDialog().getWindow().setWindowAnimations(R.style.DialogAnimation);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        connectButton = (CircularProgressButton) v.findViewById(R.id.connect);
        connectButton.setOnClickListener(this);
        connectButton.setIndeterminateProgressMode(true);
        connectButton.setProgress(50);
        startDiscoveryServer();

        return v;
    }

    public void onClick(View v) {
        if (connectButton.getProgress() == 0) {
            connectButton.setProgress(50);
            startDiscoveryServer();
        } else if (connectButton.getProgress() == -1) {
            connectButton.setProgress(0);
        } else if (connectButton.getProgress() == 100) {
            ConnectionDialogListener activity = (ConnectionDialogListener) getActivity();
            activity.onFinishConnectionDialog(mDiscoveredIp);
            this.dismiss();
        }
    }

    private void startDiscoveryServer() {
        ServerDiscovery serverDiscovery = new ServerDiscovery(getActivity());
        serverDiscovery.setCallback(new Callable<String>() {
            @Override
            public void call(final String ip) {

                Bundle b = new Bundle();
                b.putString("discovered_ip", ip);
                Message msg = new Message();
                msg.setData(b);
                mDiscoveredIpHandler.sendMessage(msg);
            }
        });
        serverDiscovery.start();
    }

    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }

}
