package ke.jukwa.privacy

import android.content.Context
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orchestrates the full media sanitization pipeline.
 * Strips EXIF, applies GPS fuzzing for Incognito mode, compresses.
 */
@Singleton
class MediaSanitizer @Inject constructor(
    private val context: Context,
    private val exifStripper: ExifStripper,
    private val gpsFuzzer: GpsFuzzer
) {
    enum class AnonymityLevel {
        STANDARD, INCOGNITO, VERIFIED
    }

    data class SanitizedMedia(
        val file: File,
        val latitude: Double?,
        val longitude: Double?,
        val wasExifStripped: Boolean
    )

    /**
     * Sanitizes a media file for upload.
     * - Always strips EXIF metadata
     * - For INCOGNITO mode, fuzzes GPS coordinates
     * - Returns the sanitized file and adjusted coordinates
     */
    fun sanitize(
        sourceFile: File,
        latitude: Double?,
        longitude: Double?,
        anonymityLevel: AnonymityLevel,
        wardCentroidLat: Double? = null,
        wardCentroidLon: Double? = null
    ): SanitizedMedia {
        // Step 1: Strip EXIF (always, regardless of anonymity mode)
        val strippedFile = exifStripper.stripExif(sourceFile)

        // Step 2: Fuzz GPS for Incognito mode
        val (finalLat, finalLon) = when (anonymityLevel) {
            AnonymityLevel.INCOGNITO -> {
                if (latitude != null && longitude != null) {
                    val (fuzzedLat, fuzzedLon) = gpsFuzzer.fuzzToWardCentroid(
                        latitude, longitude, wardCentroidLat, wardCentroidLon
                    )
                    Pair(fuzzedLat, fuzzedLon)
                } else {
                    Pair(null, null)
                }
            }
            AnonymityLevel.STANDARD -> Pair(latitude, longitude)
            AnonymityLevel.VERIFIED -> Pair(latitude, longitude)
        }

        return SanitizedMedia(
            file = strippedFile,
            latitude = finalLat,
            longitude = finalLon,
            wasExifStripped = true
        )
    }
}
