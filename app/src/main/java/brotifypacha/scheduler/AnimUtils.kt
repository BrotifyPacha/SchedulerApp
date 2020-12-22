package brotifypacha.scheduler

import android.view.View
import android.widget.TextView

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

        fun TextView.animateTextViewChange(text: String){
            val duration: Long = 250L
            this.animate()
                .alpha(0f)
                .setDuration( if (this.text.isBlank()) 0L else duration )
                .withEndAction{
                    this.text = text
                    this.animate()
                        .alpha(1f)
                        .setDuration( if (text.isBlank()) 0L else duration )
                        .start()
                }
                .start()

        }
    }

}