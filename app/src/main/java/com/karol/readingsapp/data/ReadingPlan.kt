package com.karol.readingsapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reading_plan")
data class ReadingPlan(
    @PrimaryKey 
    @ColumnInfo(name = "day_of_year") 
    val day_of_year: Int?,
    
    @ColumnInfo(name = "track_1") 
    val track_1: String?,
    
    @ColumnInfo(name = "track_2") 
    val track_2: String?,
    
    @ColumnInfo(name = "track_3") 
    val track_3: String?
)
