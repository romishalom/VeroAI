package com.rando.factchecker2;

import android.app.SearchManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.ai.client.generativeai.type.Content;
import com.rando.factchecker2.databinding.FragmentSecondBinding;

import java.util.Objects;

public class MessageFragment extends Fragment {

    private FragmentSecondBinding binding;
    private ImageButton sendBtn;
    private ScrollView messagesScroll;
    private LinearLayout promptBoxLayout;
    private LinearLayout messages;
    private EditText promptBox;
    private View spaceAfterPrompt;
    private ProgressBar progressBar;

    private HomeViewModel homeViewModel;
    private ActionsViewModel actionsViewModel;

    private GeminiClient geminiClient;
    private GeminiClientInterface geminiClientInterface;



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

        //Initialize Gemini Client interface
        geminiClientInterface = new GeminiClientInterface() {
            @Override
            public void onSuccess(String response) {
                if (isAdded()) {
                    String resultText = response.replaceAll("Gemini", "Truthify");
                    Log.d("Response", resultText);
                    promptBoxLayout.removeView(progressBar);
                    promptBoxLayout.addView(sendBtn);
                    promptBoxLayout.addView(spaceAfterPrompt);
                    messages.addView(responseBubble(resultText, false, 5));
                }
            }
            @Override
            public void onFailure(String error) {
                if (isAdded()) {
                    Log.e("ResponseError", error);
                    promptBoxLayout.removeView(progressBar);
                    promptBoxLayout.addView(sendBtn);
                    promptBoxLayout.addView(spaceAfterPrompt);
                    messages.addView(responseBubble(error, true, 5));
                }
            }
        };

        //Initialize Gemini Client
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            geminiClient = new GeminiClient(geminiClientInterface, this.requireActivity().getMainExecutor());
        }

        actionsViewModel = new ViewModelProvider(requireActivity()).get(ActionsViewModel.class);
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        if (homeViewModel.getResponse().getValue() == null) {
            firstMessage(homeViewModel.getPrompt().getValue());
        } else {
            fistResponse(homeViewModel.getResponse().getValue());
        }
        homeViewModel.setPrompt("");



        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!promptBox.getText().toString().isEmpty()) {
                    int initialNum = messages.getChildCount();
                    String prompt = promptBox.getText().toString();
                    if (prompt.startsWith("@Summarize ")) {
                        actionsViewModel.setAction(PromptAction.SUMMARIZE);
                    } else if (prompt.startsWith("@TrueFalse ")) {
                        actionsViewModel.setAction(PromptAction.TRUEFALSE);
                    }
                    messages.addView(messageBubble(prompt));
                    messagesScroll.scrollTo(0, messagesScroll.getBottom()-400);
                    promptBox.setText("");
                    promptBox.clearFocus();
                    promptBoxLayout.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(promptBox.getWindowToken(), 0);
                    progressBar = new ProgressBar(getContext());
                    promptBoxLayout.removeView(spaceAfterPrompt);
                    promptBoxLayout.removeView(sendBtn);
                    promptBoxLayout.addView(progressBar);
                    String link = LinkFinder.findLink(prompt);
                    System.out.println("link: "+link);
                    if (link != null && link.startsWith("https://youtu")) {
                        YoutubeTranscriptTask.TranscriptCallback transcriptCallback = new YoutubeTranscriptTask.TranscriptCallback() {
                            @Override
                            public void onTranscript(String transcript) {
                                System.out.println(transcript);
                                geminiClient.addChatToHistory("user", "Here is the transcript of the video from the given link: " + transcript);
                                geminiClient.sendPromptAndSetResults(prompt);
                                actionsViewModel.setAction(PromptAction.NOTHING);
                            }
                        };
                        // Replace with the YouTube URL
                        YoutubeTranscriptTask task = new YoutubeTranscriptTask(transcriptCallback);
                        task.execute(link);
                    } else {
                        geminiClient.sendPromptAndSetResults(actionString(prompt));
                        actionsViewModel.setAction(PromptAction.NOTHING);
                    }
                }

            }
        });

        promptBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptBoxLayout.requestFocus();
            }
        });


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void firstMessage(String prompt) {
        messages.addView(messageBubble(prompt));
        messagesScroll.fullScroll(View.FOCUS_DOWN);
        promptBox.setText("");
        promptBox.clearFocus();
        promptBoxLayout.clearFocus();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(promptBox.getWindowToken(), 0);
        progressBar = new ProgressBar(getContext());
        promptBoxLayout.removeView(spaceAfterPrompt);
        promptBoxLayout.removeView(sendBtn);
        promptBoxLayout.addView(progressBar);
        geminiClient.sendPromptAndSetResults(actionString(prompt));
        actionsViewModel.setAction(PromptAction.NOTHING);
    }

    public void fistResponse(String response) {
        messages.addView(responseBubble(response, false, 20));
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(promptBox.getWindowToken(), 0);
        promptBox.setText("");
        promptBox.clearFocus();
        promptBoxLayout.clearFocus();
        homeViewModel.setResponse(null);
    }

    public View responseBubble(String text, boolean error, int charsPerTime) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup responseV = (ViewGroup) inflater.inflate(R.layout.response, null);
        TextView textView = responseV.findViewById(R.id.responseText);
        ImageButton copy = responseV.findViewById(R.id.copy);
        ImageButton share = responseV.findViewById(R.id.share);
        ImageButton search = responseV.findViewById(R.id.search);
        ImageButton insert = responseV.findViewById(R.id.insert);
        //Format
        //TextFormatter.formatText(text, textView);
        Typewriter typewriter = new Typewriter(textView);
        typewriter.setCharPerTime(charsPerTime);
        if (error) {
            typewriter.animateText(TextFormatter.formatText(text+"&emsp; <font color=\"#3165d5\"><u>Reopen app</u></font>"));
        } else {
            typewriter.animateText(TextFormatter.formatText(text));
        }
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
                promptBox.setSelection(text.length()-1);
            }
        });

        if (error) {
            textView.setTextColor(getResources().getColor(R.color.red1));
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent toHome = new Intent(getContext(), HomeActivity.class);
                    toHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(toHome);
                }
            });
        }
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

        String finalText = text;
        messageV.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                copyText(getContext(), finalText, "Prompt");
                return false;
            }
        });

        if (actionsViewModel.getAction().getValue() == PromptAction.SUMMARIZE) {
            if (text.startsWith("@Summarize ")) {
                text = text.substring(10);
            }
            textView.setText(Html.fromHtml("<b><tt>@Summarize</tt></b> " + text));
        } else if (actionsViewModel.getAction().getValue() == PromptAction.TRUEFALSE) {
            if (text.startsWith("@TrueFalse ")) {
                text = text.substring(11);
            }
            textView.setText(Html.fromHtml("<b><tt>@TrueFalse</tt></b> " + text));

        } else {
            textView.setText(text);
        }
        return messageV;
    }

    public String actionString(String text) {
        String res = text;
        switch (Objects.requireNonNull(actionsViewModel.getAction().getValue())) {
            case SUMMARIZE:
                res = "Summarize the following statements or subjects: " + text.replaceFirst("@Summarize ", "");
                break;
            case TRUEFALSE:
                res = "Check if the following statement is true or false and explain: " + text.replaceFirst("@TrueFalse ", "");
                break;
        }
        System.out.println(res);
        return res;
    }


}