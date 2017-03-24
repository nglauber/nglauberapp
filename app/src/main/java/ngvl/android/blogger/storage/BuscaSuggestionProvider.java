package ngvl.android.blogger.storage;

import android.content.SearchRecentSuggestionsProvider;

public class BuscaSuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "ngvl.android.blogger.storage.BuscaSuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public BuscaSuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}