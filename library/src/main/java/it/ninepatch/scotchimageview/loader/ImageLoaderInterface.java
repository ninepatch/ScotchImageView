package it.ninepatch.scotchimageview.loader;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.DrawableRes;

import it.ninepatch.scotchimageview.ScotchImageView;

/**
 * Created by Raphael Bussa on 7/12/16.
 */
public interface ImageLoaderInterface {

    @DrawableRes
    int placeholder();

    void load(Context context, ScotchImageView clusterImageZoom, Uri url, int placeholder);

    void clear(ScotchImageView clusterImageZoom);

}
