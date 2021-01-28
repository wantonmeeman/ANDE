package com.example.ca1;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FloatingButtonBehavior extends FloatingActionButton.Behavior {
    public FloatingButtonBehavior(){
        super();
    };
    public FloatingButtonBehavior(Context context, AttributeSet attrs) {
        super();
    }

//    @Override
//    public void onNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
//        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
//        Log.i("xxd","xxxxdd");
//        if (dyConsumed > 0) {
//            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
//            int fab_bottomMargin = layoutParams.bottomMargin;
//            child.animate().translationY(child.getHeight() + fab_bottomMargin).setInterpolator(new LinearInterpolator()).start();
//        } else if (dyConsumed < 0) {
//            child.animate().translationY(0).setInterpolator(new LinearInterpolator()).start();
//        }
//    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
        if (dyUnconsumed < 0) {
            showBottomNavigationView(child);
        } else if (dyUnconsumed > 0) {
            hideBottomNavigationView(child);
        }
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View directTargetChild, View target, int nestedScrollAxes) {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    private void hideBottomNavigationView(FloatingActionButton view) {
        view.animate().translationY(view.getHeight());
    }

    private void showBottomNavigationView(FloatingActionButton view) {
        view.animate().translationY(0);
    }
}