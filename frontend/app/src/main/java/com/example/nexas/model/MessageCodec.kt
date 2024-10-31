package com.example.nexas.model

import android.graphics.BitmapFactory
import org.bson.BsonReader
import org.bson.BsonType
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import java.io.ByteArrayOutputStream
import java.time.Instant
import android.graphics.Bitmap

class MessageCodec : Codec<Message> {
    override fun encode(writer: BsonWriter, value: Message, encoderContext: EncoderContext) {
        writer.writeStartDocument()
        writer.writeString("id", value.id)
        writer.writeString("senderID", value.senderID)
        writer.writeString("groupID", value.groupID)
        writer.writeString("videoID", value.videoID)

        // Encode the timestamp as BsonDateTime
        writer.writeDateTime("timestamp", value.timestamp.toEpochMilli())

        // Handle videoImage serialization
        value.videoImage?.let { bitmap ->
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray = stream.toByteArray()
            writer.writeBinaryData("videoImage", org.bson.BsonBinary(byteArray))
        }

        writer.writeEndDocument()
    }

    override fun decode(reader: BsonReader, decoderContext: DecoderContext): Message {
        reader.readStartDocument()

        var id: String? = null
        var senderID: String? = null
        var groupID: String? = null
        var videoImage: Bitmap? = null
        var videoID: String? = null
        var timestamp: Instant? = null

        // Read each field
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            when (val fieldName = reader.readName()) {
                "id" -> id = reader.readString()
                "senderID" -> senderID = reader.readString()
                "groupID" -> groupID = reader.readString()
                "videoID" -> videoID = reader.readString()
                "timestamp" -> timestamp = Instant.ofEpochMilli(reader.readDateTime())
                "videoImage" -> {
                    val binaryData = reader.readBinaryData()
                    videoImage = BitmapFactory.decodeByteArray(binaryData.data, 0, binaryData.data.size)
                }
                else -> reader.skipValue()
            }
        }
        reader.readEndDocument()

        return Message(
            id = id ?: error("Missing 'id' field"),
            senderID = senderID ?: error("Missing 'senderID' field"),
            groupID = groupID ?: error("Missing 'groupID' field"),
            videoImage = videoImage,
            videoID = videoID ?: error("Missing 'videoID' field"),
            timestamp = timestamp ?: Instant.now() // Provide current time if missing
        )
    }

    override fun getEncoderClass(): Class<Message> = Message::class.java
}