package com.example.subscriber;

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StudentIdAdapter(private val studentList: List<Student>) : RecyclerView.Adapter<StudentIdAdapter.StudentViewHolder>() {

    // ViewHolder class for each item in the RecyclerView
    class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val studentIdTextView: TextView = itemView.findViewById(R.id.studentIdTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        // Inflate the layout for each item in the RecyclerView
        val view = LayoutInflater.from(parent.context).inflate(R.layout.student_item_layout, parent, false)
        return StudentViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        // Bind the student ID to the TextView in the ViewHolder
        val student = studentList[position]
        holder.studentIdTextView.text = student.id
    }

    override fun getItemCount(): Int {
        return studentList.size
    }
}