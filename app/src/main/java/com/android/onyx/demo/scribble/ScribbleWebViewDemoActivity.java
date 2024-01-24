package com.android.onyx.demo.scribble;

import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import com.android.onyx.demo.utils.TouchUtils;
import com.onyx.android.demo.R;
import com.onyx.android.demo.databinding.ActivityScribbleWebviewStylusDemoBinding;
import com.onyx.android.sdk.api.device.epd.EpdController;
import com.onyx.android.sdk.data.note.TouchPoint;
import com.onyx.android.sdk.pen.NeoFountainPen;
import com.onyx.android.sdk.pen.RawInputCallback;
import com.onyx.android.sdk.pen.TouchHelper;
import com.onyx.android.sdk.pen.data.TouchPointList;
import com.onyx.android.sdk.utils.NumberUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by seeksky on 2018/4/26.
 */

public class ScribbleWebViewDemoActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();
    private final float STROKE_WIDTH = 3.0f;

    private TouchHelper touchHelper;
    //    private ActivityScribbleWebviewStylusDemoBinding binding;
    private MyWebView view;
    private Button buttonPen;
    private RelativeLayout layout;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        binding = DataBindingUtil.setContentView(this, R.layout.activity_scribble_webview_stylus_demo);
        getSupportActionBar().hide();

//        binding.buttonEraser.setOnClickListener(this);

        layout = new RelativeLayout(this);
        setContentView(layout);

        initWebView();

        this.buttonPen = new Button(this);
        this.buttonPen.setOnClickListener(this);

        RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        relativeParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        relativeParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        buttonPen.setText("Pen");

        layout.addView(buttonPen, relativeParams);
    }

    @Override
    protected void onResume() {
        touchHelper.setRawDrawingEnabled(true);
        super.onResume();
    }

    @Override
    protected void onPause() {
        touchHelper.setRawDrawingEnabled(false);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        touchHelper.closeRawDrawing();
        super.onDestroy();
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }
    }

    private void initWebView() {
        this.view = new MyWebView(this);

        EpdController.setWebViewContrastOptimize(this.view, true);

        this.layout.addView(this.view, new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        touchHelper = TouchHelper.create(this.view, callback);
        this.view.setWebViewClient(new MyWebViewClient());
        this.view.getSettings().setJavaScriptEnabled(true);

        this.view.loadUrl("file:///android_asset/index.html");

        this.view.post(() -> initTouchHelper());

        this.view.setOnTouchListener((v, event) -> {
            Log.d(TAG, "surfaceView.setOnTouchListener - onTouch::action - " + event.getAction());
            boolean res = touchHelper.onTouchEvent(event);
            if (event.getAction() == MotionEvent.ACTION_MOVE && touchHelper.isRawDrawingInputEnabled()) {
                return true;
            }
            return res;
        });
    }

    private void initTouchHelper() {
        List<Rect> exclude = new ArrayList<>();
        //exclude.add(getRelativeRect(binding.surfaceview, binding.buttonEraser));
        exclude.add(getRelativeRect(this.view, this.buttonPen));
        Rect limit = new Rect();
        this.view.getLocalVisibleRect(limit);
        touchHelper.setStrokeWidth(STROKE_WIDTH)
                   .setLimitRect(limit, exclude);
        touchHelper.openRawDrawing();
        touchHelper.setStrokeStyle(TouchHelper.STROKE_STYLE_PENCIL);
    }

    public Rect getRelativeRect(final View parentView, final View childView) {
        int[] parent = new int[2];
        int[] child = new int[2];
        parentView.getLocationOnScreen(parent);
        childView.getLocationOnScreen(child);
        Rect rect = new Rect();
        childView.getLocalVisibleRect(rect);
        rect.offset(child[0] - parent[0], child[1] - parent[1]);
        return rect;
    }

    @Override
    public void onClick(View v) {
        if (v.equals(this.buttonPen)) {
            if (touchHelper.isRawDrawingInputEnabled()) {
                touchHelper.setRawDrawingEnabled(false);
            } else {
                touchHelper.setRawDrawingEnabled(true);
            }
        }
//        else if (v.equals(binding.buttonEraser)) {
//            touchHelper.setRawDrawingEnabled(false);
//            binding.surfaceview.reload();
//            return;
//        }
    }

    private RawInputCallback callback = new RawInputCallback() {
        @Override
        public void onBeginRawDrawing(boolean b, TouchPoint touchPoint) {
            Log.d(TAG, "onBeginRawDrawing");
            Log.d(TAG, touchPoint.getX() + ", " + touchPoint.getY());
            TouchUtils.disableFingerTouch(getApplicationContext());
        }

        @Override
        public void onEndRawDrawing(boolean b, TouchPoint touchPoint) {
            Log.d(TAG, "onEndRawDrawing");
            TouchUtils.enableFingerTouch(getApplicationContext());
        }

        @Override
        public void onRawDrawingTouchPointMoveReceived(TouchPoint touchPoint) {
            Log.d(TAG, "onRawDrawingTouchPointMoveReceived");
            Log.d(TAG, touchPoint.getX() + ", " + touchPoint.getY());
        }

        @Override
        public void onRawDrawingTouchPointListReceived(TouchPointList touchPointList) {
            Log.d(TAG, "onRawDrawingTouchPointListReceived");
            view.drawPointsToBitmap(touchPointList.getPoints());
        }

        @Override
        public void onBeginRawErasing(boolean b, TouchPoint touchPoint) {
            Log.d(TAG, "onBeginRawErasing");
        }

        @Override
        public void onEndRawErasing(boolean b, TouchPoint touchPoint) {
            Log.d(TAG, "onEndRawErasing");
        }

        @Override
        public void onRawErasingTouchPointMoveReceived(TouchPoint touchPoint) {
            Log.d(TAG, "onRawErasingTouchPointMoveReceived");
        }

        @Override
        public void onRawErasingTouchPointListReceived(TouchPointList touchPointList) {
            Log.d(TAG, "onRawErasingTouchPointListReceived");
        }
    };
}
