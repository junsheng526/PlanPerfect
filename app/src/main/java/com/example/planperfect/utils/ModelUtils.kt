import android.content.Context
import org.json.JSONObject
import java.io.BufferedReader
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.io.FileInputStream

class ModelUtils(private val context: Context) {

    // Load TFLite model from assets
    fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd("knn_model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    // Load label encoder (mapping indices to string labels)
    fun loadLabelEncoder(): Map<Int, String> {
        val inputStream = context.assets.open("label_encoder.json")
        val bufferedReader = BufferedReader(inputStream.reader())
        val jsonString = bufferedReader.readText()
        val jsonObject = JSONObject(jsonString)

        // Parse JSON into a Map
        val labelMap = mutableMapOf<Int, String>()
        jsonObject.keys().forEach {
            labelMap[it.toInt()] = jsonObject.getString(it)
        }

        return labelMap
    }
}