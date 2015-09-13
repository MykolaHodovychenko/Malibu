package ua.opu.itsea.malibu.mqtt;

import android.app.Activity;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import ua.opu.itsea.malibu.utils.Callable;

public class ServerDiscovery extends Thread {

    private static final String TAG = "malibu";
    private static final String REMOTE_KEY = "malibu_hash";
    private static final int DISCOVERY_PORT = 62308;
    private static final int TIMEOUT_MS = 10000;
    private static String challenge = "malibu";
    private WifiManager mWifi;
    private Callable<String> callback;
    Activity mActivity;

    public ServerDiscovery(Activity calledActivity) {
        mWifi = (WifiManager) calledActivity.getSystemService(Context.WIFI_SERVICE);
        this.mActivity = calledActivity;
    }

    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT);
            socket.setBroadcast(true);
            socket.setSoTimeout(TIMEOUT_MS);

            sendDiscoveryRequest(socket);
            String receivedServer = listenForResponses(socket);
            socket.close();

            callback.call(receivedServer);
        } catch (IOException e) {
            e.printStackTrace();
            callback.call(null);

        }
    }

    /**
     * Send a broadcast UDP packet containing a request for boxee services to
     * announce themselves.
     *
     * @throws IOException
     */
    private void sendDiscoveryRequest(DatagramSocket socket) throws IOException {
        String data = "";
        Random rand = new Random();

        data = String.valueOf(rand.nextInt(10));
        challenge = data;

        DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(),
                getBroadcastAddress(), DISCOVERY_PORT);

        socket.send(packet);
    }

    /**
     * Calculate the broadcast IP we need to send the packet along. If we send it
     * to 255.255.255.255, it never gets sent. I guess this has something to do
     * with the mobile network not wanting to do broadcast.
     */
    private InetAddress getBroadcastAddress() throws IOException {
        DhcpInfo dhcp = mWifi.getDhcpInfo();

        if (dhcp == null) {
            // TODO: Ошибка DHCP
            return null;
        }

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);

        return InetAddress.getByAddress(quads);
    }

    /**
     * Listen on socket for responses, timing out after TIMEOUT_MS
     *
     * @param socket socket on which the announcement request was sent
     * @return list of discovered servers, never null
     * @throws IOException
     */
    private String listenForResponses(DatagramSocket socket) throws IOException {
        long start = System.currentTimeMillis();
        byte[] buf = new byte[1024];

        String server = null;

        // Loop and try to receive responses until the timeout elapses. We'll get
        // back the packet we just sent out, which isn't terribly helpful, but we'll
        // discard it in parseResponse because the cmd is wrong.
        try {
            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                String s = new String(packet.getData(), 0, packet.getLength());

                if (checkSignature(s)) {
                    server = packet.getAddress().toString();
                    break;
                }
            }
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            // TODO: тайм-аут сокета
        }
        return server;
    }

    /**
     * Calculate the signature we need to send with the request. It is a string
     * containing the hex md5sum of the challenge and REMOTE_KEY.
     *
     * @return signature string
     */
    private boolean checkSignature(String recieved) {
        Log.i(TAG, "recieverd === " + recieved);

        MessageDigest digest;
        byte[] md5sum = null;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.update(challenge.getBytes());
            digest.update(REMOTE_KEY.getBytes());
            md5sum = digest.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        StringBuffer hexString = new StringBuffer();
        for (int k = 0; k < md5sum.length; ++k) {
            String s = Integer.toHexString((int) md5sum[k] & 0xFF);
            if (s.length() == 1)
                hexString.append('0');
            hexString.append(s);
        }

        Log.i(TAG, "recieved in end === " + recieved);
        Log.i(TAG, "hex in end === " + hexString);

        return recieved.equals(hexString.toString());
    }

    public void setCallback(Callable<String> callback) {
        this.callback = callback;
    }
}



