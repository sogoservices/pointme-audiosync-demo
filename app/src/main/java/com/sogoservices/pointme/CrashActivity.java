package com.sogoservices.pointme;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class CrashActivity extends AppCompatActivity {

    private static String EXTRA_THROWABLE = "EXTRA_THROWABLE";

    public static void start(Context context, Throwable t) {
        Intent intent = new Intent(context, CrashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(EXTRA_THROWABLE, t);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crash_activity);

        Throwable t = (Throwable) getIntent().getSerializableExtra(EXTRA_THROWABLE);
        findViewById(R.id.sendButton).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "PointMe crash logs");
            intent.putExtra(Intent.EXTRA_TEXT, String.format("Device manufacturer: %s\n" +
                "Device model: %s\n" +
                "Android version: %s\n" +
                "Code version: %s\n\n%s", Build.MANUFACTURER, Build.MODEL,
                Build.VERSION.RELEASE, BuildConfig.VERSION_CODE, getFullStackTrace(t)));

            if (getPackageManager().resolveActivity(intent, 0) != null) {
                startActivity(intent);
            }
        });
    }

    private String getFullStackTrace(Throwable t) {
        StringBuilder builder = new StringBuilder();
        StackTraceElement[] traces = t.getStackTrace();
        if (traces.length > 0) {
            builder.append(t.getClass().getName() + ": " + t.getMessage() + "\n");
            for (StackTraceElement trace: traces) {
                builder.append(String.format("    at %s.%s(%s:%s)\n", trace.getClassName(),
                    trace.getMethodName(), trace.getFileName(), trace.getLineNumber()));
            }
        }
        Throwable cause = t.getCause();
        while (cause != null) {
            StackTraceElement[] causeTraces = cause.getStackTrace();
            if (causeTraces.length > 0) {
                builder.append(String.format("Caused by: %s: %s\n", cause.getClass().getName(), cause.getMessage()));
                for (StackTraceElement causeTrace: causeTraces) {
                    builder.append(String.format("    at %s.%s(%s:%s)\n", causeTrace.getClassName(),
                        causeTrace.getMethodName(), causeTrace.getFileName(), causeTrace.getLineNumber()));
                }
            }
            // fetch next cause
            cause = cause.getCause();
        }
        return builder.toString();
    }
}
