package it.ninepatch.scotchimageview.sample;

import android.app.Application;
import android.content.Context;
import android.net.Uri;

import com.bumptech.glide.Glide;

import it.ninepatch.scotchimageview.ScotchImageView;
import it.ninepatch.scotchimageview.loader.ClusterInitLoader;
import it.ninepatch.scotchimageview.loader.ImageLoaderInterface;

/**
 * Created by luca on 7/12/16.
 */
public class SampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();


        ClusterInitLoader.init(new ImageLoaderInterface() {
            @Override
            public int placeholder() {

                return R.drawable.ic_assignment_late_green_300_48dp;

            }

            @Override
            public void load(Context context, ScotchImageView clusterImageZoom, Uri url, int placeholder) {

                Glide.with(context).load(url).asBitmap().placeholder(placeholder).into(clusterImageZoom);

            }

            @Override
            public void clear(ScotchImageView clusterImageZoom) {
                Glide.clear(clusterImageZoom);
            }
        });
    }

}
