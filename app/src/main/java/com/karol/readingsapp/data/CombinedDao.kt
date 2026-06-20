package com.karol.readingsapp.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
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

data class SimpleReading(
    val date: String,
    val bookName: String,
    val chapter: Int,
    val readingType: String,
)

@Entity(tableName = "translations")
data class BibleTranslation(
    @PrimaryKey val code: String,
    val language: String,
    val name: String,
)

@Entity(tableName = "verses", primaryKeys = ["translation_code", "book_id", "chapter", "verse_id"])
data class Verse(
    @ColumnInfo(name = "translation_code") val translationCode: String,
    @ColumnInfo(name = "book_id") val bookId: Int,
    val chapter: Int,
    @ColumnInfo(name = "verse_id") val verseId: Int,
    val text: String
)

@Dao
interface CombinedDao {
    @SkipQueryVerification
    @Query("SELECT * FROM bible_db.translations")
    suspend fun getAvailableTranslations(): List<BibleTranslation>

    @SkipQueryVerification
    @Query("SELECT * FROM bible_db.verses LIMIT 1")
    suspend fun getAnyVerse(): Verse?

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

    @SkipQueryVerification
    @Query(
        """
        SELECT s.date, s.bookName, s.chapter, v.text, s.readingType, v.translation_code AS translationCode
        FROM reading_schedule AS s
        INNER JOIN bible_db.verses AS v 
        ON v.book_id = s.bookId AND v.chapter = s.chapter
        WHERE s.date LIKE :monthPattern AND v.translation_code = :translationCode
        ORDER BY s.date ASC, s.readingType ASC
        """
    )
    suspend fun getReadingsForMonth(monthPattern: String, translationCode: String): List<TargetReadingDetails>

    @Query("SELECT date, bookName, chapter, readingType FROM reading_schedule WHERE date LIKE :monthPattern ORDER BY date ASC, readingType ASC")
    suspend fun getSimpleReadingsForMonth(monthPattern: String): List<SimpleReading>
}
