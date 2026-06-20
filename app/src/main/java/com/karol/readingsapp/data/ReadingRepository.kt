package com.karol.readingsapp.data

import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
        "Revelation" to 65, "Chronicles" to 12, // Supporting both "1 Chronicles" and just "Chronicles" if needed
    )

    suspend fun getReadingsForDate(date: String, translationCode: String = "ENG"): Map<String, List<TargetReadingDetails>> {
        val localDate = LocalDate.parse(date)
        val dayIndex = getPlanDayIndex(localDate)

        val plan = combinedDao.getReadingPlanByDay(dayIndex) ?: return emptyMap()

        val readings = mutableListOf<TargetReadingDetails>()
        
        readings.addAll(parseReading(plan.track1 ?: "", "First Reading", date, translationCode))
        readings.addAll(parseReading(plan.track2 ?: "", "Second Reading", date, translationCode))
        readings.addAll(parseReading(plan.track3 ?: "", "Third Reading", date, translationCode))

        return readings.groupBy { it.readingType }
    }

    private fun getPlanDayIndex(date: LocalDate): Int {
        if (!date.isLeapYear) return date.dayOfYear
        val doy = date.dayOfYear
        // Standard adjustment: Feb 29 and Mar 1 both use day 60 if only 365 readings exist.
        // A better fix would be a 366-day table, but this ensures no crashes and continuity.
        return if (doy <= 59) doy else if (doy == 60) 60 else doy - 1
    }

    private fun parseReadingInternal(readingStr: String): Triple<Int, String, String>? {
        val cleanStr = readingStr.replace("""\.+""".toRegex(), " ").replace("""\s+""".toRegex(), " ").trim()
        if (cleanStr.isEmpty()) return null

        // 1. Try exact match for book name only (e.g. "Genesis" or "1 Samuel")
        bookNameToId[cleanStr]?.let { return Triple(it, cleanStr, "1") }

        // 2. Try splitting by last space to separate book from chapter (e.g. "Genesis 1-2" or "1 Samuel 1")
        val lastSpaceIndex = cleanStr.lastIndexOf(' ')
        if (lastSpaceIndex != -1) {
            var bookPart = cleanStr.substring(0, lastSpaceIndex).trim()
            val chapterPart = cleanStr.substring(lastSpaceIndex + 1).trim()

            var bookId = bookNameToId[bookPart]
            
            // 2a. Handle leading artifact/number (e.g. "1 Isaiah 46-47" -> "Isaiah" "46-47")
            if (bookId == null) {
                val firstSpaceIndex = bookPart.indexOf(' ')
                if (firstSpaceIndex != -1) {
                    val candidateBook = bookPart.substring(firstSpaceIndex + 1).trim()
                    bookNameToId[candidateBook]?.let {
                        bookId = it
                        bookPart = candidateBook
                    }
                }
            }

            if (bookId != null) {
                return Triple(bookId!!, bookPart, chapterPart)
            }
        }

        // 3. Last resort: If we still haven't matched, but there's a book name inside the string
        for ((name, id) in bookNameToId) {
            if (cleanStr.contains(name, ignoreCase = true)) {
                // Try to extract chapters by removing the book name
                val chapters = cleanStr.replace(name, "", ignoreCase = true).replace("""\s+""".toRegex(), " ").trim()
                return Triple(id, name, chapters.ifBlank { "1" })
            }
        }

        // If all else fails, return as unknown book so it still shows up in the UI
        return Triple(-1, cleanStr, "")
    }

    private suspend fun parseReading(readingStr: String, type: String, date: String, translationCode: String): List<TargetReadingDetails> {
        val parsed = parseReadingInternal(readingStr) ?: return emptyList()
        val (bookId, bookName, chaptersStr) = parsed
        
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
                val simpleList = mutableListOf<SimpleReading>()
                
                listOf(
                    plan.track1 to "First Reading",
                    plan.track2 to "Second Reading",
                    plan.track3 to "Third Reading"
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
        return result
    }

    suspend fun getAvailableTranslations(): List<BibleTranslation> {
        return combinedDao.getAvailableTranslations()
    }
}
