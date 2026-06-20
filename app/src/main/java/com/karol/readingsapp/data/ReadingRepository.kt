package com.karol.readingsapp.data

class ReadingRepository(private val combinedDao: CombinedDao) {
    suspend fun getReadingsForDate(date: String, translationCode: String = "ENG"): Map<String, List<TargetReadingDetails>> {
        return combinedDao.getReadingForDate(date, translationCode).groupBy { it.readingType }
    }

    suspend fun getReadingsForMonth(month: String): Map<String, List<SimpleReading>> {
        // month should be in YYYY-MM format
        return combinedDao.getSimpleReadingsForMonth("$month%").groupBy { it.date }
    }

    suspend fun getAvailableTranslations(): List<BibleTranslation> {
        return combinedDao.getAvailableTranslations()
    }
}
