package com.rando.factchecker2;

import android.graphics.Typeface;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextFormatter {

    public static void formatText(String text, TextView textView) {
        StringBuffer modifiedText = new StringBuffer();
        Pattern boldPattern = Pattern.compile("\\*\\*(.*?)\\*\\*"); // Non-greedy matching
        Matcher matcher = boldPattern.matcher(text);
        List<Integer> startPositions = new ArrayList<>();
        List<Integer> endPositions = new ArrayList<>();
        while (matcher.find()) {
            String matchedText = matcher.group(1);
            int start = matcher.start(1);
            int end = matcher.end(1);
            startPositions.add(start);
            endPositions.add(end);

            matcher.appendReplacement(modifiedText, matchedText);
        }
        matcher.appendTail(modifiedText);
        SpannableString spannableString = new SpannableString(modifiedText.toString());

        for (int i = 0; i < endPositions.size(); i++) {
            spannableString.setSpan(new StyleSpan(Typeface.BOLD), startPositions.get(i), endPositions.get(i), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        textView.setText(spannableString);
        //System.out.println(text);
    }
}
