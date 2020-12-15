package com.sogoservices.pointme.demo;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class CardAdapter extends FragmentPagerAdapter {

    private SearchResult currentSearchResult;
    private List<CardFragment> fragmentList = new ArrayList<>();

    public CardAdapter(@NonNull FragmentManager fm) {
        super(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        fragmentList.add(CardFragment.newInstance(null, false));
        for (SearchResult item: SearchHistory.getInstance().getHistory()) {
            fragmentList.add(CardFragment.newInstance(item, true));
            notifyDataSetChanged();
        }
    }

    public void setCurrentSearchResult(SearchResult searchResult) {
        if ((currentSearchResult == null && searchResult != null) || (currentSearchResult != null && !currentSearchResult.equals(searchResult))) {
            currentSearchResult = searchResult;
            fragmentList.get(0).setSearchResult(searchResult);
        }
    }

    public void addSearchResultToHistory(SearchResult searchResult) {
        fragmentList.add(CardFragment.newInstance(searchResult, true));
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        if (object instanceof CardFragment && fragmentList.contains(object)) {
            return fragmentList.indexOf(object);
        }
        return POSITION_NONE;
    }

    public void onSearchButtonPressed() {
        fragmentList.get(0).changeMode();
    }
}
