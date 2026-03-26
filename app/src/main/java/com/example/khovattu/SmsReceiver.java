package com.example.khovattu;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.khovattu.api.ApiClient;
import com.example.khovattu.api.ApiService;
import com.example.khovattu.utils.SessionManager;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SmsReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "sms_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Nhận tin nhắn đến
        SmsMessage[] msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent);

        for (SmsMessage msg : msgs) {
            String body = msg.getMessageBody();
            String from = msg.getOriginatingAddress();

            if (body == null) continue;

            // ✅ Hiển thị thông báo tin nhắn đến
            showNotification(context, "Tin nhắn từ: " + from, body);

            // ✅ (Tuỳ chọn) Lưu vào hộp thư đến nếu là SMS app mặc định
            try {
                if (Telephony.Sms.getDefaultSmsPackage(context).equals(context.getPackageName())) {
                    ContentValues values = new ContentValues();
                    values.put("address", from);
                    values.put("body", body);
                    context.getContentResolver().insert(Uri.parse("content://sms/inbox"), values);
                }
            } catch (Exception ignored) {}

            // ✅ Tự động phản hồi nếu tin nhắn chứa “are you ok”
            if (body.toLowerCase().contains("are you ok")) {
                if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.SEND_SMS)
                        == PackageManager.PERMISSION_GRANTED) {
                    try {
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(from, null,
                                "Yes, I'm OK ✅", null, null);

                        Toast.makeText(context,
                                "📩 Đã phản hồi tới: " + from,
                                Toast.LENGTH_SHORT).show();

                        showNotification(context,
                                "Đã gửi phản hồi", "Yes, I'm OK ✅ tới " + from);

                        // ✅ Gửi cảnh báo về server
                        guiSmsCanhBaoServer(context, from, body);

                    } catch (SecurityException e) {
                        Toast.makeText(context,
                                "Thiếu quyền SEND_SMS", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(context,
                                "Lỗi gửi SMS: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context,
                            "⚠️ App chưa có quyền SEND_SMS, hãy cấp trong Cài đặt.",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    // ✅ Gửi cảnh báo đến server qua Retrofit
    private void guiSmsCanhBaoServer(Context context, String soDienThoai, String noiDung) {
        try {
            ApiService apiService = ApiClient.getApiService(context);
            SessionManager sessionManager = new SessionManager(context);
            String token = sessionManager.getToken();


            Call<Map<String, Object>> call = apiService.guiSmsCanhBao(
                    token,
                    soDienThoai,
                    "Tin nhắn cảnh báo: " + noiDung
            );

            call.enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(context, "✅ Đã gửi cảnh báo đến server", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "⚠️ Gửi cảnh báo thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Toast.makeText(context, "❌ Lỗi kết nối server: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(context, "❌ Lỗi khi gửi cảnh báo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ Hiển thị thông báo notification
    private void showNotification(Context context, String title, String message) {
        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Android 8+ cần channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "SMS Notifications", NotificationManager.IMPORTANCE_HIGH);
            nm.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.sym_action_chat)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        nm.notify((int) System.currentTimeMillis(), builder.build());
    }
}
