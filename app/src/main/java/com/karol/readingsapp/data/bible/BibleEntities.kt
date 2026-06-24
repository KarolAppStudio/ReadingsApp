package com.karol.readingsapp.data.bible

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "osis_code") val osisCode: String,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
    @ColumnInfo(name = "testament") val testament: String,
)

@Entity(tableName = "translations")
data class TranslationEntity(
    @PrimaryKey val code: String,
    val language: String,
    val name: String,
)

@Entity(
    tableName = "verses",
    // Primary keys ordered matching primaryKeyPosition: translation_code(1), book_id(2), chapter(3), verse(4)
    primaryKeys = ["translation_code", "book_id", "chapter", "verse"],
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class, // Linked to 'books' table
            parentColumns = ["id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION,
        ),
        ForeignKey(
            entity = TranslationEntity::class, // Linked to 'translations' table
            parentColumns = ["code"],
            childColumns = ["translation_code"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION,
        ),
    ],
)
data class Verse(
    @ColumnInfo(name = "book_id") val bookId: Int,
    @ColumnInfo(name = "chapter") val chapter: Int,
    @ColumnInfo(name = "text") val text: String,
    @ColumnInfo(name = "translation_code") val translationCode: String,
    @ColumnInfo(name = "verse") val verse: Int,
)

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
