package com.karol.readingsapp.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.SkipQueryVerification

data class TargetReadingDetails(
    val date: String,
    val bookName: String,
    val chapter: Int,
    val text: String,
    val readingType: String,
)

@Dao
interface CombinedDao {
    @SkipQueryVerification
    @Query(
        """
        SELECT s.date, s.bookName, s.chapter, v.text, s.readingType
        FROM reading_schedule AS s
        INNER JOIN bible_db.verses AS v 
        ON v.book_id = s.bookId AND v.chapter = s.chapter
        WHERE s.date = :targetDate
        """,
    )
    suspend fun getReadingForDate(targetDate: String): List<TargetReadingDetails>
}
