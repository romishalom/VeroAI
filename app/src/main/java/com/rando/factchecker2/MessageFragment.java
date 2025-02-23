package com.rando.factchecker2;

import android.app.SearchManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.rando.factchecker2.databinding.FragmentSecondBinding;

public class MessageFragment extends Fragment {

    private FragmentSecondBinding binding;
    private ImageButton sendBtn;
    private ScrollView messagesScroll;
    private LinearLayout promptBoxLayout;
    private LinearLayout messages;
    private EditText promptBox;
    private View spaceAfterPrompt;
    private ProgressBar progressBar;


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sendBtn = getView().findViewById(R.id.sendBtn);
        messagesScroll = getView().findViewById(R.id.messagesScroll);
        promptBoxLayout = getView().findViewById(R.id.promptBoxLayout);
        messages = getView().findViewById(R.id.messages);
        promptBox = getView().findViewById(R.id.promptBox);
        spaceAfterPrompt = getView().findViewById(R.id.spaceAfterPrompt);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int initialNum = messages.getChildCount();
                String prompt = promptBox.getText().toString();
                messages.addView(messageBubble(prompt));
                messagesScroll.fullScroll(View.FOCUS_DOWN);
                promptBox.setText("");
                progressBar = new ProgressBar(getContext());
                promptBoxLayout.removeView(spaceAfterPrompt);
                promptBoxLayout.removeView(sendBtn);
                promptBoxLayout.addView(progressBar);
                sendPromptAndSetResults(prompt);

            }
        });


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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
                    Log.d("Response",resultText);
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
                    messages.addView(responseBubble(t.toString(), Color.RED));
                }

            }, this.getActivity().getMainExecutor());
        }
    }

    public View responseBubble(String text, int color) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                copyText(getContext(), text, "Response");
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
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (sendIntent.resolveActivity(getContext().getPackageManager()) != null) {
            getContext().startActivity(shareIntent);
        }
    }

    public void searchText(String text) {
        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
        intent.putExtra(SearchManager.QUERY, text);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(intent);
    }

    public View messageBubble(String text) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup messageV = (ViewGroup) inflater.inflate(R.layout.message, null);
        TextView textView = messageV.findViewById(R.id.messageTextView);
        textView.setText(text);
        return messageV;
    }


}