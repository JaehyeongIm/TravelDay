package com.example.travelday_2

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
//데이터 베이스 참조 클래스
class DBRef {
    companion object {
        private val database = Firebase.database

        val contentRef = database.getReference("content")
        val userRef = database.getReference("users")
        fun writeDataToDatabase(userId: String, country: String) {
            val countryRef = userRef.child(userId).child(country)
            countryRef.setValue(country)
        }

        fun writeDataToDatabase(userId: String, country: String, date: String) {
            val dateRef = userRef.child(userId).child(country).child(date)
            dateRef.setValue(date)
        }

        fun writeDataToDatabase(
            userId: String,
            country: String,
            date: String,
            time: String,
            task: String,
            color: String
        ) {
            val taskRef = userRef.child(userId).child(country).child(date).child("tasklist").push()
            taskRef.child("time").setValue(time)
            taskRef.child("task").setValue(task)
            taskRef.child("color").setValue(color)
        }
    }
}