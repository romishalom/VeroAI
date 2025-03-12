package com.rando.factchecker2.floatingService.audio;

import java.io.File;

public interface AudioCnovCallback {
    public void onSuccess(File file);
    public void onError(Exception e);
}
