package com.rando.factchecker2;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelStore;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public class FloatingService extends Service  {

    private ViewGroup floatView;
    private int LAYOUT_TYPE;
    private WindowManager.LayoutParams floatWindowLayoutParam;
    private WindowManager windowManager;
    private ImageButton minimizeBtn;
    private ImageButton closeBtn;
    private LinearLayout nextBtn;
    private TextView explanationTextView;
    private int screenMidx;
    private int screenX;
    private ProgressBar progressBar;
    private LinearLayout responseLayout;
    public boolean isRecording;
    private String responseString;

    private GeminiClient floatingGeminiClient;

    private boolean firstSend = true;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("Hello World!!!!!!!!!!!!!!!!!!!!");

        // we need a Context for that display, so WINDOW_SERVICE is used
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // A LayoutInflater instance is created to retrieve the
        // LayoutInflater for the floating_layout xml
        LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);

        // inflate a new view hierarchy from the floating_layout xml
        floatView = (ViewGroup) inflater.inflate(R.layout.service_fl, null);
        minimizeBtn = floatView.findViewById(R.id.minimizeImgBtn);
        closeBtn = floatView.findViewById(R.id.closeImgBtn);
        nextBtn = floatView.findViewById(R.id.nextLL);
        explanationTextView = floatView.findViewById(R.id.explo);
        responseLayout = floatView.findViewById(R.id.responseLL);


        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        screenX = displayMetrics.widthPixels;
        screenMidx = (int) screenX / 2;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_TOAST;
        }

        floatWindowLayoutParam = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                LAYOUT_TYPE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        floatWindowLayoutParam.gravity = Gravity.TOP | Gravity.CLIP_HORIZONTAL;

        // The ViewGroup that inflates the floating_layout.xml is
        // added to the WindowManager with all the parameters
        windowManager.addView(floatView, floatWindowLayoutParam);

        // Another feature of the floating window is, the window is movable.
        // The window can be moved at any position on the screen.
        floatView.setOnTouchListener(new View.OnTouchListener() {
            final WindowManager.LayoutParams floatWindowLayoutUpdateParam = floatWindowLayoutParam;
            double x;
            double y;
            double px;
            double py;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    // When the window will be touched,
                    // the x and y position of that position
                    // will be retrieved
                    case MotionEvent.ACTION_DOWN:
                        x = floatWindowLayoutUpdateParam.x;
                        y = floatWindowLayoutUpdateParam.y;

                        // returns the original raw X
                        // coordinate of this event
                        px = event.getRawX();

                        // returns the original raw Y
                        // coordinate of this event
                        py = event.getRawY();
                        break;
                    // When the window will be dragged around,
                    // it will update the x, y of the Window Layout Parameter
                    case MotionEvent.ACTION_MOVE:
                        floatWindowLayoutUpdateParam.x = (int) ((x + event.getRawX()) - px);
                        floatWindowLayoutUpdateParam.y = (int) ((y + event.getRawY()) - py);

                        // updated parameter is applied to the WindowManager
                        windowManager.updateViewLayout(floatView, floatWindowLayoutUpdateParam);
                        break;
                }
                return false;
            }

        });

        minimizeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMinimized();
            }
        });
        progressBar = new ProgressBar(FloatingService.this);
        responseLayout.addView(progressBar);

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAll();
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toHome = new Intent(FloatingService.this, HomeActivity.class);
                toHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                toHome.putExtra("responseExtra", responseString);
                startActivity(toHome);
                System.out.println("Nexting");
                stopAll();
            }
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            floatingGeminiClient = new GeminiClient(new GeminiClientInterface() {
                @Override
                public void onSuccess(String response) {
                    Log.d("Response",response);
                    responseLayout.removeView(progressBar);
                    explanationTextView.setText(Html.fromHtml(TextFormatter.formatText(response)));
                    responseString = response;                }
                @Override
                public void onFailure(String error) {
                    responseLayout.removeView(progressBar);
                    explanationTextView.setText(error);
                    explanationTextView.setTextColor(getResources().getColor(R.color.red1));
                }
            }, this.getMainExecutor());
        }

        floatingGeminiClient.sendPromptAndSetResults("Write a poem");

    }

    @SuppressLint("ClickableViewAccessibility")
    public void startMinimized() {
        LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        ViewGroup minimizedV = (ViewGroup) inflater.inflate(R.layout.minimized, null);
        WindowManager.LayoutParams minimizedParams = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                LAYOUT_TYPE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        minimizedParams.gravity = Gravity.TOP | Gravity.START;
        windowManager.removeView(floatView);
        windowManager.addView(minimizedV, minimizedParams);
        minimizedV.findViewById(R.id.openImgBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                windowManager.removeView(minimizedV);
                windowManager.addView(floatView, floatWindowLayoutParam);
            }
        });
        //stopRec();
        isRecording = false;
        ImageButton recordBtn = minimizedV.findViewById(R.id.recordBtn);
        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording) {
                    //startRec();
                    recordBtn.setBackground(getResources().getDrawable(R.drawable.start_recording_back));
                    recordBtn.setImageResource(R.drawable.ic_start_rec);
                    isRecording = false;
                    //stopRec();
                    windowManager.removeView(minimizedV);
                    windowManager.addView(floatView, floatWindowLayoutParam);
                    //floatingGeminiClient.sendPromptAndSetResults( checkString + Transcriber.transcribeCurrentFile());
                } else {


                    recordBtn.setBackground(getResources().getDrawable(R.drawable.stop_recording_back));
                    recordBtn.setImageResource(R.drawable.ic_stop_rec);
                    isRecording = true;
                }
            }

        });

        minimizedV.findViewById(R.id.openImgBtn).setOnTouchListener(new View.OnTouchListener() {
            final WindowManager.LayoutParams minimizedParamsUpdated = minimizedParams;
            double x;
            double y;
            double px;
            double py;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    // When the window will be touched,
                    // the x and y position of that position
                    // will be retrieved
                    case MotionEvent.ACTION_DOWN:
                        x = minimizedParamsUpdated.x;
                        y = minimizedParamsUpdated.y;

                        if (minimizedParamsUpdated.x <= 0 || minimizedParamsUpdated.x >= screenX-200 ) {
                            minimizedV.setBackground(getResources().getDrawable(R.drawable.minimied_back_2));
                        } else {
                            minimizedV.setBackground(getResources().getDrawable(R.drawable.minimized_back));
                        }

                        if (minimizedParamsUpdated.x>screenMidx) {
                            minimizedV.setScaleX(-1);
                        } else {
                            minimizedV.setScaleX(1);
                        }
                        // returns the original raw X
                        // coordinate of this event
                        px = event.getRawX();

                        // returns the original raw Y
                        // coordinate of this event
                        py = event.getRawY();
                        break;
                    // When the window will be dragged around,
                    // it will update the x, y of the Window Layout Parameter
                    case MotionEvent.ACTION_MOVE:
                        minimizedParamsUpdated.x = (int) ((x + event.getRawX()) - px);
                        minimizedParamsUpdated.y = (int) ((y + event.getRawY()) - py);

                        if (minimizedParamsUpdated.x <= 0 || minimizedParamsUpdated.x >= screenX-200 ) {
                            minimizedV.setBackground(getResources().getDrawable(R.drawable.minimied_back_2));
                        } else {
                            minimizedV.setBackground(getResources().getDrawable(R.drawable.minimized_back));
                        }

                        if (minimizedParamsUpdated.x>screenMidx) {
                            minimizedV.setScaleX(-1);
                        } else {
                            minimizedV.setScaleX(1);
                        }



                        // updated parameter is applied to the WindowManager
                        windowManager.updateViewLayout(minimizedV, minimizedParamsUpdated);
                        break;
                }
                return false;
            }

        });

        recordBtn.setOnTouchListener(new View.OnTouchListener() {
            final WindowManager.LayoutParams minimizedParamsUpdated = minimizedParams;
            double x;
            double y;
            double px;
            double py;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    // When the window will be touched,
                    // the x and y position of that position
                    // will be retrieved
                    case MotionEvent.ACTION_DOWN:
                        x = minimizedParamsUpdated.x;
                        y = minimizedParamsUpdated.y;

                        if (minimizedParamsUpdated.x <= 0 || minimizedParamsUpdated.x >= screenX-200 ) {
                            minimizedV.setBackground(getResources().getDrawable(R.drawable.minimied_back_2));
                        } else {
                            minimizedV.setBackground(getResources().getDrawable(R.drawable.minimized_back));
                        }

                        if (minimizedParamsUpdated.x>screenMidx) {
                            minimizedV.setScaleX(-1);
                        } else {
                            minimizedV.setScaleX(1);
                        }
                        // returns the original raw X
                        // coordinate of this event
                        px = event.getRawX();

                        // returns the original raw Y
                        // coordinate of this event
                        py = event.getRawY();
                        break;
                    // When the window will be dragged around,
                    // it will update the x, y of the Window Layout Parameter
                    case MotionEvent.ACTION_MOVE:
                        minimizedParamsUpdated.x = (int) ((x + event.getRawX()) - px);
                        minimizedParamsUpdated.y = (int) ((y + event.getRawY()) - py);

                        if (minimizedParamsUpdated.x <= 0 || minimizedParamsUpdated.x >= screenX-200 ) {
                            minimizedV.setBackground(getResources().getDrawable(R.drawable.minimied_back_2));
                        } else {
                            minimizedV.setBackground(getResources().getDrawable(R.drawable.minimized_back));
                        }

                        if (minimizedParamsUpdated.x>screenMidx) {
                            minimizedV.setScaleX(-1);
                        } else {
                            minimizedV.setScaleX(1);
                        }



                        // updated parameter is applied to the WindowManager
                        windowManager.updateViewLayout(minimizedV, minimizedParamsUpdated);
                        break;
                }
                return false;
            }

        });



    }

    public boolean resetFocus() {
        /*if (promptBox.hasFocus()) {
            promptBox.clearFocus();
            promptBox.setFocusable(false);
            //windowManager.updateViewLayout(floatView, floatWindowLayoutParam);
            System.out.println("fffff");
            //promptBox.setFocusable(true);
            //windowManager.removeView(floatView);
            //windowManager.addView(floatView, floatWindowLayoutParam);

            return true;
        } return false;*/ return false;
    }

    // It is called when stopService()
    // method is called in MainActivity
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
        // Window is removed from the screen
        windowManager.removeView(floatView);
    }

    public void stopAll() {
        if (windowManager != null && floatView != null) {
            //windowManager.removeView(floatView);
        }
        stopSelf();
    }

    private static class ViewModelStoreOwner implements androidx.lifecycle.ViewModelStoreOwner {
        private final ViewModelStore viewModelStore = new ViewModelStore();

        @NonNull
        @Override
        public ViewModelStore getViewModelStore() {
            return viewModelStore;
        }

        public static final ViewModelStoreOwner INSTANCE = new ViewModelStoreOwner();
    }
}

