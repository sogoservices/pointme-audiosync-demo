package com.sogoservices.pointme.demo;

import java.util.ArrayList;
import java.util.List;

public class SearchHistory {

    private static SearchHistory sInstance;

    public static SearchHistory getInstance() {
        if (sInstance == null) {
            sInstance = new SearchHistory();
        }
        return sInstance;
    }

    private List<SearchResult> searchHistory = new ArrayList<>();

    private SearchHistory() {}

    public List<SearchResult> getHistory() {
        return this.searchHistory;
    }

    public boolean addItem(SearchResult item) {
        if (!this.searchHistory.contains(item)) {
            this.searchHistory.add(item);
            return true;
        }
        return false;
    }

    public void clear() {
        this.searchHistory.clear();
    }
}
