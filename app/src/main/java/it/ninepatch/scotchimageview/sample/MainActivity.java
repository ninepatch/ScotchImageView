package it.ninepatch.scotchimageview.sample;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import it.ninepatch.scotchimageview.ScotchImageView;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ScotchImageView scotchImageView = (ScotchImageView) findViewById(R.id.image);
        //scotchImageView.setImageResource(R.drawable.ic_assignment_late_green_300_48dp);
        scotchImageView.setUrlImage("http://feelgrafix.com/data/wallpapers/wallpapers-20.jpg");

        //   Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_assignment_late_green_300_48dp);
        //    touch.setImageBitmap(bitmap);
    }
}
