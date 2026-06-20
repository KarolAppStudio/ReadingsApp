package com.karol.readingsapp.data

class ReadingRepository(private val combinedDao: CombinedDao) {
    suspend fun getReadingsForDate(date: String, translationCode: String = "ENG"): Map<String, List<TargetReadingDetails>> {
        return combinedDao.getReadingForDate(date, translationCode).groupBy { it.readingType }
    }

    suspend fun getAvailableTranslations(): List<BibleTranslation> {
        return combinedDao.getAvailableTranslations()
    }
}
