package com.android.onyx.demo.scribble;

import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import com.android.onyx.demo.utils.TouchUtils;
import com.onyx.android.demo.R;
import com.onyx.android.sdk.api.device.epd.EpdController;
import com.onyx.android.sdk.data.note.TouchPoint;
import com.onyx.android.sdk.pen.RawInputCallback;
import com.onyx.android.sdk.pen.TouchHelper;
import com.onyx.android.sdk.pen.data.TouchPointList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by seeksky on 2018/4/26.
 */

public class ScribbleWebViewDemoActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName();
    private final float STROKE_WIDTH = 3.0f;

    private TouchHelper touchHelper;

    private WebView view;
    private ImageButton buttonPen;
    private ImageButton buttonAddEmptyArea;

    private WebViewContainer webViewContainer;
    private Button buttonSend;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

        RelativeLayout layout = new RelativeLayout(this);
        setContentView(layout);

        initWebView();

        buttonPen = new ImageButton(this);

        buttonPen.setOnClickListener(v -> {
            final boolean rawDrawingInputEnabled = touchHelper.isRawDrawingInputEnabled();
            if (rawDrawingInputEnabled) {
                touchHelper.setRawDrawingEnabled(false);
                buttonPen.setImageResource(R.drawable.baseline_edit_24);
            } else {
                touchHelper.setRawDrawingEnabled(true);
                buttonPen.setImageResource(R.drawable.baseline_swipe_vertical_black_24);
            }
        });

        buttonPen.setId(R.id.button_pen);
        buttonPen.setImageResource(R.drawable.baseline_edit_24);
        buttonPen.setBackgroundResource(R.drawable.outline_circle_24);

        buttonAddEmptyArea = new ImageButton(this);
        buttonAddEmptyArea.setImageResource(R.drawable.baseline_add_24);
        buttonAddEmptyArea.setBackgroundResource(R.drawable.outline_circle_24);

        webViewContainer = new WebViewContainer(this, view, touchHelper, buttonPen);

        buttonAddEmptyArea.setOnClickListener(v -> webViewContainer.switchAddEmptyAreaMode());

        layout.addView(this.view, new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        layout.addView(webViewContainer, new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        RelativeLayout.LayoutParams relativeParamsPen = new RelativeLayout.LayoutParams(150, 150);
        relativeParamsPen.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        relativeParamsPen.addRule(RelativeLayout.ALIGN_PARENT_START);
        layout.addView(buttonPen, relativeParamsPen);

        RelativeLayout.LayoutParams relativeParamsAddEmptyArea = new RelativeLayout.LayoutParams(150, 150);
        relativeParamsAddEmptyArea.addRule(RelativeLayout.ABOVE, R.id.button_pen);
        layout.addView(buttonAddEmptyArea, relativeParamsAddEmptyArea);

        buttonSend = new Button(this);
        buttonSend.setText("Изпрати до учител");

        RelativeLayout.LayoutParams relativeParamsSend = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,  ViewGroup.LayoutParams.WRAP_CONTENT);
        relativeParamsSend.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        relativeParamsSend.addRule(RelativeLayout.ALIGN_PARENT_END);
        layout.addView(buttonSend, relativeParamsSend);
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

        touchHelper = TouchHelper.create(this.view, callback);
        view.setWebViewClient(new MyWebViewClient());
        view.getSettings().setJavaScriptEnabled(true);

        view.loadUrl("file:///android_asset/index.html?page=32");

        view.post(this::initTouchHelper);
        view.setVisibility(View.INVISIBLE);
    }

    private void initTouchHelper() {
        List<Rect> exclude = new ArrayList<>();
        //exclude.add(getRelativeRect(binding.surfaceview, binding.buttonEraser));
        exclude.add(getRelativeRect(this.view, this.buttonPen));
        exclude.add(getRelativeRect(this.view, this.buttonAddEmptyArea));
        exclude.add(getRelativeRect(this.view, this.buttonSend));

        Rect limit = new Rect();
        this.view.getLocalVisibleRect(limit);
        touchHelper.setStrokeWidth(STROKE_WIDTH)
                   .setLimitRect(limit, exclude)
                   .setStrokeColor(Color.BLACK);
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

    private final RawInputCallback callback = new RawInputCallback() {
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
            touchHelper.setRawDrawingRenderEnabled(false);
        }

        @Override
        public void onEndRawErasing(boolean b, TouchPoint touchPoint) {
            touchHelper.setRawDrawingRenderEnabled(true);
        }

        @Override
        public void onRawErasingTouchPointMoveReceived(TouchPoint touchPoint) {
            Log.d(TAG, "onRawErasingTouchPointMoveReceived");
        }

        @Override
        public void onRawErasingTouchPointListReceived(TouchPointList touchPointList) {
            webViewContainer.deletePaths(touchPointList.getPoints());
        }
    };
}
