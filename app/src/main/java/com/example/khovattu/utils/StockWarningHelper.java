package com.example.khovattu.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.khovattu.R;
import com.example.khovattu.model.SanPham;

public class StockWarningHelper {

    private static final String CHANNEL_ID = "low_stock_channel";

    // ====== KIỂM TRA TỒN KHO ======
    public static void checkLowStock(Context context, SanPham sp) {

        if (sp == null) return;

        int ton = sp.getTonKho() > 0 ? sp.getTonKho() : sp.getSoLuong();

        if (ton < 10) {

            String message =
                    "Sản phẩm: " + sp.getTenHang() +
                            "\nCòn lại: " + ton + " cái" +
                            "\n⚠ Vui lòng nhập thêm để không bị hết hàng!";

            showNotification(context, "⚠ Cảnh báo tồn kho thấp", message);
        }
    }

    // ====== HIỂN THỊ NOTIFICATION ======
    private static void showNotification(Context context, String title, String message) {

        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // ===== TẠO CHANNEL =====
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Cảnh báo tồn kho",
                    NotificationManager.IMPORTANCE_HIGH
            );

            AudioAttributes attrs = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();

            channel.setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    attrs
            );

            channel.enableLights(true);
            channel.enableVibration(true);

            nm.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_warning) // icon của bạn
                        .setContentTitle(title)
                        .setContentText(message)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setColor(ContextCompat.getColor(context, R.color.red_error))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setVibrate(new long[]{300, 300, 300})
                        .setAutoCancel(true);

        nm.notify((int) System.currentTimeMillis(), builder.build());
    }
}
