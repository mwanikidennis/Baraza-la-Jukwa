package ke.jukwa.privacy

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Strips all EXIF metadata from images before network transmission.
 * This is a privacy requirement -- EXIF data can contain GPS coordinates,
 * device info, timestamps, and other identifying information.
 */
@Singleton
class ExifStripper @Inject constructor(
    private val context: Context
) {
    /**
     * Strips EXIF metadata from an image file by re-encoding it.
     * Returns a new file with all metadata removed.
     * The original file is NOT modified or deleted.
     */
    fun stripExif(sourceFile: File): File {
        val options = BitmapFactory.Options().apply {
            inMutable = false
        }
        val bitmap = BitmapFactory.decodeFile(sourceFile.absolutePath, options)
            ?: throw IllegalArgumentException("Cannot decode image: ${sourceFile.absolutePath}")

        // Read rotation from EXIF to apply correct orientation
        val exif = ExifInterface(sourceFile.absolutePath)
        val rotation = when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }

        val rotatedBitmap = if (rotation != 0f) {
            val matrix = Matrix().apply { postRotate(rotation) }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }

        // Write to new file as JPEG with no EXIF (re-encoding strips all metadata)
        val outputFile = File(context.cacheDir, "stripped_${System.currentTimeMillis()}_${sourceFile.name}")
        FileOutputStream(outputFile).use { fos ->
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos)
        }

        if (rotatedBitmap !== bitmap) {
            rotatedBitmap.recycle()
        }
        bitmap.recycle()

        return outputFile
    }
}
