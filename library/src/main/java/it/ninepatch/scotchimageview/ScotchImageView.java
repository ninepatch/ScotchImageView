package it.ninepatch.scotchimageview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

import it.ninepatch.scotchimageview.loader.ImageLoaderInterface;
import it.ninepatch.scotchimageview.loader.ScotchInitLoader;
import it.ninepatch.scotchimageview.utils.ScotchImageCostant;

/**
 * Created by luca on 7/12/16.
 **/
public class ScotchImageView extends ImageView {

    private static final float DEFAULT_SHADOW_RADIUS = 8.0f;
    public static String TAG = ScotchImageView.class.getSimpleName();
    private Matrix matrix = new Matrix();
    private int mode = ScotchImageCostant.NONE;
    private PointF last = new PointF();
    private PointF start = new PointF();
    private float minScale = 1f;
    private float maxScale = 4f;
    private float[] m;
    private float redundantXSpace, redundantYSpace;
    private float width, height;
    private float saveScale = 1f;
    private float right, bottom, origWidth, origHeight, bmWidth, bmHeight;
    private ScaleGestureDetector scaleGestureDetector;
    private int placeholder;
    private int canvasSize;




    private Bitmap bitmap;


    private int borderColor = Color.BLACK;
    private int borderWidth = 0;
    private boolean circle;
    private Paint roundPaint;

