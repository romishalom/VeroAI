package com.rando.factchecker2.floatingService.audio;

public interface ICallback {
    public void onSuccess(String text);
    public void onFailure(Exception e);
}
