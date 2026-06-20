package com.karol.readingsapp.data

class ReadingRepository(private val combinedDao: CombinedDao) {
    suspend fun getReadingsForDate(date: String): Map<String, List<TargetReadingDetails>> {
        return combinedDao.getReadingForDate(date).groupBy { it.readingType }
    }
}
