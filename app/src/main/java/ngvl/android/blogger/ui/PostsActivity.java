package ngvl.android.blogger.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;

import ngvl.android.blogger.R;
import ngvl.android.blogger.model.Entry;
import ngvl.android.blogger.ui.view.SlidingTabLayout;

public class PostsActivity extends ActionBarActivity implements
		OnPostSelectedListener, OnPageChangeListener {

    private static final String TAG = PostsActivity.class.getSimpleName();

	private static final String STATE_TAB_POSITION = "tabPosition";

	private static final String TAG_FRAGMENT_DETAIL = "detail";

	private ViewPager mViewPager;
    private Toolbar mToolbar;
    private SlidingTabLayout mSlidingTabLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

		setContentView(R.layout.activity_post);

        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

		mViewPager = (ViewPager) findViewById(R.id.viewPager);
		if (mViewPager != null) {
			mViewPager.setAdapter(new RssPageAdapter(getSupportFragmentManager()));
			mViewPager.setOnPageChangeListener(this);
		}

        mSlidingTabLayout = (SlidingTabLayout)findViewById(R.id.tabs);
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setViewPager(mViewPager);
        mSlidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.accent));

        initLeftPanel();
	}

    @Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
        outState.putInt(STATE_TAB_POSITION, mViewPager.getCurrentItem());
	}

	@Override
	public void onPostSelected(long entryId) {
		Log.d(TAG, "onEntrySelected");
		FragmentManager fm = getSupportFragmentManager();
		if (isTablet()) {
			Log.d(TAG, "tablet");
			Entry entry = PostDetailFragment.loadEntryFromId(this, entryId);

			if (entry != null) {
				
				PostDetailFragment fragment = PostDetailFragment
						.newInstance(entry);
				fm.beginTransaction()
						.replace(R.id.postDetail, fragment, TAG_FRAGMENT_DETAIL)
						.commit();
			}

		} else {
			Log.d(TAG, "smartphone");
			Intent it = new Intent(this, PostDetailActivity.class);
			Entry entry = PostDetailFragment.loadEntryFromId(this, entryId);
			it.putExtra(PostDetailActivity.EXTRA_ENTRY, entry);
			startActivity(it);
		}
	}

    // ******************************************************************************************
	// RssPageAdapter
	// ******************************************************************************************
	public class RssPageAdapter extends FragmentPagerAdapter {
		private PostsWebListFragment fragment;
		private PostsFavoritesListFragment fragmentFavorites;

		public RssPageAdapter(FragmentManager fm) {
			super(fm);
			fragment = (PostsWebListFragment) fm.findFragmentByTag("web");
			fragmentFavorites = (PostsFavoritesListFragment) fm
					.findFragmentByTag("local");

			if (fragment == null || fragmentFavorites == null) {
				fragment = new PostsWebListFragment();
				fragmentFavorites = new PostsFavoritesListFragment();
			}
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public Fragment getItem(int position) {
			Log.d(TAG, "RssPageAdapter:getItem:position=" + position);
			if (position == 0) {
				return fragment;
			} else {
				return fragmentFavorites;
			}
		}

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return getString(R.string.recent_posts);
            } else {
                return getString(R.string.favorites);
            }
        }
    }

	// ******************************************************************************************
	// PageListener
	// ******************************************************************************************
	@Override
	public void onPageScrollStateChanged(int state) {
	}

	@Override
	public void onPageScrolled(int position, float scrollOffset,
			int positionOffsetPixels) {
	}

	@Override
	public void onPageSelected(int position) {
        invalidateOptionsMenu();
	}
	
	// ******************************************************************************************
	// Private Methods
	// ******************************************************************************************
	private boolean isTablet() {
		return getResources().getBoolean(R.bool.tablet);
	}

    private void initLeftPanel() {
        final SlidingPaneLayout slidingPaneLayout = (SlidingPaneLayout)
                findViewById(R.id.sliding_pane_layout);
        if (isTablet() && slidingPaneLayout != null){
            slidingPaneLayout.setPanelSlideListener(new SlidingPaneLayout.PanelSlideListener() {
                @Override
                public void onPanelSlide(View panel, float slideOffset) {

                }

                @Override
                public void onPanelOpened(View panel) {
                    slidingPaneLayout.requestLayout();
                }

                @Override
                public void onPanelClosed(View panel) {
                    slidingPaneLayout.requestLayout();
                }
            });
        }
    }

}
