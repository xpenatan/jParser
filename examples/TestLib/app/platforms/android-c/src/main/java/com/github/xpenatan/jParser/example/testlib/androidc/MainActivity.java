package com.github.xpenatan.jParser.example.testlib.androidc;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean passed = false;
        Throwable failure = null;
        try {
            passed = AndroidCBridge.runTestLibTest();
        }
        catch(Throwable t) {
            failure = t;
        }
        System.out.println("TestLib Android-C Pass " + passed);

        TextView view = new TextView(this);
        view.setGravity(Gravity.CENTER);
        view.setTextSize(22);
        view.setTextColor(Color.WHITE);
        view.setBackgroundColor(passed ? Color.rgb(0, 170, 0) : Color.rgb(170, 0, 0));
        view.setText(passed ? "TestLib Android-C Pass" : failureText(failure));
        setContentView(view);
    }

    private static String failureText(Throwable failure) {
        if(failure == null) {
            return "TestLib Android-C Failed";
        }
        return "TestLib Android-C Failed\n" + failure.getClass().getSimpleName() + ": " + failure.getMessage();
    }
}
