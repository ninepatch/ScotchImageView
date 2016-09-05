package it.ninepatch.scotchimageview.sample;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import it.ninepatch.scotchimageview.ScotchImageView;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ScotchImageView scotchImageView1 = (ScotchImageView) findViewById(R.id.scotchimageview1);
        ScotchImageView scotchImageView2 = (ScotchImageView) findViewById(R.id.scotchimageview2);
        scotchImageView1.setUrlImage("http://www.regione.veneto.it/image/image_gallery?uuid=a622239e-08cb-4322-883f-3415e4b5d20f&groupId=10709&t=1438336966306");

        scotchImageView2.setZoomable(true);
        scotchImageView1.setZoomable(false);
        //   Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_assignment_late_green_300_48dp);
        //    touch.setImageBitmap(bitmap);
    }
}
