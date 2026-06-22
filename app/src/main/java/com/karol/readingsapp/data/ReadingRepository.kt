package com.karol.readingsapp.data

import com.karol.readingsapp.data.bible.BibleDao
import com.karol.readingsapp.data.bible.BookEntity
import com.karol.readingsapp.data.bible.TargetReadingDetails
import com.karol.readingsapp.data.bible.TranslationEntity
import com.karol.readingsapp.data.plan.ReadingPlanDao
import com.karol.readingsapp.data.plan.SimpleReading
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ReadingRepository(
    private val bibleDao: BibleDao,
    private val planDao: ReadingPlanDao,
) {

    private val bookNameToId = mapOf(
        "Genesis" to 0, "Exodus" to 1, "Leviticus" to 2, "Numbers" to 3, "Deuteronomy" to 4,
        "Joshua" to 5, "Judges" to 6, "Ruth" to 7, "1 Samuel" to 8, "2 Samuel" to 9,
        "1 Kings" to 10, "2 Kings" to 11, "1 Chronicles" to 12, "2 Chronicles" to 13,
        "Ezra" to 14, "Nehemiah" to 15, "Esther" to 16, "Job" to 17, "Psalms" to 18,
        "Proverbs" to 19, "Ecclesiastes" to 20, "Song of Solomon" to 21, "Isaiah" to 22,
        "Jeremiah" to 23, "Lamentations" to 24, "Ezekiel" to 25, "Daniel" to 26,
        "Hosea" to 27, "Joel" to 28, "Amos" to 29, "Obadiah" to 30, "Jonah" to 31,
        "Micah" to 32, "Nahum" to 33, "Habakkuk" to 34, "Zephaniah" to 35, "Haggai" to 36,
        "Zechariah" to 37, "Malachi" to 38, "Matthew" to 39, "Mark" to 40, "Luke" to 41,
        "John" to 42, "Acts" to 43, "Romans" to 44, "1 Corinthians" to 45, "2 Corinthians" to 46,
        "Galatians" to 47, "Ephesians" to 48, "Philippians" to 49, "Colossians" to 50,
        "1 Thessalonians" to 51, "2 Thessalonians" to 52, "1 Timothy" to 53, "2 Timothy" to 54,
        "Titus" to 55, "Philemon" to 56, "Hebrews" to 57, "James" to 58, "1 Peter" to 59,
        "2 Peter" to 60, "1 John" to 61, "2 John" to 62, "3 John" to 63, "Jude" to 64,
        "Revelation" to 65,
        // Common Abbreviations
        "Gen" to 0, "Ex" to 1, "Exod" to 1, "Lev" to 2, "Num" to 3, "Deut" to 4,
        "Josh" to 5, "Judg" to 6, "1Sam" to 8, "2Sam" to 9, "1Kings" to 10, "2Kings" to 11,
        "1Chr" to 12, "2Chr" to 13, "Neh" to 15, "Esth" to 16, "Ps" to 18, "Prov" to 19,
        "Eccl" to 20, "Song" to 21, "Isa" to 22, "Jer" to 23, "Lam" to 24, "Ezek" to 25,
        "Dan" to 26, "Hos" to 27, "Mic" to 32, "Hab" to 34, "Zeph" to 35, "Hag" to 36,
        "Zech" to 37, "Mal" to 38, "Matt" to 39, "Mt" to 39, "Mk" to 40, "Lk" to 41,
        "Jn" to 42, "Rom" to 44, "1Cor" to 45, "2Cor" to 46, "Gal" to 47, "Eph" to 48,
        "Phil" to 49, "Col" to 50, "1Thess" to 51, "2Thess" to 52, "1Tim" to 53, "2Tim" to 54,
        "Tit" to 55, "Philem" to 56, "Heb" to 57, "Jas" to 58, "1Pet" to 59, "2Pet" to 60,
        "1Jn" to 61, "2Jn" to 62, "3Jn" to 63, "Rev" to 65,
        // With spaces and periods
        "1 Sam" to 8, "2 Sam" to 9, "1 Kings" to 10, "2 Kings" to 11,
        "1 Chron" to 12, "2 Chron" to 13, "1 Cor" to 45, "2 Cor" to 46,
        "1 Thess" to 51, "2 Thess" to 52, "1 Tim" to 53, "2 Tim" to 54,
        "1 Pet" to 59, "2 Pet" to 60, "1 John" to 61, "2 John" to 62, "3 John" to 63,
        "Gen." to 0, "Exod." to 1, "Lev." to 2, "Num." to 3, "Deut." to 4,
        "Josh." to 5, "Judg." to 6, "1 Sam." to 8, "2 Sam." to 9, "1 Kings." to 10, "2 Kings." to 11,
        "1 Chron." to 12, "2 Chron." to 13, "Neh." to 15, "Esth." to 16, "Ps." to 18, "Prov." to 19,
        "Eccl." to 20, "Isa." to 22, "Jer." to 23, "Lam." to 24, "Lament" to 24, "Ezek." to 25,
        "Dan." to 26, "Hos." to 27, "Mic." to 32, "Hab." to 34, "Zeph." to 35, "Hag." to 36,
        "Zech." to 37, "Mal." to 38, "Matt." to 39, "Gal." to 47, "Eph." to 48,
        "Phil." to 49, "Col." to 50, "1 Thess." to 51, "2 Thess." to 52, "1 Tim." to 53,
        "2 Tim." to 54, "Heb." to 57, "1 Pet." to 59, "2 Pet." to 60, "Rev." to 65,
        "1 Cor." to 45, "2 Cor." to 46, "1 John." to 61, "2 John." to 62, "3 John." to 63,
        "Chronicles" to 12, "Song of Songs" to 21,
        "Thesselonians" to 51, "1 Thesselonians" to 51, "2 Thesselonians" to 52,
        "1 Thess." to 51, "2 Thess." to 52,
    )

    private val sortedBookNames = bookNameToId.keys.sortedByDescending { it.length }

    suspend fun getReadingsForDate(date: String, translationCode: String = "ENG"): Map<String, List<TargetReadingDetails>> = withContext(Dispatchers.IO) {
        val localDate = LocalDate.parse(date)
        val dayIndex = getPlanDayIndex(localDate)

        var plan = planDao.getReadingPlanByDay(dayIndex)
        if (plan == null) {
            // Try 0-indexed fallback (some databases start at 0)
            plan = planDao.getReadingPlanByDay(dayIndex - 1)
        }

        if (plan == null) {
            android.util.Log.e("ReadingRepository", "No reading plan found for day $dayIndex or ${dayIndex-1}")
            return@withContext emptyMap()
        }

        val readings = mutableListOf<TargetReadingDetails>()
        
        readings.addAll(parseReading(plan.track1 ?: "", "First Reading", date, translationCode))
        readings.addAll(parseReading(plan.track2 ?: "", "Second Reading", date, translationCode))
        readings.addAll(parseReading(plan.track3 ?: "", "Third Reading", date, translationCode))

        readings.groupBy { it.readingType }
    }

    private fun getPlanDayIndex(date: LocalDate): Int {
        if (!date.isLeapYear) return date.dayOfYear
        val doy = date.dayOfYear
        return if (doy <= 60) doy else doy - 1
    }

    private fun parseReadingInternal(readingStr: String): Triple<Int, String, String>? {
        // Normalize all types of spaces (including non-breaking spaces) to a single standard space
        val normalizedSpaces = readingStr.replace("""[\s\u00A0\u2007\u202F\u205F\u3000]+""".toRegex(), " ")
        
        // Normalize dots: some plans use "Matt.1" others "Matt . 1"
        // Replacing dots with spaces helps split the book and chapter, but we must ensure
        // our bookNameToId map handles both "1 Thess" and "1 Thess." (which it does via the fallbacks)
        val cleanStr = normalizedSpaces.replace("""\.+""".toRegex(), " ").replace("""\s+""".toRegex(), " ").trim()

        if (cleanStr.isEmpty()) return null

        // Try exact match first on the cleaned book name part
        val lastSpaceIndex = cleanStr.lastIndexOf(' ')
        if (lastSpaceIndex != -1) {
            var bookPart = cleanStr.substring(0, lastSpaceIndex).trim()
            val chapterPart = cleanStr.substring(lastSpaceIndex + 1).trim()

            var bookId = bookNameToId[bookPart]
            
            if ((bookId == null) && !bookPart.endsWith(".")) {
                bookId = bookNameToId["$bookPart."]
                if (bookId != null) {
                    bookPart = "$bookPart."
                }
            }

            bookId?.let { return Triple(it, bookPart, chapterPart) }
        } else {
            // No space, try bookNameToId directly
            bookNameToId[cleanStr]?.let { return Triple(it, cleanStr, "1") }
        }

        // Try partial matches using sorted names to avoid "Lam" matching "Lamentations" prematurely
        for (name in sortedBookNames) {
            val id = bookNameToId[name]!!
            if (cleanStr.contains(name, ignoreCase = true)) {
                val chapters = cleanStr.replace(name, "", ignoreCase = true).replace("""\s+""".toRegex(), " ").trim()
                return Triple(id, name, chapters.ifBlank { "1" })
            }
        }

        return Triple(-1, cleanStr, "")
    }

    private suspend fun parseReading(readingStr: String, type: String, date: String, translationCode: String): List<TargetReadingDetails> {
        val results = mutableListOf<TargetReadingDetails>()
        val parts = readingStr.split(";")
        
        for (part in parts) {
            val parsed = parseReadingInternal(part.trim()) ?: continue
            val (bookId, bookName, chaptersStr) = parsed
            
            val chapters = mutableListOf<Int>()
            if (chaptersStr.isNotEmpty()) {
                val chapterParts = chaptersStr.split(",")
                for (cpPart in chapterParts) {
                    val trimmedPart = cpPart.trim()
                    if (trimmedPart.contains("-")) {
                        val rangeParts = trimmedPart.split("-")
                        val start = rangeParts.firstOrNull()?.trim()?.toIntOrNull() ?: 1
                        val end = rangeParts.lastOrNull()?.trim()?.toIntOrNull() ?: start
                        for (i in start..end) chapters.add(i)
                    } else {
                        trimmedPart.toIntOrNull()?.let { chapters.add(it) }
                    }
                }
            }

            if ((chapters.isNotEmpty()) && (bookId != -1)) {
                val verses = bibleDao.getVersesForReading(date, bookId, chapters, type, translationCode, bookName)
                if (verses.isEmpty()) {
                    // Fallback to ensure something is displayed if the plan has the reference but DB lacks the text
                    results.add(
                        TargetReadingDetails(
                        date = date,
                        bookId = bookId,
                        bookName = bookName,
                        chapter = chapters.first(),
                        verseId = 1,
                        text = "Reading content not found.",
                        readingType = type,
                        translationCode = translationCode,
                    ))
                } else {
                    results.addAll(verses)
                }
            }
        }
        return results
    }


    suspend fun getReadingsForMonth(monthStr: String): Map<String, List<SimpleReading>> = withContext(Dispatchers.IO) {
        val firstOfMonth = LocalDate.parse("$monthStr-01")
        val daysInMonth = firstOfMonth.lengthOfMonth()
        
        val result = mutableMapOf<String, List<SimpleReading>>()
        for (day in 1..daysInMonth) {
            val date = firstOfMonth.withDayOfMonth(day)
            val dayIndex = getPlanDayIndex(date)
            val fullDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            
            var plan = planDao.getReadingPlanByDay(dayIndex)
            if (plan == null) {
                plan = planDao.getReadingPlanByDay(dayIndex - 1)
            }

            plan?.let { p ->
                val simpleList = mutableListOf<SimpleReading>()
                
                listOf(
                    p.track1 to "First Reading",
                    p.track2 to "Second Reading",
                    p.track3 to "Third Reading",
                ).forEach { (ref, type) ->
                    if (!ref.isNullOrBlank()) {
                        parseReadingInternal(ref)?.let { (bookId, bookName, chaptersStr) ->
                            simpleList.add(SimpleReading(fullDate, bookId, bookName, chaptersStr, type))
                        }
                    }
                }
                result[fullDate] = simpleList
            }
        }
        result
    }

    suspend fun getAvailableTranslations(): List<TranslationEntity> = withContext(Dispatchers.IO) {
        bibleDao.getAvailableTranslations()
    }

    suspend fun getAllBooks(): List<BookEntity> = withContext(Dispatchers.IO) {
        bibleDao.getAllBooks()
    }

    suspend fun getChapterCount(bookId: Int): Int = withContext(Dispatchers.IO) {
        bibleDao.getChapterCount(bookId)
    }

    suspend fun getVerseCount(bookId: Int, chapter: Int): Int = withContext(Dispatchers.IO) {
        bibleDao.getVerseCount(bookId, chapter)
    }

    suspend fun getChapterVerses(bookId: Int, chapter: Int, translationCode: String): List<TargetReadingDetails> = withContext(Dispatchers.IO) {
        bibleDao.getChapterVerses(bookId, chapter, translationCode)
    }
}
