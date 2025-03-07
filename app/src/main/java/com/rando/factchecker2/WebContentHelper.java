package com.rando.factchecker2;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;

public class WebContentHelper {

    private static final OkHttpClient client = new OkHttpClient();

    public static String getWebContent(String urlString) {
        Request request = new Request.Builder()
                .url(urlString)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) return "Error: HTTP " + response.code();
            if (response.body() == null) return "Error: Empty response body";

            Document doc = Jsoup.parse(response.body().string());
            /*doc.select("script, style, nav, aside, footer, header, head, meta, link, title, input, button, ").remove(); //remove unneeded elements.
            doc.select("footer").remove();
            StringBuilder mainContent = new StringBuilder();
            for (Element element : doc.getAllElements()) {
                mainContent.append(element.text());
                mainContent.append("\n");
            }*/
            Element mainContent = doc.selectFirst("article, main, div[role='main']");
            mainContent.select("script, style, nav, aside, link, input, button").remove();

            return mainContent.text();

        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
