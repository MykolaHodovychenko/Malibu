package ua.opu.itsea.malibu.mqtt;

import android.app.Activity;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.Charset;

import ua.opu.itsea.malibu.ScanActivity;

/**
 * Malibu, created by NickGodov on 13.09.2015.
 * This software is protected by copyright law and international treaties.
 * Unauthorized reproduction or distribution of this program, or any portion of it, may result in severe
 * civil and criminal penalties, and will be prosecuted to the maximum extent possible under law.
 */
public class MQTTClient {

    String mDiscoveredIp;
    private static final String QUEUE_NAME = "client_check";

    public interface MQTTClientListener {
        void onSendToBrokerReceivedResult(ScanActivity.BrokerResult result, String data);
    }

    public MQTTClient(String mDiscoveredId) {
        this.mDiscoveredIp = mDiscoveredId;
    }

    public void sendToBroker(final Activity activity, final String data) {
        MqttClient client = tryConnect(mDiscoveredIp);
        if (client == null) {
            MQTTClientListener a = (MQTTClientListener) activity;
            a.onSendToBrokerReceivedResult(ScanActivity.BrokerResult.CONNECTION_ERROR, data);
            return;
        }

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {
//                MQTTClientListener a = (MQTTClientListener) activity;
//                a.onSendToBrokerReceivedResult(ScanActivity.BrokerResult.UNKNOWN_ERROR, data);
//                return;
            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                //TODO: Пришло сообщение от брокера
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                MQTTClientListener a = (MQTTClientListener) activity;
                a.onSendToBrokerReceivedResult(ScanActivity.BrokerResult.SUCCESS, data);
                return;
            }
        });

        MqttMessage message = new MqttMessage(data.getBytes(Charset.forName("UTF-8")));
        message.setQos(1);
        try {
            client.publish(QUEUE_NAME, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public MqttClient tryConnect(String ip) {
        MqttClient client = null;
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            client = new MqttClient("tcp:/" + ip, "1", persistence);
            client.setTimeToWait(1000);
            client.connect();
        } catch (MqttException | IllegalArgumentException e) {
            e.printStackTrace();
            //TODO: Ошибка подключения
        }

        return client;
    }
}
