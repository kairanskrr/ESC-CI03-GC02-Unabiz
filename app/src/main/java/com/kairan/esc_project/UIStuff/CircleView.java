package com.kairan.esc_project.UIStuff;

import android.content.Context;
import android.graphics.*;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewTreeObserver;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.kairan.esc_project.R;

public class CircleView extends SubsamplingScaleImageView {

    private int strokeWidth;

    private final PointF sCenter = new PointF();
    private final PointF vCenter = new PointF();
    private final Paint paint = new Paint();

    private Bitmap pin;

    public CircleView(Context context) {
        this(context, null);
    }

    public CircleView(Context context, AttributeSet attr) {
        super(context, attr);
        initialise();
    }

    private void initialise() {
        float density = getResources().getDisplayMetrics().densityDpi;
        strokeWidth = (int)(density/60f);
        pin = BitmapFactory.decodeResource(getResources(), R.drawable.app_icon);
        Log.i("CHECKIMAGE","decoding bitmap");
        Log.i("CHECKIMAGE","Pin: "+pin);
        /*float w = (density/420f) * pin.getWidth();
        float h = (density/420f) * pin.getHeight();
        pin = Bitmap.createScaledBitmap(pin, (int)w, (int)h, true);*/
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                pin =getResizedBitmap(pin,50,50);
            }
        });

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//        // Don't draw pin before image is ready so it doesn't move around during setup.
//        if (!isReady()) {
//            return;
//        }

        sCenter.set(getSWidth()/2, getSHeight()/2);
        sourceToViewCoord(sCenter, vCenter);
        float radius = (getScale() * getSWidth()) * 0.25f;

        paint.setAntiAlias(true);
        paint.setStyle(Style.STROKE);
        paint.setStrokeCap(Cap.ROUND);
        paint.setStrokeWidth(strokeWidth * 2);
        paint.setColor(Color.BLACK);
//        canvas.drawCircle(vCenter.x, vCenter.y, radius, paint);
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(Color.argb(255, 51, 181, 229));
        canvas.drawCircle(100, 100, 100, paint);
        canvas.drawBitmap(pin,60,50,null);
    }

    private Bitmap getResizedBitmap(Bitmap bitmap, int reqWidth, int reqHeight){
        Matrix matrix = new Matrix();

        RectF src = new RectF(0,0,bitmap.getWidth(),bitmap.getHeight());
        RectF dst = new RectF(0,0,reqWidth,reqHeight);

        matrix.setRectToRect(src,dst, Matrix.ScaleToFit.CENTER);
        return Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
    }

}