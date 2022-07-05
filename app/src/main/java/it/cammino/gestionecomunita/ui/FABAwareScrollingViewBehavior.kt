package it.cammino.gestionecomunita.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

@Suppress("unused")
class FABAwareScrollingViewBehavior : AppBarLayout.ScrollingViewBehavior {
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor() : super()

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        return super.layoutDependsOn(
            parent,
            child,
            dependency
        ) || dependency is ExtendedFloatingActionButton
    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout, child: View,
        directTargetChild: View, target: View, nestedScrollAxes: Int, type: Int
    ): Boolean {
        // Ensure we react to vertical scrolling
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL || super.onStartNestedScroll(
            coordinatorLayout,
            child,
            directTargetChild,
            target,
            nestedScrollAxes,
            type
        )
    }

    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
        super.onNestedScroll(
            coordinatorLayout,
            child,
            target,
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            type,
            consumed
        )
        if (dyConsumed > 0) {
            // User scrolled down -> hide the FAB
            val dependencies = coordinatorLayout.getDependencies(child)
            dependencies
                .filterIsInstance<ExtendedFloatingActionButton>()
                .forEach {
                    it.shrink()
                }
        } else if (dyConsumed < 0) {
            // User scrolled up -> show the FAB
            val dependencies = coordinatorLayout.getDependencies(child)
            dependencies
                .filterIsInstance<ExtendedFloatingActionButton>()
                .forEach { it.extend() }
        }
    }
}
