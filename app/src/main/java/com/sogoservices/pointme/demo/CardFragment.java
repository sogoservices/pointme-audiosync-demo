package com.sogoservices.pointme.demo;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sogoservices.pointme.R;

public class CardFragment extends Fragment {

    private static String ARGS_SEARCH_RESULT = "ARGS_SEARCH_RESULT";
    private static String ARGS_IS_HISTORY = "ARGS_IS_HISTORY";

    public static CardFragment newInstance(SearchResult searchResult, boolean isHistory) {
        CardFragment fragment = new CardFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARGS_SEARCH_RESULT, searchResult);
        args.putBoolean(ARGS_IS_HISTORY, isHistory);
        fragment.setArguments(args);
        return fragment;
    }

    public enum Mode { DETAILS, WEB }

    private DemoActivity demoActivity;
    private SearchResult searchResult;
    private boolean isHistory;
    private Mode mode = Mode.DETAILS;
    private Handler handler = new Handler();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            searchResult = args.getParcelable(ARGS_SEARCH_RESULT);
            isHistory = args.getBoolean(ARGS_IS_HISTORY);
        }
        demoActivity = (DemoActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.demo_fragment_card, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        getChildFragmentManager().beginTransaction()
            .replace(R.id.container, CardDescriptionFragment.newInstance(searchResult, isHistory), null).commit();
    }

    @Override
    public void onPause() {
        super.onPause();
        getChildFragmentManager().beginTransaction()
            .replace(R.id.container, CardDescriptionFragment.newInstance(searchResult, isHistory), null).commit();
        mode = Mode.DETAILS;
    }

    @Override
    public void onStop() {
        super.onStop();
        handler.removeCallbacksAndMessages(null);
    }

    public void setSearchResult(SearchResult searchResult) {
        this.searchResult = searchResult;
        getChildFragmentManager().beginTransaction()
            .replace(R.id.container, CardDescriptionFragment.newInstance(searchResult, isHistory), null).commit();
        mode = Mode.DETAILS;
    }

    void changeMode() {
        if (searchResult == null) return;
        if (mode == Mode.DETAILS) {
            mode = Mode.WEB;
            getChildFragmentManager().beginTransaction()
                .setCustomAnimations(R.animator.flip_right_in, R.animator.flip_left_out)
                .replace(R.id.container, CardWebFragment.newInstance(searchResult.url, isHistory), null).commit();
            demoActivity.navigate(searchResult);
            if (!isHistory) {
                demoActivity.stopSearch();
                demoActivity.onSearchButtonClick();
            }
            demoActivity.vibrate();
        } else {
            mode = Mode.DETAILS;
            getChildFragmentManager().beginTransaction()
                .setCustomAnimations(R.animator.flip_left_in, R.animator.flip_right_out)
                .replace(R.id.container, CardDescriptionFragment.newInstance(searchResult, isHistory), null)
                .commit();
            if (!isHistory) {
                handler.postDelayed(() -> demoActivity.startSearch(),
                    (int) (1.5 * getResources().getInteger(R.integer.card_flip_time_full)));
            }
        }
    }
}
