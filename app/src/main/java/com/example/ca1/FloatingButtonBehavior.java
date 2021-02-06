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
//This is a custom class, to move the FloatingActionButton down in conjunction with the BottomNavbar
//Note that it just moves down, it is not meant to hide Button
public class FloatingButtonBehavior extends FloatingActionButton.Behavior {
    public FloatingButtonBehavior(){
        super();
    };
    public FloatingButtonBehavior(Context context, AttributeSet attrs) {
        super();
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
        if (dyConsumed < 0) {
            //If user scrolls Up
            showFAB(child);
        } else if (dyConsumed > 0) {
            //If user scrolls down
            hideFAB(child);
        }
    }
    /*We can actually make the Floating Action bar disappear, but its counter productive, so we shall not do it*/
    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View directTargetChild, View target, int nestedScrollAxes) {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    private void hideFAB(FloatingActionButton view) {
        //view.animate().translationY(view.getHeight()*3);
        view.animate().translationY(view.getHeight());
    }

    private void showFAB(FloatingActionButton view) {
        view.animate().translationY(0);
    }
}