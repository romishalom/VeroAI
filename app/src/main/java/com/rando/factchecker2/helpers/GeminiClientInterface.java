package com.rando.factchecker2.helpers;

public interface GeminiClientInterface {
    public void onSuccess(String response);
    public void onFailure(String error);
}
