package it.ninepatch.scotchimageview.loader;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import it.ninepatch.scotchimageview.ScotchImageView;

/**
 * Created by Raphael Bussa on 7/12/16.
 */
public class ScotchInitLoader {

    private static final String TAG = ScotchInitLoader.class.getName();

    private static ScotchInitLoader instance;

    private ImageLoaderInterface imageLoaderInterface;

    public static ScotchInitLoader init(ImageLoaderInterface imageLoaderInterface) {
        initInstance();
        instance.imageLoaderInterface = imageLoaderInterface;
        return instance;
    }

    private static void initInstance() {
        if (instance == null) {
            instance = new ScotchInitLoader();
        }
    }

    public static ScotchInitLoader getInstance() {
        initInstance();
        return instance;
    }

    public ImageLoaderInterface getImageLoaderInterface() {

        if (imageLoaderInterface == null) {
            imageLoaderInterface = new ImageLoaderInterface() {
                @Override
                public int placeholder() {
                    return 0;
                }

                @Override
                public void load(Context context, ScotchImageView scotchImageView, Uri url, int placeholder) {
                    Log.e(TAG, "init image loader in your application class for set image from url");
                }

                @Override
                public void clear(ScotchImageView clusterImageZoom) {

                }
            };
        }
        return imageLoaderInterface;
    }
}
