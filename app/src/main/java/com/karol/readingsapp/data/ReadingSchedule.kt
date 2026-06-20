package com.karol.readingsapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reading_schedule")
data class ReadingSchedule(
    @PrimaryKey val id: Int,
    val date: String,
    val bookId: Int,
    val bookName: String,
    val chapter: Int,
    val readingType: String, // "First Reading", "Second Reading", "Third Reading"
)
