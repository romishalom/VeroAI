package com.rando.factchecker2;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

public class ShareHandlerActivity extends AppCompatActivity {

    private String etText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent);
            }
        }
    }

    void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            etText = sharedText;
            goHome();
        }
    }

    private void goHome() {
        Intent intent1 = new Intent(this, HomeActivity.class);
        //intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent1.putExtra("etText", etText);
        startActivity(intent1);

        finish();
    }

}