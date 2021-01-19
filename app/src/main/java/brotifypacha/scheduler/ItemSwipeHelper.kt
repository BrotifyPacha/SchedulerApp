package brotifypacha.scheduler

import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import androidx.recyclerview.widget.RecyclerView

class ItemSwipeHelper(val listener:OnSwipeListener): RecyclerView.OnItemTouchListener{

    companion object {
        const val SWIPE_LEFT = 0
        const val SWIPE_RIGHT = 1
        const val ACTIVE_POINTER_ID_NONE = -1
    }
    interface OnSwipeListener {
        fun getSwipeThreshold(): Float
        fun getSwipeDirection(): Int
        fun onMove(holder: RecyclerView.ViewHolder, dx: Float)
        fun onUp(holder: RecyclerView.ViewHolder, isSwipeSuccessful: Boolean)
    }

    private var eventStartTime: Long = 0
    private val TAG: String = ItemSwipeHelper::class.java.simpleName


    private var initX: Float = 0f
    private var initY: Float = 0f
    private var selectedHolder: RecyclerView.ViewHolder? = null
    private var isInitialSwipeDirectionCorrect: Boolean? = null
    private var currentPointerId = ACTIVE_POINTER_ID_NONE

    private fun getViewHolderUnder(rv: RecyclerView, x: Float, y: Float): RecyclerView.ViewHolder?{
        val view = rv.findChildViewUnder(x, y) ?: return null
        return rv.getChildViewHolder(view)
    }

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        eventStartTime = System.nanoTime()
        val action = e.actionMasked
        when (action) {
            MotionEvent.ACTION_DOWN -> {
//                Log.d("TAG", "{onIntercept ACTION_DOWN}")
                currentPointerId = e.getPointerId(0)
                initX = e.x
                initY = e.y
                selectedHolder = getViewHolderUnder(rv, initX, initY)
//                obtainVelocityTracker()
            }
            MotionEvent.ACTION_MOVE -> {
//                Log.d("TAG", "{onIntercept ACTION_MOVE}")
                if (rv.scrollState == RecyclerView.SCROLL_STATE_DRAGGING || rv.scrollState == RecyclerView.SCROLL_STATE_SETTLING) return false
                when (listener.getSwipeDirection()) {
                    SWIPE_LEFT -> {
                        if (isInitialSwipeDirectionCorrect == null) {
                            if (e.x - initX > 0) return false
                            val absDx = Math.abs(initX - e.x)
                            val absDy = Math.abs(initY - e.y)
                            if (absDy > absDx) return false
                            isInitialSwipeDirectionCorrect = true
                        }
                    }
                    SWIPE_RIGHT -> {
                        if (isInitialSwipeDirectionCorrect == null) {
                            if (e.x - initX < 0) return false
                            val absDx = Math.abs(initX - e.x)
                            val absDy = Math.abs(initY - e.y)
                            if (absDy > absDx) return false
                            isInitialSwipeDirectionCorrect = true
                        }
                    }
                }
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
//                Log.d("TAG", "{onIntercept ACTION_UP}")
                currentPointerId = ACTIVE_POINTER_ID_NONE
                selectedHolder = null
                isInitialSwipeDirectionCorrect = null
                return false
            }
        }
        if (System.nanoTime() - eventStartTime < 50 * 1000000 && Math.abs(e.x - initX) < 3f) return false
        return selectedHolder != null
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
        val action = e.actionMasked
        when (action) {
            MotionEvent.ACTION_MOVE -> {
//                Log.d("TAG", "{onTouch ACTION_MOVE}")
                val dX = e.x - initX
                listener.onMove(selectedHolder!!, dX)
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
//                Log.d("TAG", "{onTouch ACTION_UP}")
                val dx = e.x - initX
                if (listener.getSwipeDirection() == SWIPE_LEFT) {
                    listener.onUp(selectedHolder!!, dx < -listener.getSwipeThreshold())
                } else if (listener.getSwipeDirection() == SWIPE_RIGHT) {
                    listener.onUp(selectedHolder!!, dx > listener.getSwipeThreshold())
                }
                selectedHolder = null
                isInitialSwipeDirectionCorrect = null
            }
        }
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        //Do nothing
    }
}