package com.karol.readingsapp.data.plan

import androidx.room.Dao
import androidx.room.Query

@Dao
interface ReadingPlanDao {
    @Query("SELECT * FROM reading_plan WHERE day_of_year = :dayOfYear LIMIT 1")
    suspend fun getReadingPlanByDay(dayOfYear: Int): ReadingPlanEntity?

    @Query("SELECT * FROM reading_plan WHERE day_of_year BETWEEN :startDay AND :endDay ORDER BY day_of_year ASC")
    suspend fun getReadingPlanInRange(startDay: Int, endDay: Int): List<ReadingPlanEntity>
}
