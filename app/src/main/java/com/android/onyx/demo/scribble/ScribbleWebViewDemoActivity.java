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
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
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
    private WebView view;
    private Button buttonPen;
    private Button buttonAddEmptyArea;

    private RelativeLayout layout;
    private WebViewContainer webViewContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        binding = DataBindingUtil.setContentView(this, R.layout.activity_scribble_webview_stylus_demo);
        getSupportActionBar().hide();

//        binding.buttonEraser.setOnClickListener(this);

        layout = new RelativeLayout(this);
        setContentView(layout);

        initWebView();

        view.setVisibility(View.INVISIBLE);
        webViewContainer = new WebViewContainer(this, view, touchHelper);

        this.layout.addView(webViewContainer, new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        this.buttonPen = new Button(this);
        this.buttonPen.setOnClickListener(v -> touchHelper.setRawDrawingEnabled(!touchHelper.isRawDrawingInputEnabled()));

        buttonPen.setId(R.id.button_pen);
        buttonPen.setText("Pen");

        RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        relativeParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        relativeParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        layout.addView(buttonPen, relativeParams);

        this.buttonAddEmptyArea = new Button(this);
        this.buttonAddEmptyArea.setOnClickListener(v -> webViewContainer.switchAddEmptyAreaMode());

        relativeParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        relativeParams.addRule(RelativeLayout.ABOVE, R.id.button_pen);
        buttonAddEmptyArea.setText("+");

        layout.addView(buttonAddEmptyArea, relativeParams);
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
            webViewContainer.onPageFinished(view);
        }
    }

    private void initWebView() {
        this.view = new WebView(this);

        EpdController.setWebViewContrastOptimize(this.view, true);

        this.layout.addView(this.view, new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        touchHelper = TouchHelper.create(this.view, callback);
        view.setWebViewClient(new MyWebViewClient());
        view.getSettings().setJavaScriptEnabled(true);

        view.loadUrl("file:///android_asset/index.html?page=6");

        view.post(this::initTouchHelper);
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
            webViewContainer.drawPointsToBitmap(touchPointList.getPoints());
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
