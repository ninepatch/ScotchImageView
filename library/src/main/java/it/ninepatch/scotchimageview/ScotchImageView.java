package it.ninepatch.scotchimageview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
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

    public static String TAG = "ScotchImageView";

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


    //circle opt
    private RectF drawableRect;
    private RectF borderRect;
    private Matrix shaderMatrix;
    private Paint bitmapPaint;
    private Paint borderPaint;
    private Paint fillPaint;

    private int borderColor = Color.BLACK;
    private int borderWidth = 0;
    private int fillColor = Color.TRANSPARENT;
    private Bitmap bitmap;
    private BitmapShader bitmapShader;
    private int bitmapWidth;
    private int bitmapHeight;
    private float drawableRadius;
    private float borderRadius;

    public ScotchImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        super.setClickable(true);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ZoomImage, defStyleAttr, 0);
        setPlaceholder(a.getResourceId(R.styleable.ZoomImage_placeholder, 0));
        setUrlImage(a.getString(R.styleable.ZoomImage_url_image));
        setupZoom(a.getBoolean(R.styleable.ZoomImage_zoomable, false));
        setupCircle(a.getBoolean(R.styleable.ZoomImage_circle, false));
        a.recycle();

    }

    public ScotchImageView(Context context, AttributeSet attr) {
        this(context, attr, 0);
    }

    public void setPlaceholder(@DrawableRes int placeholder) {
        this.placeholder = placeholder;
    }

    public void setMaxScale(float maxScale) {
        this.maxScale = maxScale;
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

    private void setupZoom(boolean zoomable) {
        if (!zoomable) return;
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

    private void setupCircle(boolean circle) {

        if (!circle) return;

        if (getWidth() == 0 && getHeight() == 0) {
            return;
        }

        bitmap = ((BitmapDrawable) getDrawable()).getBitmap();

        drawableRect = new RectF();
        borderRect = new RectF();
        shaderMatrix = new Matrix();
        bitmapPaint = new Paint();
        borderPaint = new Paint();
        fillPaint = new Paint();


        bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        bitmapPaint.setAntiAlias(true);
        bitmapPaint.setShader(bitmapShader);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setAntiAlias(true);
        borderPaint.setColor(borderColor);
        borderPaint.setStrokeWidth(borderWidth);

        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setAntiAlias(true);
        fillPaint.setColor(fillColor);

        bitmapHeight = bitmap.getHeight();
        bitmapWidth = bitmap.getWidth();

        borderRect.set(getRect());
        borderRadius = Math.min((borderRect.height() - borderWidth) / 2.0f, (borderRect.width() - borderWidth) / 2.0f);

        drawableRect.set(borderRect);
       /* if (!borderOverlay && borderWidth > 0) {
            drawableRect.inset(borderWidth - 1.0f, borderWidth - 1.0f);
        }
*/
        drawableRadius = Math.min(drawableRect.height() / 2.0f, drawableRect.width() / 2.0f);


        updateShaderMatrix();
        invalidate();
    }

    private RectF getRect() {
        int width = getWidth() - getPaddingLeft() - getPaddingRight();
        int height = getHeight() - getPaddingTop() - getPaddingBottom();

        int sideLength = Math.min(height, width);

        float left = getPaddingLeft() + (width - sideLength) / 2f;
        float top = getPaddingTop() + (height - sideLength) / 2f;

        return new RectF(left, top, left + sideLength, top + sideLength);
    }


    private void updateShaderMatrix() {

        float scale;
        float dx = 0;
        float dy = 0;


        if (this.bitmapWidth * this.drawableRect.height() > drawableRect.width() * bitmapHeight) {
            scale = drawableRect.height() / (float) bitmapHeight;
            dx = (drawableRect.width() - bitmapWidth * scale) * 0.5f;
        } else {
            scale = drawableRect.width() / (float) bitmapWidth;
            dy = (drawableRect.height() - bitmapHeight * scale) * 0.5f;
        }
        shaderMatrix.set(null);
        shaderMatrix.setScale(scale, scale);
        shaderMatrix.postTranslate((int) (dx + 0.5f) + drawableRect.left, (int) (dy + 0.5f) + drawableRect.top);

        bitmapShader.setLocalMatrix(shaderMatrix);

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