package com.rando.factchecker2.floatingService.audio;

public interface TranscriberCallback {
    public void onSuccess(String text);
    public void onFailure(Exception e);
}
