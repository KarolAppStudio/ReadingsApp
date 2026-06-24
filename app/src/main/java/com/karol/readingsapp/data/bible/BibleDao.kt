package com.karol.readingsapp.data.bible

import androidx.room.Dao
import androidx.room.Query

@Dao
interface BibleDao {
    @Query("SELECT * FROM translations")
    suspend fun getAvailableTranslations(): List<TranslationEntity>

    @Query("SELECT * FROM books ORDER BY sort_order ASC")
    suspend fun getAllBooks(): List<BookEntity>

    @Query("SELECT MAX(chapter) FROM verses WHERE book_id = :bookId")
    suspend fun getChapterCount(bookId: Int): Int

    @Query("SELECT MAX(verse) FROM verses WHERE book_id = :bookId AND chapter = :chapter")
    suspend fun getVerseCount(
        bookId: Int,
        chapter: Int,
    ): Int

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

    @Query(
        """
        SELECT '' AS date, :bookId AS bookId, '' AS bookName, chapter, verse AS verseId, text, 'Bible' AS readingType, translation_code AS translationCode
        FROM verses
        WHERE book_id = :bookId AND chapter = :chapter AND translation_code = :translationCode
        ORDER BY verse ASC
        """,
    )
    suspend fun getChapterVerses(
        bookId: Int,
        chapter: Int,
        translationCode: String,
    ): List<TargetReadingDetails>
}
