package com.karol.readingsapp.data

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class ReadingRepository(private val combinedDao: CombinedDao) {

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
        "Revelation" to 65, "Chronicles" to 12 // Supporting both "1 Chronicles" and just "Chronicles" if needed
    )

    suspend fun getReadingsForDate(date: String, translationCode: String = "ENG"): Map<String, List<TargetReadingDetails>> {
        val localDate = LocalDate.parse(date)
        val dayIndex = getPlanDayIndex(localDate)

        val plan = combinedDao.getReadingPlanByDay(dayIndex) ?: return emptyMap()

        val readings = mutableListOf<TargetReadingDetails>()
        
        readings.addAll(parseReading(plan.track_1 ?: "", "First Reading", date, translationCode))
        readings.addAll(parseReading(plan.track_2 ?: "", "Second Reading", date, translationCode))
        readings.addAll(parseReading(plan.track_3 ?: "", "Third Reading", date, translationCode))

        return readings.groupBy { it.readingType }
    }

    private fun getPlanDayIndex(date: LocalDate): Int {
        if (!date.isLeapYear) return date.dayOfYear
        val doy = date.dayOfYear
        // Standard adjustment: Feb 29 and Mar 1 both use day 60 if only 365 readings exist.
        // A better fix would be a 366-day table, but this ensures no crashes and continuity.
        return if (doy <= 59) doy else if (doy == 60) 60 else doy - 1
    }

    private suspend fun parseReading(readingStr: String, type: String, date: String, translationCode: String): List<TargetReadingDetails> {
        // Format: "Genesis 1-2" or "Psalms 23" or "1 Chronicles 1"
        val lastSpaceIndex = readingStr.lastIndexOf(' ')
        if (lastSpaceIndex == -1) return emptyList()

        val bookName = readingStr.substring(0, lastSpaceIndex).trim()
        val chaptersStr = readingStr.substring(lastSpaceIndex + 1).trim()

        val bookId = bookNameToId[bookName] ?: return emptyList()
        val chapters = mutableListOf<Int>()

        if (chaptersStr.contains("-")) {
            val parts = chaptersStr.split("-")
            val start = parts[0].toIntOrNull() ?: 1
            val end = if (parts.size > 1) parts[1].toIntOrNull() ?: start else start
            for (i in start..end) chapters.add(i)
        } else {
            chaptersStr.toIntOrNull()?.let { chapters.add(it) }
        }

        if (chapters.isEmpty()) return emptyList()

        return combinedDao.getVersesForReading(date, bookId, chapters, type, translationCode, bookName)
    }

    suspend fun getReadingsForMonth(monthStr: String): Map<String, List<SimpleReading>> {
        // monthStr is "YYYY-MM"
        val firstOfMonth = LocalDate.parse("$monthStr-01")
        val daysInMonth = firstOfMonth.lengthOfMonth()
        
        val result = mutableMapOf<String, List<SimpleReading>>()
        for (day in 1..daysInMonth) {
            val date = firstOfMonth.withDayOfMonth(day)
            val dayIndex = getPlanDayIndex(date)
            val fullDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            
            combinedDao.getReadingPlanByDay(dayIndex)?.let { plan ->
                val simpleList = listOf(
                    SimpleReading(fullDate, plan.track_1 ?: "", "First Reading"),
                    SimpleReading(fullDate, plan.track_2 ?: "", "Second Reading"),
                    SimpleReading(fullDate, plan.track_3 ?: "", "Third Reading")
                )
                result[fullDate] = simpleList
            }
        }
        return result
    }

    suspend fun getAvailableTranslations(): List<BibleTranslation> {
        return combinedDao.getAvailableTranslations()
    }
}
