package ke.jukwa.privacy

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Fuzzes GPS coordinates to ward centroid for Incognito mode.
 * Adds plus/minus 500m random noise to prevent exact location tracking.
 */
@Singleton
class GpsFuzzer @Inject constructor() {
    // ~500m in degrees (approximate at equator)
    // 1 degree latitude ~111km, 1 degree longitude ~111km at equator
    private val fuzzMeters = 500.0
    private val degreesPerMeter = 1.0 / 111000.0 // approximate

    /**
     * Fuzzes a coordinate by adding random noise.
     * @param latitude Original latitude
     * @param longitude Original longitude
     * @return Fuzzed coordinates with plus/minus 500m noise
     */
    fun fuzz(latitude: Double, longitude: Double): Pair<Double, Double> {
        val latOffset = Random.nextDouble(-fuzzMeters, fuzzMeters) * degreesPerMeter
        val lonOffset = Random.nextDouble(-fuzzMeters, fuzzMeters) * degreesPerMeter
        return Pair(latitude + latOffset, longitude + lonOffset)
    }

    /**
     * Snaps a coordinate to the nearest ward centroid (if known) then fuzzes.
     * This provides stronger privacy than pure fuzzing.
     * @param latitude Original latitude
     * @param longitude Original longitude
     * @param wardCentroidLat Ward centroid latitude (null if unknown)
     * @param wardCentroidLon Ward centroid longitude (null if unknown)
     * @return Fuzzed coordinates
     */
    fun fuzzToWardCentroid(
        latitude: Double,
        longitude: Double,
        wardCentroidLat: Double?,
        wardCentroidLon: Double?
    ): Pair<Double, Double> {
        val baseLat = wardCentroidLat ?: latitude
        val baseLon = wardCentroidLon ?: longitude
        return fuzz(baseLat, baseLon)
    }
}
