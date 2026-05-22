package com.shrimpadvisor.plcycle.data

import kotlinx.coroutines.flow.Flow

class PondCycleRepository(
    private val pondCycleDao: PondCycleDao,
    private val dailyReadingDao: DailyReadingDao,
    private val regionProfileDao: RegionProfileDao
) {
    val allCycles: Flow<List<PondCycle>> = pondCycleDao.getAllCycles()

    fun getCycleById(id: Int): Flow<PondCycle?> = pondCycleDao.getCycleById(id)

    suspend fun insert(cycle: PondCycle): Long = pondCycleDao.insertCycle(cycle)
    suspend fun update(cycle: PondCycle) = pondCycleDao.updateCycle(cycle)
    suspend fun delete(cycle: PondCycle) = pondCycleDao.deleteCycle(cycle)

    fun getReadingsForCycle(cycleId: Int): Flow<List<DailyReading>> =
        dailyReadingDao.getReadingsForCycle(cycleId)

    suspend fun insertDailyReading(reading: DailyReading): Long =
        dailyReadingDao.insert(reading)

    suspend fun deleteDailyReading(reading: DailyReading) =
        dailyReadingDao.delete(reading)

    // Region profile operations
    val allRegionProfiles: Flow<List<RegionProfile>> = regionProfileDao.getAllProfiles()

    suspend fun getRegionById(id: Int): RegionProfile? = regionProfileDao.getById(id)

    suspend fun insertRegionProfile(profile: RegionProfile): Long =
        regionProfileDao.insert(profile)

    suspend fun updateRegionProfile(profile: RegionProfile) =
        regionProfileDao.update(profile)

    suspend fun deleteRegionProfile(profile: RegionProfile) =
        regionProfileDao.delete(profile)
}
