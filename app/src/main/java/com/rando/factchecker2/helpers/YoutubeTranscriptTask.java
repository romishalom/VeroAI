package com.rando.factchecker2.helpers;

import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

public class YoutubeTranscriptTask extends AsyncTask<String, Void, String> {

    private static final String TAG = "YoutubeTranscriptTask";
    private final TranscriptCallback callback;

    public YoutubeTranscriptTask(TranscriptCallback callback) {
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... params) {
        String youtubeUrl = params[0];
        String videoId = extractVideoId(youtubeUrl);
        if (videoId == null) {
            return "Invalid YouTube URL";
        }

        return getTranscript(videoId);
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        callback.onTranscript(result);
    }

    private String extractVideoId(String youtubeUrl) {
        String videoId = null;
        String pattern = "(?<=watch\\?v=|/videos/|embed/|youtu.be/|/v/|/e/|/shorts/)[^&#?\\n]+";
        java.util.regex.Pattern compiledPattern = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher matcher = compiledPattern.matcher(youtubeUrl);
        if (matcher.find()) {
            videoId = matcher.group();
        }
        return videoId;
    }

    private String getTranscript(String videoId) {
        try {
            String url = "https://www.youtube.com/watch?v=" + videoId;
            Document doc = Jsoup.connect(url).get();
            Elements scriptElements = doc.select("script");
            for (org.jsoup.nodes.Element script : scriptElements) {
                String scriptData = script.data();
                if (scriptData.contains("captions\":")) {
                    int captionsStart = scriptData.indexOf("captions\":") + "captions\":".length();
                    int captionsEnd = scriptData.indexOf("}]}", captionsStart) + 3;
                    String captionsJson = scriptData.substring(captionsStart, captionsEnd);

                    if (captionsJson.contains("\"baseUrl\":\"")) {
                        int baseUrlStart = captionsJson.indexOf("\"baseUrl\":\"") + "\"baseUrl\":\"".length();
                        int baseUrlEnd = captionsJson.indexOf("\"", baseUrlStart);
                        String baseUrl = captionsJson.substring(baseUrlStart, baseUrlEnd).replace("\\u0026", "&");
                        Document transcriptDoc = Jsoup.connect(baseUrl).get();
                        return transcriptDoc.select("text").text();
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error retrieving transcript: " + e.getMessage());
        }
        return null;
    }

    public interface TranscriptCallback {
        void onTranscript(String transcript);
    }
}