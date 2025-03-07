package com.rando.factchecker2;

import android.os.Handler;
import android.text.Html;
import android.widget.ScrollView;
import android.widget.TextView;

public class Typewriter {

    private TextView textView;
    private String text;
    private int charIndex;
    private long delay; // Delay between each character in milliseconds
    private int charPerTime;
    private Handler handler;
    //private ScrollView scrollView;


    public Typewriter(TextView textView) {
        this.textView = textView;
        this.charIndex = 0;
        this.delay = 0; // Default delay
        this.charPerTime = 1;
        this.handler = new Handler();
    }

    public void animateText(String text) {
        this.text = text;
        this.charIndex = 0;
        textView.setText(""); // Clear existing text
        handler.removeCallbacks(characterAdder); // Prevent multiple animations running at once
        handler.postDelayed(characterAdder, delay);
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public void setCharPerTime(int charPerTime) {
        this.charPerTime = charPerTime;
    }

    private Runnable characterAdder = new Runnable() {
        @Override
        public void run() {
            if (charIndex <= text.length()) {
                textView.setText(Html.fromHtml(text.substring(0, charIndex)));
                charIndex+=charPerTime;
                handler.postDelayed(characterAdder, delay);
            }
        }
    };

    public void cancelAnimation() {
        handler.removeCallbacks(characterAdder);
        charIndex = text.length();
        if(text != null) {
            textView.setText(Html.fromHtml(text));
        }
    }

}
