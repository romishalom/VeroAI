package com.rando.factchecker2.helpers;

import android.os.Build;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class GeminiClient {
    private final String apiKey = "AIzaSyAJblAu9zBleYMGovgNeT2CtoqfolZImT4";
    private final String modelName = "gemini-2.0-flash";
    private GenerativeModel gm;
    private Executor executor;
    private GeminiClientInterface geminiClientInterface;
    private List<Content> chatHistory;
    public GeminiClient(GeminiClientInterface geminiClientInterface, Executor executor) {
        this.gm = new GenerativeModel(modelName, apiKey);
        this.executor = executor;
        this.geminiClientInterface = geminiClientInterface;
        this.chatHistory = new ArrayList<>();
    }

    public void setUserName(String name) {
        Content.Builder contentB = new Content.Builder().addText("Hi, my name is "+ name);
        contentB.setRole("user");
        this.chatHistory.add(contentB.build());
    }

    public void addChatToHistory(String role, String text) {
        Content.Builder contentB = new Content.Builder().addText(text);
        contentB.setRole(role);
        this.chatHistory.add(contentB.build());
    }

    public void sendPromptAndSetResults(String prompt) {

        GenerativeModelFutures model = GenerativeModelFutures.from(this.gm);
        Content.Builder contentB = new Content.Builder().addText(prompt);
        contentB.setRole("user");

        Content content = contentB.build();
        chatHistory.add(content);

        ListenableFuture<GenerateContentResponse> response = model.startChat(chatHistory).sendMessage(content);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    Content.Builder modelContentB = new Content.Builder().addText(result.getText());
                    modelContentB.setRole("model");
                    chatHistory.add(modelContentB.build());
                    GeminiClient.this.geminiClientInterface.onSuccess(result.getText());
                }
                @Override
                public void onFailure(Throwable t) {
                    GeminiClient.this.geminiClientInterface.onFailure(t.getMessage());
                }

            }, this.executor);
        }
    }

    public void sendPromptAndSetResults(String prompt, File file) {

        GenerativeModelFutures model = GenerativeModelFutures.from(this.gm);
        Content content = new Content.Builder()
                .addText(prompt)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    GeminiClient.this.geminiClientInterface.onSuccess(result.getText());
                }
                @Override
                public void onFailure(Throwable t) {
                    GeminiClient.this.geminiClientInterface.onFailure(t.getMessage());
                }

            }, this.executor);
        }
    }



    public void setGeminiClientInterface(GeminiClientInterface geminiClientInterface) {
        this.geminiClientInterface = geminiClientInterface;
    }


}


