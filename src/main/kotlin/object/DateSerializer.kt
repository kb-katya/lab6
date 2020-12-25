package `object`

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate

@Serializer(forClass = LocalDate::class)
object DateSerializer : KSerializer<LocalDate> {

    override fun serialize(output: Encoder, obj: LocalDate) {
        output.encodeString(obj.toString())
    }

    override fun deserialize(input: Decoder): LocalDate {
        return LocalDate.parse(input.decodeString())
    }
}