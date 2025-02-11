package com.rando.factchecker2;

import android.content.Context;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatTextView; // Or just TextView if not using AppCompat

public class GradientTextView extends AppCompatTextView {

    public GradientTextView(Context context) {
        super(context);
    }

    public GradientTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GradientTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (w > 0 && h > 0) {
            int[] colors = new int[]{
                    getResources().getColor(R.color.grad1),
                    getResources().getColor(R.color.grad2), // Add middle color
                    getResources().getColor(R.color.grad3)
            };

            // Optional: Add color positions if you want more control
            float[] positions = null; // Even distribution by default
            // Example positions (0.0 = start, 1.0 = end):
            // float[] positions = new float[] { 0.0f, 0.5f, 1.0f }; // Middle color at 50%

            LinearGradient shader = new LinearGradient(
                    0, 0, w, 0,
                    colors,
                    positions, // Use positions if defined, null otherwise
                    Shader.TileMode.CLAMP);

            getPaint().setShader(shader);
        }
    }
}
