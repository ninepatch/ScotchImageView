package it.ninepatch.scotchimageview.sample;

import android.app.Application;
import android.content.Context;
import android.net.Uri;

import com.bumptech.glide.Glide;

import it.ninepatch.scotchimageview.ScotchImageView;
import it.ninepatch.scotchimageview.loader.ScotchInitLoader;
import it.ninepatch.scotchimageview.loader.ImageLoaderInterface;

/**
 * Created by luca on 7/12/16.
 */
public class SampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();


        ScotchInitLoader.init(new ImageLoaderInterface() {
            @Override
            public int placeholder() {
                return R.drawable.ic_assignment_late_green_300_48dp;
            }

            @Override
            public void load(Context context, ScotchImageView scotchImageView, Uri url, int placeholder) {
                Glide.with(context).load(url).asBitmap().placeholder(placeholder).into(scotchImageView);
            }

            @Override
            public void clear(ScotchImageView scotchImageView) {
                   Glide.clear(scotchImageView);
            }
        });

    }

}
