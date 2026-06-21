package com.karol.readingsapp.data.plan

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reading_plan")
data class ReadingPlanEntity(
    @PrimaryKey 
    @ColumnInfo(name = "day_of_year") 
    val dayOfYear: Int?,
    
    @ColumnInfo(name = "track_1") 
    val track1: String?,
    
    @ColumnInfo(name = "track_2") 
    val track2: String?,
    
    @ColumnInfo(name = "track_3") 
    val track3: String?,
)

data class SimpleReading(
    val date: String,
    val bookId: Int,
    val bookName: String,
    val chaptersStr: String,
    val readingType: String,
)
