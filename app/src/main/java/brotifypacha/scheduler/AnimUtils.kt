package brotifypacha.scheduler

import android.view.View

class AnimUtils {
    companion object{

        fun animateViewWiggle(v: View){
            val by = (v.width*0.007).toFloat()
            v.animate()
                .translationXBy(by)
                .setDuration(50)
                .withEndAction {
                    v.animate()
                        .translationXBy(-2 * by)
                        .setDuration(65)
                        .withEndAction{
                            v.animate()
                                .translationXBy(by)
                                .setDuration(30)
                                .start()
                        }
                        .start()
                }
                .start()
        }
    }

}