    public ScotchImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        super.setClickable(true);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ZoomImage, defStyleAttr, 0);
        roundPaint = new Paint();
        setPlaceholder(a.getResourceId(R.styleable.ZoomImage_placeholder, 0));
        setUrlImage(a.getString(R.styleable.ZoomImage_url_image));
        //  setupCircle(a.getBoolean(R.styleable.ZoomImage_circle, false));
        circle = a.getBoolean(R.styleable.ZoomImage_circle, false);
        borderWidth = a.getDimensionPixelOffset(R.styleable.ZoomImage_border_width, 0);
        borderColor = a.getDimensionPixelOffset(R.styleable.ZoomImage_border_color, Color.BLACK);
        setupZoom(a.getBoolean(R.styleable.ZoomImage_zoomable, false));
        a.recycle();

    }

    public ScotchImageView(Context context, AttributeSet attr) {
        this(context, attr, 0);
    }


    public void setPlaceholder(@DrawableRes int placeholder) {
        this.placeholder = placeholder;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
        float scale;
        float scaleX = width / bmWidth;
        float scaleY = height / bmHeight;
        scale = Math.min(scaleX, scaleY);
        matrix.setScale(scale, scale);
        setImageMatrix(matrix);
        saveScale = 1f;
        redundantYSpace = height - (scale * bmHeight);
        redundantXSpace = width - (scale * bmWidth);
        redundantYSpace /= 2;
        redundantXSpace /= 2;
        matrix.postTranslate(redundantXSpace, redundantYSpace);
        origWidth = width - 2 * redundantXSpace;
        origHeight = height - 2 * redundantYSpace;
        right = width * saveScale - width - (2 * redundantXSpace * saveScale);
        bottom = height * saveScale - height - (2 * redundantYSpace * saveScale);
        setImageMatrix(matrix);
    }


    public void setZoomable(boolean zoomable) {
        setupZoom(zoomable);
    }

    private void setupZoom(boolean zoomable) {

        if (!zoomable) {
            setOnTouchListener(null);
            return;
        }
        if (getDrawable() != null) {
            Bitmap bitmap = ((BitmapDrawable) getDrawable()).getBitmap();
            setImageBitmap(bitmap);
        }

        scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
        matrix.setTranslate(1f, 1f);
        m = new float[9];
        setImageMatrix(matrix);
        setScaleType(ScaleType.MATRIX);

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scaleGestureDetector.onTouchEvent(event);

                matrix.getValues(m);
                float x = m[Matrix.MTRANS_X];
                float y = m[Matrix.MTRANS_Y];
                PointF curr = new PointF(event.getX(), event.getY());

                switch (event.getAction()) {
                    //when one finger is touching
                    //set the mode to DRAG
                    case MotionEvent.ACTION_DOWN:
                        last.set(event.getX(), event.getY());
                        start.set(last);
                        mode = ScotchImageCostant.DRAG;
                        break;
                    //when two fingers are touching
                    //set the mode to ZOOM
                    case MotionEvent.ACTION_POINTER_DOWN:
                        last.set(event.getX(), event.getY());
                        start.set(last);
                        mode = ScotchImageCostant.ZOOM;
                        break;
                    //when a finger moves
                    //If mode is applicable move image
                    case MotionEvent.ACTION_MOVE:
                        //if the mode is ZOOM or
                        //if the <ode is DRAG and already zoomed
                        if (mode == ScotchImageCostant.ZOOM || (mode == ScotchImageCostant.DRAG && saveScale > minScale)) {
                            float deltaX = curr.x - last.x;// x difference
                            float deltaY = curr.y - last.y;// y difference
                            float scaleWidth = Math.round(origWidth * saveScale);// width after applying current scale
                            float scaleHeight = Math.round(origHeight * saveScale);// height after applying current scale
                            //if scaleWidth is smaller than the views width
                            //in other words if the image width fits in the view
                            //limit left and right movement
                            if (scaleWidth < width) {
                                deltaX = 0;
                                if (y + deltaY > 0)
                                    deltaY = -y;
                                else if (y + deltaY < -bottom)
                                    deltaY = -(y + bottom);
                            }

                            //if scaleHeight is smaller than the views height
                            //in other words if the image height fits in the view
                            //limit up and down movement
                            else if (scaleHeight < height) {
                                deltaY = 0;
                                if (x + deltaX > 0)
                                    deltaX = -x;
                                else if (x + deltaX < -right)
                                    deltaX = -(x + right);
                            }
                            //if the image doesnt fit in the width or height
                            //limit both up and down and left and right
                            else {
                                if (x + deltaX > 0)
                                    deltaX = -x;
                                else if (x + deltaX < -right)
                                    deltaX = -(x + right);

                                if (y + deltaY > 0)
                                    deltaY = -y;
                                else if (y + deltaY < -bottom)
                                    deltaY = -(y + bottom);
                            }
                            //move the image with the matrix
                            matrix.postTranslate(deltaX, deltaY);
                            //set the last touch location to the current
                            last.set(curr.x, curr.y);
                        }
                        break;
                    //first finger is lifted
                    case MotionEvent.ACTION_UP:
                        mode = ScotchImageCostant.NONE;
                        int xDiff = (int) Math.abs(curr.x - start.x);
                        int yDiff = (int) Math.abs(curr.y - start.y);
                        if (xDiff < ScotchImageCostant.CLICK && yDiff < ScotchImageCostant.CLICK)
                            performClick();
                        break;
                    // second finger is lifted
                    case MotionEvent.ACTION_POINTER_UP:
                        mode = ScotchImageCostant.NONE;
                        break;
                }
                setImageMatrix(matrix);
                invalidate();
                return true;
            }

        });
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        if (bm != null) {
            bmWidth = bm.getWidth();
            bmHeight = bm.getHeight();
        }
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        Drawable drawable = ContextCompat.getDrawable(getContext(), resId);
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        bmWidth = bitmap.getWidth();
        bmHeight = bitmap.getHeight();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        if (drawable == null || !(drawable instanceof Drawable)) return;

        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

        bmWidth = bitmap.getWidth();
        bmHeight = bitmap.getHeight();

    }

    /**
     * Use this method for set an image from url in ClusterImageZoom
     *
     * @param url image url
     */

    public void setUrlImage(String url) {
        if (TextUtils.isEmpty(url)) return;
        setUrlImage(Uri.parse(url));
    }

    /**
     * Use this method for set an image from url in ClusterImageZoom
     *
     * @param url image url
     */

    public void setUrlImage(Uri url) {
        if (url == null) return;
        ImageLoaderInterface initLoader = ScotchInitLoader.getInstance().getImageLoaderInterface();

        if (placeholder == 0) {
            placeholder = initLoader.placeholder();
        }

        initLoader.load(getContext(), this, url, placeholder);
    }


    private void updateShader() {
        if (bitmap == null)
            return;

        // Crop Center Image
        bitmap = cropBitmap(bitmap);

        // Create Shader
        BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        // Center Image in Shader
        Matrix matrix = new Matrix();
        matrix.setScale((float) canvasSize / (float) bitmap.getWidth(), (float) canvasSize / (float) bitmap.getHeight());
        shader.setLocalMatrix(matrix);

        // Set Shader in Paint
        roundPaint.setShader(shader);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (!circle) {
            super.onDraw(canvas);
            return;
        }
        // Load the bitmap


        if (getDrawable() != null) {
            bitmap = ((BitmapDrawable) getDrawable()).getBitmap();
        }
        updateShader();

        // Check if image isn't null
        if (bitmap == null)
            return;

        if (!isInEditMode()) {
            canvasSize = canvas.getWidth();
            if (canvas.getHeight() < canvasSize) {
                canvasSize = canvas.getHeight();
            }
        }


        int circleCenter = (int) (canvasSize - (borderWidth * 2)) / 2;

        canvas.drawCircle(circleCenter + borderWidth, circleCenter + borderWidth, circleCenter - (DEFAULT_SHADOW_RADIUS + DEFAULT_SHADOW_RADIUS / 2), roundPaint);

    }

    private Bitmap cropBitmap(Bitmap bitmap) {
        Bitmap bmp;
        if (bitmap.getWidth() >= bitmap.getHeight()) {
            bmp = Bitmap.createBitmap(
                    bitmap,
                    bitmap.getWidth() / 2 - bitmap.getHeight() / 2,
                    0,
                    bitmap.getHeight(), bitmap.getHeight());
        } else {
            bmp = Bitmap.createBitmap(
                    bitmap,
                    0,
                    bitmap.getHeight() / 2 - bitmap.getWidth() / 2,
                    bitmap.getWidth(), bitmap.getWidth());
        }
        return bmp;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mode = ScotchImageCostant.ZOOM;
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float mScaleFactor = detector.getScaleFactor();
            float origScale = saveScale;
            saveScale *= mScaleFactor;
            if (saveScale > maxScale) {
                saveScale = maxScale;
                mScaleFactor = maxScale / origScale;
            } else if (saveScale < minScale) {
                saveScale = minScale;
                mScaleFactor = minScale / origScale;
            }
            right = width * saveScale - width - (2 * redundantXSpace * saveScale);
            bottom = height * saveScale - height - (2 * redundantYSpace * saveScale);
            if (origWidth * saveScale <= width || origHeight * saveScale <= height) {
                matrix.postScale(mScaleFactor, mScaleFactor, width / 2, height / 2);
                if (mScaleFactor < 1) {
                    matrix.getValues(m);
                    float x = m[Matrix.MTRANS_X];
                    float y = m[Matrix.MTRANS_Y];
                    if (mScaleFactor < 1) {
                        if (Math.round(origWidth * saveScale) < width) {
                            if (y < -bottom)
                                matrix.postTranslate(0, -(y + bottom));
                            else if (y > 0)
                                matrix.postTranslate(0, -y);
                        } else {
                            if (x < -right)
                                matrix.postTranslate(-(x + right), 0);
                            else if (x > 0)
                                matrix.postTranslate(-x, 0);
                        }
                    }
                }
            } else {
                matrix.postScale(mScaleFactor, mScaleFactor, detector.getFocusX(), detector.getFocusY());
                matrix.getValues(m);
                float x = m[Matrix.MTRANS_X];
                float y = m[Matrix.MTRANS_Y];
                if (mScaleFactor < 1) {
                    if (x < -right)
                        matrix.postTranslate(-(x + right), 0);
                    else if (x > 0)
                        matrix.postTranslate(-x, 0);
                    if (y < -bottom)
                        matrix.postTranslate(0, -(y + bottom));
                    else if (y > 0)
                        matrix.postTranslate(0, -y);
                }
            }
            return true;
        }

    }

}