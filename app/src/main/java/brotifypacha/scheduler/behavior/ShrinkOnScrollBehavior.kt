package brotifypacha.scheduler.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewPropertyAnimator
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class ShrinkOnScrollBehavior(context: Context?, attrs: AttributeSet?) : CoordinatorLayout.Behavior<ExtendedFloatingActionButton>(context, attrs) {

    private val TAG: String = ShrinkOnScrollBehavior::class.java.simpleName

    private var bottomAppBar : BottomAppBar? = null
    private val centerTranslation : Float = 0f
    private var endTranslation : Float = 0f
    private var scrollDirection : Boolean? = null

    private var animator : ViewPropertyAnimator? = null

    /*
     * Calculating fab's end translations value based on layout direction
     */
    override fun onLayoutChild(parent: CoordinatorLayout, child: ExtendedFloatingActionButton, layoutDirection: Int): Boolean {
        if (endTranslation == 0f) {
            if (layoutDirection == ViewCompat.LAYOUT_DIRECTION_LTR){
                endTranslation += child.pivotX
            } else {
                endTranslation -= child.pivotX
            }
        }
        return super.onLayoutChild(parent, child, layoutDirection)
    }

    override fun layoutDependsOn(parent: CoordinatorLayout, child: ExtendedFloatingActionButton, dependency: View): Boolean {
        if (dependency.isScrollContainer) {
        dependency.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            // if upon layout change scrollContainer isnt able to scroll we show bottomAppBar and extend FAB
            if (!dependency.canScrollVertically(-1) && !dependency.canScrollVertically(1)) {
                maybeAnimateEFABtoExtend(child)
                bottomAppBar?.performShow()
            }
        }
        }
        if (dependency is BottomAppBar) {
            bottomAppBar = dependency
        }
        return false
    }

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: ExtendedFloatingActionButton, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: ExtendedFloatingActionButton, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int, consumed: IntArray) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, consumed)
        val currentScrollDirection = dyConsumed > 0
        if (currentScrollDirection != scrollDirection && dyConsumed != 0) {
            if (dyConsumed > 0) {
                maybeAnimateEFABtoShrink(child)
            } else if (dyConsumed < 0) {
                maybeAnimateEFABtoExtend(child)
            }
            scrollDirection = currentScrollDirection
        }
    }

    private fun maybeAnimateEFABtoExtend(efab: ExtendedFloatingActionButton) {
        if (animator != null) animator!!.cancel()
        animator = efab.animate().translationX(centerTranslation)
        animator!!.start()
        efab.extend()
    }
    private fun maybeAnimateEFABtoShrink(efab: ExtendedFloatingActionButton) {
        if (animator != null) animator!!.cancel()
        animator = efab.animate().translationX(endTranslation)
        animator!!.start()
        efab.shrink()
    }


}