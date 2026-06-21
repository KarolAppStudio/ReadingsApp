package com.karol.readingsapp.data

import com.karol.readingsapp.data.bible.BibleDao
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
    private val planDao: ReadingPlanDao
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
        // Abbreviations from readings.xml
        "Lev." to 2, "Deut." to 4, "1 Sam." to 8, "2 Sam." to 9,
        "1 Chron." to 12, "2 Chron." to 13, "Nehem." to 15, "Song" to 21, "Lament." to 24,
        "Ezek." to 25, "Dan." to 26, "Habak." to 34, "Zephan." to 35, "Matt." to 39,
        "Gal." to 47, "Ephes." to 48, "Philip." to 49, "Col." to 50, "1 Thess." to 51,
        "2 Thess." to 52, "1 Tim." to 53, "2 Tim." to 54, "Heb." to 57, "1 Pet." to 59,
        "2 Pet." to 60, "Rev." to 65, "Chronicles" to 12,
    )

    suspend fun getReadingsForDate(date: String, translationCode: String = "ENG"): Map<String, List<TargetReadingDetails>> = withContext(Dispatchers.IO) {
        val localDate = LocalDate.parse(date)
        val dayIndex = getPlanDayIndex(localDate)

        val plan = planDao.getReadingPlanByDay(dayIndex) ?: return@withContext emptyMap()

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
        val cleanStr = readingStr.replace("""\.+""".toRegex(), " ").replace("""\s+""".toRegex(), " ").trim()
        if (cleanStr.isEmpty()) return null

        bookNameToId[cleanStr]?.let { return Triple(it, cleanStr, "1") }

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
        }

        for ((name, id) in bookNameToId) {
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

            if (chapters.isNotEmpty()) {
                results.addAll(bibleDao.getVersesForReading(date, bookId, chapters, type, translationCode, bookName))
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
            
            planDao.getReadingPlanByDay(dayIndex)?.let { plan ->
                val simpleList = mutableListOf<SimpleReading>()
                
                listOf(
                    plan.track1 to "First Reading",
                    plan.track2 to "Second Reading",
                    plan.track3 to "Third Reading",
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
}
