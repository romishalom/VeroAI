package com.rando.factchecker2;

public interface GeminiClientInterface {
    public void onSuccess(String response);
    public void onFailure(String error);
}
