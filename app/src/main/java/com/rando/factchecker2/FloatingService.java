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
import android.util.DisplayMetrics;
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
    private ImageButton maximizeBtn;
    private EditText promptBox;
    private ImageButton moveBtn;
    private ImageButton closeBtn;
    private ImageButton sendBtn;
    private LinearLayout messages;
    private ScrollView messagesScroll;
    private ImageButton minimizeBtn;
    private int screenMidx;
    private int screenX;
    private ProgressBar progressBar;
    private LinearLayout promptBoxLayout;
    private Space spaceAfterPrompt;

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
        floatView = (ViewGroup) inflater.inflate(R.layout.service_floating, null);
        moveBtn = floatView.findViewById(R.id.moveImgBtn);
        closeBtn = floatView.findViewById(R.id.closeImgBtn);
        sendBtn = floatView.findViewById(R.id.sendBtn);
        maximizeBtn = floatView.findViewById(R.id.fullscreenImgBtn);
        promptBox = floatView.findViewById(R.id.promptBox);
        messages = floatView.findViewById(R.id.messages);
        messagesScroll = floatView.findViewById(R.id.messagesScroll);
        minimizeBtn = floatView.findViewById(R.id.minimizeImgBtn);
        promptBoxLayout = floatView.findViewById(R.id.promptBoxLayout);
        spaceAfterPrompt = floatView.findViewById(R.id.spaceAfterPrompt);


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

        // The button that helps to maximize the app
        maximizeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSelf();

                // The app will maximize again. So the MainActivity
                // class will be called again.
                Intent backToHome = new Intent(FloatingService.this, MainActivity.class);
                backToHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(backToHome);
            }
        });

        floatView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return resetFocus();
            }
        });


        messagesScroll.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return resetFocus();
            }
        });

        promptBox.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                promptBox.setCursorVisible(true);
                WindowManager.LayoutParams floatWindowLayoutParamUpdateFlag = floatWindowLayoutParam;
                floatWindowLayoutParamUpdateFlag.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

                // WindowManager is updated with the Updated Parameters
                windowManager.updateViewLayout(floatView, floatWindowLayoutParamUpdateFlag);
                return false;
            }
        });

        // Another feature of the floating window is, the window is movable.
        // The window can be moved at any position on the screen.
        moveBtn.setOnTouchListener(new View.OnTouchListener() {
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

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAll();
            }
        });
        if (sendBtn != null) {
            sendBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int initialNum = messages.getChildCount();
                    String prompt = promptBox.getText().toString();
                    messages.addView(messageBubble(prompt));
                    messagesScroll.fullScroll(View.FOCUS_DOWN);
                    promptBox.setText("");
                    progressBar = new ProgressBar(FloatingService.this);
                    progressBar.setScrollBarSize(35);
                    promptBoxLayout.removeView(spaceAfterPrompt);
                    promptBoxLayout.removeView(sendBtn);
                    promptBoxLayout.addView(progressBar);
                    sendPromptAndSetResults(prompt);

                }
            });
        }

        minimizeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMinimized();
            }
        });

    }

    public void sendPromptAndSetResults(String prompt) {
        GenerativeModel gm = new GenerativeModel("gemini-pro" /*Don't change!!!*/, "AIzaSyAJblAu9zBleYMGovgNeT2CtoqfolZImT4");
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);
        Content content = new Content.Builder()
                .addText(prompt)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    String resultText = result.getText();
                    System.out.println(resultText);
                    promptBoxLayout.removeView(progressBar);
                    promptBoxLayout.addView(sendBtn);
                    promptBoxLayout.addView(spaceAfterPrompt);
                    messages.addView(responseBubble(resultText, getResources().getColor(R.color.white)));
                }
                @Override
                public void onFailure(Throwable t) {
                    promptBoxLayout.removeView(progressBar);
                    promptBoxLayout.addView(sendBtn);
                    promptBoxLayout.addView(spaceAfterPrompt);
                    messages.addView(responseBubble(t.toString(),Color.RED));
                }

            }, this.getMainExecutor());
        }
    }

    public View responseBubble(String text, int color) {
        LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        ViewGroup responseV = (ViewGroup) inflater.inflate(R.layout.response, null);
        TextView textView = responseV.findViewById(R.id.responseText);
        ImageButton copy = responseV.findViewById(R.id.copy);
        ImageButton share = responseV.findViewById(R.id.share);
        ImageButton search = responseV.findViewById(R.id.search);
        ImageButton insert = responseV.findViewById(R.id.insert);
        //Format
        TextFormatter.formatText(text, textView);
        //textView.setText(text);
        //textView.setTextColor(color);

        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyText(FloatingService.this, text, "Response");
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareText(text);
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchText(text);
            }
        });

        insert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptBox.setText(text);
                promptBox.setSelection(text.length());
            }
        });

        return responseV;
    }

    public static void copyText(Context context, String text, String label) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text); // Label is a user-visible description
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show();
    }

    public void shareText(String text) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");
        Intent shareIntent = Intent.createChooser(sendIntent, "Share response via"); // Create chooser
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        if (sendIntent.resolveActivity(getPackageManager()) != null) {
            getApplicationContext().startActivity(shareIntent);
            stopAll();
        }
    }

    public void searchText(String text) {
        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
        intent.putExtra(SearchManager.QUERY, text);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(intent);
        stopAll();
    }

    public View messageBubble(String text) {
        LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        ViewGroup messageV = (ViewGroup) inflater.inflate(R.layout.message, null);
        TextView textView = messageV.findViewById(R.id.messageTextView);
        textView.setText(text);
        return messageV;
    }

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

                        if (minimizedParamsUpdated.x <= 0 || minimizedParamsUpdated.x >= screenX-100 ) {
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

                        if (minimizedParamsUpdated.x <= 0 || minimizedParamsUpdated.x >= screenX-100 ) {
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

