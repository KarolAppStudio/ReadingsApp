package com.karol.readingsapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reading_plan")
data class ReadingPlan(
    @PrimaryKey @ColumnInfo(name = "day_of_year") val dayOfYear: Int,
    @ColumnInfo(name = "track_1_book") val track1Book: String,
    @ColumnInfo(name = "track_1_chapters") val track1Chapters: String,
    @ColumnInfo(name = "track_2_book") val track2Book: String,
    @ColumnInfo(name = "track_2_chapters") val track2Chapters: String,
    @ColumnInfo(name = "track_3_book") val track3Book: String,
    @ColumnInfo(name = "track_3_chapters") val track3Chapters: String,
)
