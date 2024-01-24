package com.android.onyx.demo.scribble;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import com.onyx.android.sdk.data.note.TouchPoint;

import java.util.ArrayList;
import java.util.List;

public class MyWebView extends WebView {
    private final float STROKE_WIDTH = 3.0f;

    private Canvas offscreenCanvas;
    private Bitmap offscreenBitmap;

    private Canvas penCanvas;
    private Bitmap penBitmap;

    private Paint paint = new Paint();

    private List<EmptyArea> emptyAreas = new ArrayList<EmptyArea>() {{
        add(new EmptyArea(500, 500));
    }};

    private class EmptyArea {
        private int y;
        private int height;

        public int getHeight() {
            return height;
        }

        public EmptyArea(final int y, final int height) {
            this.y = y;
            this.height = height;
        }
    }

    public MyWebView(@NonNull final Context context) {
        super(context);
        initPaint();
    }

    public void drawPointsToBitmap(final List<TouchPoint> points) {
        if (penBitmap == null) {
            penBitmap = Bitmap.createBitmap(this.getWidth(), this.getHeight() * 20, Bitmap.Config.ALPHA_8);
            penCanvas = new Canvas(penBitmap);
        }
        for (TouchPoint point : points) {
            point.offset(0, getScrollY());
        }
        Path path = new Path();
        PointF prePoint = new PointF(points.get(0).x, points.get(0).y);
        path.moveTo(prePoint.x, prePoint.y);
        for (TouchPoint point : points) {
            path.quadTo(prePoint.x, prePoint.y, point.x, point.y);
            prePoint.x = point.x;
            prePoint.y = point.y;
        }
        penCanvas.drawPath(path, paint);
    }

    private void initPaint() {
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(STROKE_WIDTH);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (offscreenCanvas == null) {
            offscreenBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            offscreenCanvas = new Canvas(offscreenBitmap);
        }
        //We want the superclass to draw directly to the offscreen canvas so that we don't get an infinitely deep recursive call
        if (canvas == offscreenCanvas) {
//            final Rect originalClipBounds = getClipBounds();
//            setClipBounds(new Rect(0, 500, getWidth(), getHeight() + 500));
            super.onDraw(offscreenCanvas);
//            setClipBounds(originalClipBounds);
        } else {
            ((RelativeLayout) getParent()).draw(offscreenCanvas);

            List<Bitmap> bitmaps = new ArrayList<Bitmap>();
            int cursor = 0;
            for (final EmptyArea emptyArea : emptyAreas) {
                int emptyAreaHeight = emptyArea.height;
                if (getScrollY() < emptyArea.y) {
                    Bitmap web = Bitmap.createBitmap(offscreenBitmap, 0, 0, getWidth(), emptyArea.y);
                    cursor = emptyArea.y - getScrollY();
                    bitmaps.add(web);
                } else {
                    emptyAreaHeight = Math.max(emptyArea.height - (getScrollY() - emptyArea.y), 0);
                }

                if (emptyAreaHeight > 0) {
                    Bitmap empty = Bitmap.createBitmap(getWidth(), emptyAreaHeight, Bitmap.Config.ALPHA_8);
                    Canvas tmp = new Canvas(empty);
                    tmp.drawColor(Color.YELLOW);
                    bitmaps.add(empty);
                }
            }

            Bitmap remaining = Bitmap.createBitmap(offscreenBitmap, 0, cursor + getScrollY(), getWidth(), getHeight() - cursor - getScrollY());
            bitmaps.add(remaining);

            Bitmap finalBitmap = mergeBitmaps(bitmaps.toArray(new Bitmap[0]));
            canvas.drawBitmap(finalBitmap, 0, 0, paint);

            finalBitmap.recycle();
        }
//        super.onDraw(canvas);
        if (penBitmap != null) {
            canvas.drawColor(Color.TRANSPARENT);
            canvas.drawBitmap(penBitmap, 0f, 0f, paint);
        }
    }

    private Bitmap mergeBitmaps(Bitmap... bitmaps) {
        Bitmap res = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(res);
        int cursor = 0;
        for (final Bitmap bitmap : bitmaps) {
            c.drawBitmap(bitmap, 0, cursor, null);
            cursor += bitmap.getHeight();
            bitmap.recycle();
        }
        return res;
    }
}
