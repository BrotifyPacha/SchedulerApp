package brotifypacha.scheduler.view_schedule_fragment

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import brotifypacha.scheduler.R
import brotifypacha.scheduler.databinding.LessonItemBinding

class LessonsAdapter(var lessons: List<String>): RecyclerView.Adapter<LessonsAdapter.LessonViewHolder>(){

    val TAG = LessonsAdapter::class.java.simpleName

    fun setData(lessons: List<String>){
        this.lessons = lessons
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder {
        return LessonViewHolder.from(parent)
    }

    override fun getItemCount(): Int {
        return lessons.size
    }
    override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
        holder.bind(position+1, lessons[position])
    }

    class LessonViewHolder(val binding: LessonItemBinding): RecyclerView.ViewHolder(binding.root){
        companion object {
            fun from(parent: ViewGroup): LessonViewHolder{
                val inflater: LayoutInflater = LayoutInflater.from(parent.context)
                val bind: LessonItemBinding = DataBindingUtil.inflate(inflater, R.layout.lesson_item, parent, false)
                return LessonViewHolder(bind)
            }
        }

        fun bind(num: Int, name: String){
            binding.setNumber(num.toString())
            binding.setName(name)
            binding.executePendingBindings()
        }
    }

}