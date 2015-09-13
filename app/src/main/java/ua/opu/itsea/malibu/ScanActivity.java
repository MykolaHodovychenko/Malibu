package ua.opu.itsea.malibu;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.getbase.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ua.opu.itsea.malibu.mqtt.MQTTClient;
import ua.opu.itsea.malibu.utils.AboutDialog;
import ua.opu.itsea.malibu.utils.Notifications;
import ua.opu.itsea.malibu.utils.RecyclerAdapter;
import ua.opu.itsea.malibu.utils.ScanResult;

public class ScanActivity extends AppCompatActivity implements ConnectionDialog.ConnectionDialogListener, MQTTClient.MQTTClientListener {

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerAdapter mAdapter;

    private String mDiscoveredIp;

    private CoordinatorLayout mLayout;

    private ArrayList<ScanResult> mScansList;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        mLayout = (CoordinatorLayout) findViewById(R.id.layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setSize(FloatingActionButton.SIZE_NORMAL);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(
                            "com.google.zxing.client.android.SCAN");
                    intent.putExtra("SCAN_MODE", "QR_CODE_MODE,PRODUCT_MODE");
                    startActivityForResult(intent, 0);
                } catch (Exception e) {
                    Notifications.showErrorNotification(mLayout,getString(R.string.error_scan_intent));
                    e.printStackTrace();
                }
            }
        });

        if (savedInstanceState == null) {
            new ConnectionDialog().show(getFragmentManager(), "conndlg");
            mScansList = new ArrayList<>();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new RecyclerAdapter(mScansList);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_info:
                new AboutDialog().show(getFragmentManager(), "aboutdlg");
                return true;
            case R.id.action_reconnect:
                new ConnectionDialog().show(getFragmentManager(), "conndlg");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    final View.OnClickListener reconnectListener = new View.OnClickListener() {
        public void onClick(View v) {
            new ConnectionDialog().show(getFragmentManager(), "conndlg");
        }
    };

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0 && intent != null) {
            String data = intent.getStringExtra("SCAN_RESULT");
            switch (resultCode) {
                case RESULT_OK:
                    // Передача сообщения брокеру
                    MQTTClient client = new MQTTClient(mDiscoveredIp);
                    client.sendToBroker(ScanActivity.this, validateQRFormat(data));
                    break;
                case RESULT_CANCELED:
                    Notifications.showErrorNotification(mLayout, getString(R.string.qr_scan_cancelled));
                    break;
                default:
                    Notifications.showErrorNotification(mLayout, getString(R.string.error_qr_scan));
                    break;
            }
        } else {
            Notifications.showErrorNotification(mLayout, getString(R.string.error_qr_scan));
        }
    }

    private String validateQRFormat(String data) {
        String validatedData = data;
        try {
            // Если преобразование прошло успешно -
            // передаем данные на сервер без изменений
            new JSONObject(validatedData);
        } catch (JSONException e) {
            // Если не удалось преобразовать в JSON, то скорее всего
            // идет дело о старой версии QR-кода, где был зашит только ID клиента
            // Создаем новый JSON-объект с ключом "CardId" и с значением ID,
            // которое было считано из QR-кода и отправляем это брокеру
            JSONObject object = new JSONObject();
            try {
                object.put("CardId", Integer.parseInt(validatedData));
                validatedData = object.toString();
            } catch (JSONException | NumberFormatException e1) {
                // Если не удалось распарсить ID на карточке, то
                // это "левый" QR-код, просто передаем на сервер данные "как есть"
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        return validatedData;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
        outState.putString("discovered_ip", mDiscoveredIp);
        outState.putParcelableArrayList("scans", mScansList);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mDiscoveredIp = savedInstanceState.getString("discovered_ip");
        mScansList = savedInstanceState.getParcelableArrayList("scans");
        mAdapter = new RecyclerAdapter(mScansList);
        mAdapter.notifyDataSetChanged();
    }


    private void addCard(String data, BrokerResult result) {
        ScanResult scanResult = new ScanResult(data, result);
        mScansList.add(0, scanResult);
        if (mScansList.size() > 10) {
            mScansList.remove(mScansList.size() - 1);
        }
        mAdapter.notifyDataSetChanged();
    }


    @Override
    public void onFinishConnectionDialog(String discoveredIp) {
        this.mDiscoveredIp = discoveredIp;
    }

    @Override
    public void onSendToBrokerReceivedResult(final BrokerResult result, final String data) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (result == BrokerResult.SUCCESS) {
                    Notifications.showOkNotification(mLayout, getString(R.string.ok_message_sent));
                } else if (result == BrokerResult.UNKNOWN_ERROR) {
                    Notifications.showErrorNotification(mLayout, getString(R.string.error_mqtt_connection_lost));
                } else if (result == BrokerResult.CONNECTION_ERROR) {
                    Notifications.showErrorNotification(mLayout, getString(R.string.error_mqtt_noclient) + mDiscoveredIp);
                }
                addCard(data, result);
            }
        });
    }

    public enum BrokerResult {
        SUCCESS,
        CONNECTION_ERROR,
        UNKNOWN_ERROR
    }

}



