package brotifypacha.scheduler.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewPropertyAnimator
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class ShrinkOnScrollBehavior(context: Context?, attrs: AttributeSet?) : CoordinatorLayout.Behavior<ExtendedFloatingActionButton>(context, attrs) {

    private val TAG: String = ShrinkOnScrollBehavior::class.java.simpleName

    private var bottomAppBar : BottomAppBar? = null
    private val center_transl : Float = 0f
    private var end_transl : Float = 0f

    private var animator : ViewPropertyAnimator? = null

    override fun layoutDependsOn(parent: CoordinatorLayout, child: ExtendedFloatingActionButton, dependency: View): Boolean {
        if (dependency is BottomAppBar) {
            bottomAppBar = dependency
            return true
        } else return false
    }

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: ExtendedFloatingActionButton, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: ExtendedFloatingActionButton, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int, consumed: IntArray) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, consumed)
        if (end_transl == 0f) {
            end_transl += child.pivotX
        }
        if (dyConsumed > 0) {
            if (animator != null) animator!!.cancel()
            animator = child.animate().translationX(end_transl)
            animator!!.start()
            child.shrink()
        } else if (dyConsumed < 0) {
            if (animator != null) animator!!.cancel()
            animator = child.animate().translationX(center_transl)
            animator!!.start()
            child.extend()
        }
    }
}