package com.sogoservices.pointme.demo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.sogoservices.pointme.R;

public class CardWebFragment extends Fragment {

    private static String ARGS_URL = "ARGS_URL";
    private static String ARGS_IS_HISTORY = "ARGS_IS_HISTORY";

    public static CardWebFragment newInstance(String url, boolean isHistory) {
        CardWebFragment fragment = new CardWebFragment();
        Bundle args = new Bundle();
        args.putString(ARGS_URL, url);
        args.putBoolean(ARGS_IS_HISTORY, isHistory);
        fragment.setArguments(args);
        return fragment;
    }

    private CardFragment cardFragment;
    private String url;
    private boolean isHistory;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cardFragment = (CardFragment) getParentFragment();
        Bundle args = getArguments();
        if (args != null) {
            url = args.getString(ARGS_URL);
            isHistory = args.getBoolean(ARGS_IS_HISTORY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.demo_fragment_card_web, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        view.findViewById(R.id.backView).setOnClickListener(v -> {
            cardFragment.changeMode();
        });

        WebView webView = view.findViewById(R.id.webView);
        if (url != null) {
            webView.loadUrl(url);
        }

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
        });

        if (isHistory) {
            view.findViewById(R.id.backView).setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green2));
            view.findViewById(R.id.backgroundView).setBackgroundResource(R.drawable.bg_card_green2);
        }
    }
}
