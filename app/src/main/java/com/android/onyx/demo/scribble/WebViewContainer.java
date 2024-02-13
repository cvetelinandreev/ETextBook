package com.android.onyx.demo.scribble;

import android.content.Context;
import android.graphics.*;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;
import com.onyx.android.sdk.data.note.TouchPoint;
import com.onyx.android.sdk.pen.TouchHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK;

public class WebViewContainer extends View {
    private final float STROKE_WIDTH = 3.0f;

    private Point mEmptyAreaStart;
    private Point mEmptyAreaEnd;
    private Canvas webViewCanvas;
    private Bitmap webviewBitmap;
    private final WebView webView;

    private final Paint strokesPaint = new Paint();
    private final Paint emptyAreaPaint = new Paint();

    private final List<EmptyArea> emptyAreas = new ArrayList<>();
    boolean addEmptyAreaMode = false;

    private static class EmptyArea {
        private final int y;
        private final int height;

        public EmptyArea(final int y, final int height) {
            this.y = y;
            this.height = height;
        }
    }

    public WebViewContainer(final Context context, WebView webView, TouchHelper touchHelper, ImageButton penButton) {
        super(context);
        this.webView = webView;

//        webView.setOnLongClickListener(v -> true);
//        webView.setLongClickable(false);
//
//        setOnTouchListener((v, event) -> {
//            if (ACTION_CLICK == event.getAction()) {
//                performClick();
//            }
//
//            if (event.getAction() == MotionEvent.ACTION_MOVE && touchHelper.isRawDrawingInputEnabled()) {
//                return true;
//            }
//
//            if (!addEmptyAreaMode) {
//                // TODO offset if empty area
//                boolean res = webView.onTouchEvent(event);
//                invalidate();
//                return res;
//            } else {
//                final Function<Void, Void> runnable = (a) -> {
//                    mEmptyAreaEnd = new Point((int)event.getX(), (int)event.getY());
//
////                    if (!emptyAreas.isEmpty()) {
////                        final EmptyArea lastEmptyArea = emptyAreas.get(emptyAreas.size() - 1);
////
////                        if (lastEmptyArea.y == mEmptyAreaStart.y + webView.getScrollY()) {
////                            emptyAreas.remove(lastEmptyArea);
////                        }
////                    }
//                    // TODO keep the current empty area in a dedicated variable
//                    emptyAreas.clear();
//                    emptyAreas.add(new EmptyArea(mEmptyAreaStart.y + webView.getScrollY(), mEmptyAreaEnd.y - mEmptyAreaStart.y));
//                    invalidate();
//                    return null;
//                };
//                switch (event.getActionMasked()) {
//                    case MotionEvent.ACTION_DOWN:
//                        mEmptyAreaStart = new Point((int)event.getX(), (int)event.getY());
//                        break;
//                    case MotionEvent.ACTION_MOVE:
//                        runnable.apply(null);
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        runnable.apply(null);
//                        addEmptyAreaMode = false;
//                        penButton.callOnClick();
//                        break;
//                    default:
//                        super.onTouchEvent(event);
//                        break;
//                }
//                return true;
//            }
//        });

        emptyAreaPaint.setStyle(Paint.Style.STROKE);
        emptyAreaPaint.setColor(Color.YELLOW);
        emptyAreaPaint.setStrokeWidth(5f);

        strokesPaint.setAntiAlias(true);
        strokesPaint.setStyle(Paint.Style.STROKE);
        strokesPaint.setStrokeWidth(STROKE_WIDTH);
    }

    public void switchAddEmptyAreaMode() {
        addEmptyAreaMode = !addEmptyAreaMode;
    }

    public void onPageFinished(final WebView view) {
        postDelayed(view::invalidate, 300);
        postDelayed(this::invalidate, 300);
    }

    // TODO consider replacing webView.getScrollY.
    //  The webview scroll is different than the actual if there are empty areas
    @Override
    protected void onDraw(final Canvas canvas) {
        if (webViewCanvas == null) {
            webviewBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            webViewCanvas = new Canvas(webviewBitmap);
        }
        webViewCanvas.translate(0, -webView.getScrollY());
        webView.draw(webViewCanvas);
        webViewCanvas.translate(0, webView.getScrollY());

        int webViewCursor = 0;
        int emptyAreasTotalHeight = 0;
        for (final EmptyArea emptyArea : emptyAreas) {
            int emptyAreaHeight;
            if (webView.getScrollY() < emptyArea.y) {
                webViewCursor = emptyArea.y - webView.getScrollY();
                emptyAreaHeight = emptyArea.height;
                Rect rect = new Rect(0, 0, getWidth(), webViewCursor);
                canvas.drawBitmap(webviewBitmap, rect, rect, strokesPaint);
            } else {
                webViewCursor = 0;
                emptyAreaHeight = Math.max(emptyArea.height - (webView.getScrollY() - emptyArea.y), 0);
            }

            if (emptyAreaHeight > 0) {
                canvas.drawRect(0, webViewCursor, getWidth(), webViewCursor + emptyAreaHeight, emptyAreaPaint);
                emptyAreasTotalHeight += emptyAreaHeight;
            }
        }

        Rect remainingSrc = new Rect(0, webViewCursor, getWidth(), getHeight() - emptyAreasTotalHeight);
        Rect remainingDest = new Rect(0, webViewCursor + emptyAreasTotalHeight, getWidth(), getHeight());
        canvas.drawBitmap(webviewBitmap, remainingSrc, remainingDest, strokesPaint);

        for (final Path path : paths) {
            Path pathToDraw = new Path(path);
            pathToDraw.offset(0, -webView.getScrollY());
            canvas.drawPath(pathToDraw, strokesPaint);
        }
    }

    private final List<Path> paths = new ArrayList<>();

    public void drawPointsToBitmap(final List<TouchPoint> points) {
        final Path path = createPath(points);
        paths.add(path);
        invalidate();
    }

    public void deletePaths(final List<TouchPoint> points) {
        final Path eraserPath = createPath(points);

        final Path intersectPath = new Path();
        final List<Path> pathsToDelete = new ArrayList<>();
        for (final Path path : paths) {
            intersectPath.op(path, eraserPath, Path.Op.INTERSECT);
            if (!intersectPath.isEmpty()) {
                pathsToDelete.add(path);
            }
        }

        paths.removeAll(pathsToDelete);
    }

    private Path createPath(final List<TouchPoint> points) {
        Path path = new Path();
        PointF prePoint = new PointF(points.get(0).x, points.get(0).y);
        path.moveTo(prePoint.x, prePoint.y);
        for (TouchPoint point : points) {
            path.quadTo(prePoint.x, prePoint.y, point.x, point.y);
            prePoint.x = point.x;
            prePoint.y = point.y;
        }
        path.offset(0, webView.getScrollY());
        return path;
    }
}
