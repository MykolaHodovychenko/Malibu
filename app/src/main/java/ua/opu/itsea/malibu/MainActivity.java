/*
 * MainActivity
 * Version 1.0
 *
 * date: 15.08.2015
 * author: Mykola Hodovychenko
 * email: nick.godov@gmail.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package ua.opu.itsea.malibu;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.dd.CircularProgressButton;

import ua.opu.itsea.malibu.utils.Notifications;

public class MainActivity extends Activity {

    CircularProgressButton circularButton1;
    EditText mLogin;
    EditText mPassword;

    FrameLayout mLayout;
    ImageView mLoginScrenBackground;

    // Список изображений, одно из которых выбирается для фона
    int[] mBackgroundsList =
            {R.drawable.bg_aerobics,
             R.drawable.bg_aqua,
             R.drawable.bg_gym,
             R.drawable.bg_cycle,
             R.drawable.bg_dance};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLayout = (FrameLayout) findViewById(R.id.layout);

        mLogin = (EditText) findViewById(R.id.login);
        mPassword = (EditText) findViewById(R.id.password);

        circularButton1 = (CircularProgressButton) findViewById(R.id.connect);
        circularButton1.setIndeterminateProgressMode(true);
        circularButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (circularButton1.getProgress() == 0) {
                    circularButton1.setProgress(50);
                    new AuthenticationTask().execute();
                } else if (circularButton1.getProgress() == -1) {
                    circularButton1.setProgress(0);
                } else if (circularButton1.getProgress() == 100){
                    Intent subActivity = new Intent(MainActivity.this,
                            ScanActivity.class);
                    startActivity(subActivity);
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        setRandomBackground();
        mLogin.setText("");
        mPassword.setText("");
    }

    /**
     * Метод для случайной установки одного из нескольких фонов
     * Список фонов хранится в mBackgroundsList
     */
    private void setRandomBackground() {
        mLoginScrenBackground = (ImageView) findViewById(R.id.background);
        int random = (int) (Math.random() * 1000) % mBackgroundsList.length;
        mLoginScrenBackground.setImageResource(mBackgroundsList[random]);
    }

    class AuthenticationTask extends AsyncTask<Void, Boolean, Void> {
        protected void onProgressUpdate(Boolean... params) {
            if (params[0]) {
                circularButton1.setProgress(100);
            } else {
                circularButton1.setProgress(-1);
                Notifications.showErrorNotification(mLayout, getString(R.string.error_no_internet_connection));
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (checkInternetConnection()) {
                publishProgress(true);
            } else {
                publishProgress(false);
            }
            return null;
        }
    }

    /**
     * Проверка интернет-соединения
     * @return наличие интернет-соединения
     */
    private boolean checkInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


}
