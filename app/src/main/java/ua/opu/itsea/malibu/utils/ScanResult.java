package ua.opu.itsea.malibu.utils;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import ua.opu.itsea.malibu.R;
import ua.opu.itsea.malibu.ScanActivity;

/**
 * Malibu, created by NickGodov on 12.09.2015.
 * This software is protected by copyright law and international treaties.
 * Unauthorized reproduction or distribution of this program, or any portion of it, may result in severe
 * civil and criminal penalties, and will be prosecuted to the maximum extent possible under law.
 */
public class ScanResult implements Parcelable{

    private final String DATE_FORMAT = "dd MMMM";
    private final String TIME_FORMAT = "HH:mm";

    private Long timestamp;
    private String mContents;
    private int mImageResource;


    public ScanResult(String data, ScanActivity.BrokerResult result) {
        this.timestamp = System.currentTimeMillis();

        mContents = buildContents(data);

        if (result == ScanActivity.BrokerResult.SUCCESS) {
            mImageResource = R.drawable.scan_card_ok;
        } else if (result == ScanActivity.BrokerResult.CONNECTION_ERROR) {
            mImageResource = R.drawable.scan_card_no_connection;
        } else {
            mImageResource = R.drawable.scan_card_error;
        }
    }

    // region Геттеры

    public int getmImageResource() {
        return this.mImageResource;
    }

    public String getContents() {
        return this.mContents;
    }
    // endregion

    private String buildContents(String data) {
        StringBuilder builder = new StringBuilder();
        JSONObject object;
        try {
            object = new JSONObject(data);

            // Fullname
            if (!object.isNull("Fullname")) {
                builder.append("Имя клиента: ");
                builder.append(object.getString("Fullname"));
                builder.append("\n");
            }

            // ClientID
            if (!object.isNull("ClientId")) {
                builder.append("ID клиента: ");
                builder.append(object.getString("ClientId"));
                builder.append("\n");
            }

            // AbonementID
            if (!object.isNull("AbonementId")) {
                builder.append("ID абонемента: ");
                builder.append(object.getString("AbonementId"));
                builder.append("\n");
            }

            // SubscriptionID
            if (!object.isNull("SubscriptionId")) {
                builder.append("ID подписки: ");
                builder.append(object.getString("SubscriptionId"));
            }

        } catch (JSONException e) {
            // Если не удалось преобразовать в JSON, то скорее всего
            // идет дело о старой версии QR-кода, где был зашит только ID клиента
            // Парсим ID клиента и добавляем в билдер для карточки
            // Это может быть и "левый" QR-кол, в любом случае - выводим на карточку
            builder.append("Информация QR-кода: ");
            builder.append(data);
        }

        return builder.toString();
    }

    private String validateQRFormat(String data) {
        String validatedData = data;
        try {
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


    public String getTime() {
        SimpleDateFormat formatter = new SimpleDateFormat(TIME_FORMAT);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        return formatter.format(calendar.getTime());
    }

    public String getDate() {
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT, new Locale("ru", "RU"));

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        return formatter.format(calendar.getTime());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(timestamp);
        dest.writeString(mContents);
        dest.writeInt(mImageResource);
    }

    public static final Parcelable.Creator<ScanResult> CREATOR
            = new Parcelable.Creator<ScanResult>() {
        public ScanResult createFromParcel(Parcel in) {
            return new ScanResult(in);
        }

        public ScanResult[] newArray(int size) {
            return new ScanResult[size];
        }
    };

    /** recreate object from parcel */
    private ScanResult(Parcel in) {
        timestamp = in.readLong();
        mContents = in.readString();
        mImageResource = in.readInt();
    }
}
