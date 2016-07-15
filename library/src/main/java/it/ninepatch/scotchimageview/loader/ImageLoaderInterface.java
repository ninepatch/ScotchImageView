package it.ninepatch.scotchimageview.loader;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.DrawableRes;

import it.ninepatch.scotchimageview.ScotchImageView;

/**
 * Created by Raphael Bussa on 7/12/16.
 */
public interface ImageLoaderInterface {

    /**
     * @return drawable res of placeholder
     */
    @DrawableRes
    int placeholder();

    /**
     * @param context current context
     * @param clusterImageZoom current ScotchImageView
     * @param url uri to load
     * @param placeholder res placeholder
     */
    void load(Context context, ScotchImageView clusterImageZoom, Uri url, @DrawableRes int placeholder);

    /**
     * @param clusterImageZoom current ScotchImageView
     */
    void clear(ScotchImageView clusterImageZoom);

}
