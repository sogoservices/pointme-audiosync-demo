package com.sogoservices.pointme.demo;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.sogoservices.pointme.R;

public class CardDescriptionFragment extends Fragment {

    private static String ARGS_SEARCH_RESULT = "ARGS_SEARCH_RESULT";
    private static String ARGS_IS_HISTORY = "ARGS_IS_HISTORY";

    public static CardDescriptionFragment newInstance(SearchResult searchResult, boolean isHistory) {
        CardDescriptionFragment fragment = new CardDescriptionFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARGS_SEARCH_RESULT, searchResult);
        args.putBoolean(ARGS_IS_HISTORY, isHistory);
        fragment.setArguments(args);
        return fragment;
    }

    private SearchResult searchResult;
    private boolean isHistory;
    private CardFragment cardFragment;
    private Handler handler = new Handler();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cardFragment = (CardFragment) getParentFragment();
        Bundle args = getArguments();
        if (args != null) {
            searchResult = args.getParcelable(ARGS_SEARCH_RESULT);
            isHistory = args.getBoolean(ARGS_IS_HISTORY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.demo_fragment_card_description, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        CardView rootView = view.findViewById(R.id.root);
        ImageView iconView = view.findViewById(R.id.iconView);
        TextView descriptionView = view.findViewById(R.id.descriptionView);

        int backgroundColorResId = isHistory ? R.color.green2 : R.color.blue;
        rootView.setCardBackgroundColor(ContextCompat.getColor(requireContext(), backgroundColorResId));

        if (searchResult != null && searchResult.bitmap != null) {
            iconView.setImageBitmap(searchResult.bitmap);
        }

        if (searchResult != null) {
            descriptionView.setText(searchResult.description);
        } else {
            descriptionView.setText(R.string.demo_description);
        }

        rootView.setEnabled(false);
        handler.postDelayed(() -> {
            rootView.setEnabled(true);
            rootView.setOnClickListener(v -> cardFragment.changeMode());
        }, getResources().getInteger(R.integer.card_flip_time_half));
    }

    @Override
    public void onStop() {
        super.onStop();
        handler.removeCallbacksAndMessages(null);
    }
}
