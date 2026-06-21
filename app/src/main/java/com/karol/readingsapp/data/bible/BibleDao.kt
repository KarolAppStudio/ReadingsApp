package com.karol.readingsapp.data.bible

import androidx.room.Dao
import androidx.room.Query

@Dao
interface BibleDao {
    @Query("SELECT * FROM translations")
    suspend fun getAvailableTranslations(): List<TranslationEntity>

    @Query(
        """
        SELECT :date AS date, :bookId AS bookId, :bookName AS bookName, chapter, verse AS verseId, text, :readingType AS readingType, translation_code AS translationCode
        FROM verses
        WHERE book_id = :bookId AND chapter IN (:chapters) AND translation_code = :translationCode
        """,
    )
    suspend fun getVersesForReading(
        date: String,
        bookId: Int,
        chapters: List<Int>,
        readingType: String,
        translationCode: String,
        bookName: String,
    ): List<TargetReadingDetails>
}
