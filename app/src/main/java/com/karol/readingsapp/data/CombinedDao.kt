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
    val translationCode: String,
)

data class BibleTranslation(
    val code: String,
    val language: String,
    val name: String,
)

@Dao
interface CombinedDao {
    @SkipQueryVerification
    @Query("SELECT * FROM bible_db.translations")
    suspend fun getAvailableTranslations(): List<BibleTranslation>

    @SkipQueryVerification
    @Query(
        """
        SELECT s.date, s.bookName, s.chapter, v.text, s.readingType, v.translation_code AS translationCode
        FROM reading_schedule AS s
        INNER JOIN bible_db.verses AS v 
        ON v.book_id = s.bookId AND v.chapter = s.chapter
        -- Must filter by translationCode to avoid duplicate verses from different translations
        WHERE s.date = :targetDate AND v.translation_code = :translationCode
        """,
    )
    suspend fun getReadingForDate(targetDate: String, translationCode: String): List<TargetReadingDetails>
}
