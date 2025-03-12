package com.rando.factchecker2.home;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ActionsViewModel extends ViewModel {



    private final MutableLiveData<PromptAction> action = new MutableLiveData<>();

    public void setAction(PromptAction action) {
        this.action.setValue(action);
    }

    public MutableLiveData<PromptAction> getAction() {
        return action;
    }


}

