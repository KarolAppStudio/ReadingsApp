package com.karol.readingsapp.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.SkipQueryVerification

data class TargetReadingDetails(
    val date: String,
    val bookId: Int,
    val bookName: String,
    val chapter: Int,
    val verseId: Int,
    val text: String,
    val readingType: String,
    val translationCode: String,
)

data class SimpleReading(
    val date: String,
    val bookId: Int,
    val bookName: String,
    val chaptersStr: String,
    val readingType: String,
)

@Entity(tableName = "translations")
data class BibleTranslation(
    @PrimaryKey val code: String,
    val language: String,
    val name: String,
)

@Entity(tableName = "verses", primaryKeys = ["translation_code", "book_id", "chapter", "verse"])
data class Verse(
    @ColumnInfo(name = "translation_code") val translationCode: String,
    @ColumnInfo(name = "book_id") val bookId: Int,
    val chapter: Int,
    @ColumnInfo(name = "verse") val verseId: Int,
    val text: String,
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
    @Query("SELECT DISTINCT book_id FROM bible_db.verses LIMIT 10")
    suspend fun getBookIds(): List<Int>

    @Query("SELECT * FROM reading_plan WHERE day_of_year = :dayOfYear LIMIT 1")
    suspend fun getReadingPlanByDay(dayOfYear: Int): ReadingPlan?

    @Query("SELECT * FROM reading_plan WHERE day_of_year BETWEEN :startDay AND :endDay ORDER BY day_of_year ASC")
    suspend fun getReadingPlanInRange(startDay: Int, endDay: Int): List<ReadingPlan>

    @SkipQueryVerification
    @Query(
        """
        SELECT :date AS date, :bookId AS bookId, :bookName AS bookName, chapter, verse AS verseId, text, :readingType AS readingType, translation_code AS translationCode
        FROM bible_db.verses
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
