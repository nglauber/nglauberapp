package ngvl.android.blogger;

import android.app.Application;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import ngvl.android.blogger.service.SyncUtil;

public class RssReaderApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
        int imgSize = (int)(getResources().getDimension(R.dimen.thumbnail_size) / 2);

		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .displayer(new RoundedBitmapDisplayer(imgSize))
				.cacheInMemory(true)
                .cacheOnDisc(true)
                .build();

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				getApplicationContext()).defaultDisplayImageOptions(
				defaultOptions).build();

		ImageLoader.getInstance().init(config);

        SyncUtil.setSyncAutomatically(this);
	}
}
