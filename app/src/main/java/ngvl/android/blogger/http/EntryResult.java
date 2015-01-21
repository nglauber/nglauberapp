package ngvl.android.blogger.http;

import java.util.List;

import ngvl.android.blogger.model.Entry;

/**
 * Class that represents the result of read atom.xml
 */
public class EntryResult {
	private List<Entry> entries;
	private String currentPage;
	private String nextPage;

	public EntryResult(List<Entry> entries, String currentPage, String nextPage) {
		super();
		this.entries = entries;
		this.currentPage = currentPage;
		this.nextPage = nextPage;
	}

	/**
	 * Start entry index used for pagination. If a feed has 100 entries and this
	 * value returns 26, {@link EntryResult#getEntries()} will return a list
	 * with {@link ngvl.android.blogger.http.EntryResult#getEntries()} entries,
     * starting from 26th entry.
	 */
	public String getCurrentPage() {
		return currentPage;
	}

	/**
	 * Used for pagination, this method returns the number of entries loaded for
	 * each request. Note that this value maybe different of
	 * {@link EntryResult#getEntries()} size.
	 */
	public String getNextPage() {
		return nextPage;
	}

	/**
	 * List of entries parsed.
	 */
	public List<Entry> getEntries() {
		return entries;
	}
}
