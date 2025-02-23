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
        List<Integer> startPositions = new ArrayList<>();
        List<Integer> endPositions = new ArrayList<>();
        StringBuilder formattedText = new StringBuilder();
        int currentIndex = 0;

        Pattern pattern = Pattern.compile("\\*\\*(.*?)\\*\\*"); // Matches text between asterisks
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            // Append the text before the matched pattern
            formattedText.append(text.substring(currentIndex, matcher.start()));

            // Store the start and end positions (relative to the *content*)
            startPositions.add(formattedText.length()); // Start position of the content
            formattedText.append(matcher.group(1));  // Append the content without *
            endPositions.add(formattedText.length()); // End position of the content

            currentIndex = matcher.end(); // Move the current index to the end of the match
        }


        // Append any remaining text after the last match
        formattedText.append(text.substring(currentIndex));
        SpannableString spannableString = new SpannableString(formattedText.toString().replace("\n*", "\n•"));

        for (int i = 0; i < endPositions.size(); i++) {
            spannableString.setSpan(new StyleSpan(Typeface.BOLD), startPositions.get(i), endPositions.get(i), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        textView.setText(spannableString);
        //System.out.println(text);
    }
}
