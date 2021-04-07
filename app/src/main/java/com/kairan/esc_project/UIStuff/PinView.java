package com.kairan.esc_project.UIStuff;

import android.content.Context;
import android.graphics.*;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;

import androidx.annotation.Nullable;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.kairan.esc_project.R;

public class PinView extends SubsamplingScaleImageView {

    private final Paint paint = new Paint();
    private PointF vPin = new PointF();
    private PointF sPin;
    private Bitmap pin;

    private static final int PIN_SIZE_DEF = 20;

    public PinView(Context context) {
        this(context, null);
        initialise(null);
    }

    public PinView(Context context, AttributeSet attr) {
        super(context, attr);
        initialise(attr);
    }

    public void setPin(PointF sPin) {
        this.sPin = sPin;
        Log.i("CHECKIMAGE","sPin: "+sPin);
        //initialise();
        postInvalidate();
    }

    private void initialise(@Nullable AttributeSet set) {
        //float density = getResources().getDisplayMetrics().densityDpi;
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
                pin =getResizedBitmap(pin,getWidth(),getHeight());
            }
        });

        paint.setAntiAlias(true);
        //paint.setColor(Color.GRAY);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Don't draw pin before image is ready so it doesn't move around during setup.
        /*if (!isImageLoaded()) {
            Log.i("CHECKIMAGE","not ready");
            return;
        }*/

        /*boolean m = (sPin != null);
        boolean n = (pin != null);
        Log.i("CHECKIMAGE","spin != null: " + m);
        Log.i("CHECKIMAGE","pin != null: " + n);
        if (sPin != null && pin != null) {
            vPin = sourceToViewCoord(sPin);
            float vX = vPin.x - (pin.getWidth()/2);
            float vY = vPin.y - pin.getHeight();
            float vX = sPin.x;
            float vY = sPin.y;
            canvas.drawBitmap(pin, vX, vY, paint);
            Log.i("CHECKIMAGE","pin: "+pin);
            Log.i("CHECKIMAGE","vX: "+ vX);
            Log.i("CHECKIMAGE","vY: "+ vY);
            //canvas.drawBitmap(pin,0,0,paint);
        }*/
        canvas.drawBitmap(pin,0,0,null);
        Log.i("CHECKIMAGE","draw bitmap");

    }


    private Bitmap getResizedBitmap(Bitmap bitmap, int reqWidth, int reqHeight){
        Matrix matrix = new Matrix();

        RectF src = new RectF(0,0,bitmap.getWidth(),bitmap.getHeight());
        RectF dst = new RectF(0,0,reqWidth,reqHeight);

        matrix.setRectToRect(src,dst, Matrix.ScaleToFit.CENTER);
        return Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
    }



}