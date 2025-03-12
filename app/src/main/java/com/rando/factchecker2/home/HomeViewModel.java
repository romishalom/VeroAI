package com.rando.factchecker2.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {
        private final MutableLiveData<String> prompt = new MutableLiveData<>();
        private final MutableLiveData<String> response = new MutableLiveData<>();
        private final MutableLiveData<String> etText = new MutableLiveData<>();

        public void setPrompt(String p) {
            this.prompt.setValue(p);
        }

        public LiveData<String> getPrompt() {
            return prompt;
        }

        public void setResponse(String r) {
            this.response.setValue(r);
        }

        public LiveData<String> getResponse() {
            return response;
        }

        public void setEtText(String t) {
            this.etText.setValue(t);
        }

        public LiveData<String> getEtText() {
            return etText;
        }


}
