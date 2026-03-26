package com.example.khovattu.utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.khovattu.R;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

public class MyMarkerView extends MarkerView {

    private final TextView tvContent;
    private final LinearLayout layoutMarker;

    public MyMarkerView(Context context) {
        super(context, R.layout.custom_marker);
        tvContent = findViewById(R.id.tvContent);
        layoutMarker = findViewById(R.id.layoutMarker);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null); // cần để bật blur glow
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        int x = (int) e.getX();
        String label;
        int startColor, endColor;

        // 🎨 Chọn màu gradient tùy loại cột
        if (x == 0) {
            label = "Nhập";
            startColor = 0xFF4CAF50; // xanh đậm
            endColor = 0xFF81C784;   // xanh nhạt
        } else if (x == 1) {
            label = "Xuất";
            startColor = 0xFFF44336; // đỏ đậm
            endColor = 0xFFFF8A65;   // đỏ nhạt
        } else {
            label = "Khác";
            startColor = 0xFF607D8B;
            endColor = 0xFFB0BEC5;
        }

        // 🌈 Gradient nền chính
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{startColor, endColor});
        gradient.setCornerRadius(20f);

        // 💡 Glow effect — tạo lớp ánh sáng xung quanh
        GradientDrawable glow = new GradientDrawable();
        glow.setShape(GradientDrawable.RECTANGLE);
        glow.setCornerRadius(22f);
        glow.setColor(startColor);
        glow.setAlpha(90); // độ sáng
        glow.setStroke(10, startColor);
        glow.setGradientRadius(100f);
        glow.setGradientType(GradientDrawable.RADIAL_GRADIENT);

        // Gộp 2 lớp lại (glow + nền)
        LayerDrawable layerDrawable = new LayerDrawable(new GradientDrawable[]{glow, gradient});
        layerDrawable.setLayerInset(1, 4, 4, 4, 4);
        layoutMarker.setBackground(layerDrawable);

        // 💬 Cập nhật text
        tvContent.setText(label + ": " + (int) e.getY() + " sản phẩm");

        // 🔄 Reset trạng thái ban đầu
        layoutMarker.setScaleX(0.6f);
        layoutMarker.setScaleY(0.6f);
        layoutMarker.setAlpha(0f);

        // ✨ Fade + bounce animation
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(layoutMarker, "alpha", 0f, 1f);
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 0.6f, 1.1f, 1f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 0.6f, 1.1f, 1f);
        ObjectAnimator bounceAnim = ObjectAnimator.ofPropertyValuesHolder(layoutMarker, scaleX, scaleY);
        bounceAnim.setInterpolator(new BounceInterpolator());

        // 💥 Glow sáng nhẹ (fade out dần)
        ObjectAnimator glowFade = ObjectAnimator.ofFloat(layoutMarker, "translationZ", 0f, 12f, 0f);
        glowFade.setDuration(1000);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(fadeIn, bounceAnim, glowFade);
        set.setDuration(650);
        set.start();

        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        // 🔹 Hiển thị tooltip ở giữa và phía trên cột
        return new MPPointF(-(getWidth() / 2f), -getHeight() - 10);
    }
}
