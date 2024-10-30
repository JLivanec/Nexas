package com.example.nexas.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.bson.BsonReader
import org.bson.BsonType
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class UserProfileCodec : Codec<UserProfile> {

    override fun encode(writer: BsonWriter, value: UserProfile, encoderContext: EncoderContext) {
        writer.writeStartDocument()

        // Write the fields of the UserProfile
        writer.writeString("uname", value.uname)
        writer.writeString("fname", value.fname)
        writer.writeString("lname", value.lname)
        writer.writeString("email", value.email)
        writer.writeString("location", value.location)
        writer.writeString("description", value.description ?: "")
        writer.writeString("hashedPassword", value.hashedPassword)
        writer.writeInt32("age", value.age)

        // Serialize the avatar Bitmap to byte array if not null
        value.avatar?.let { bitmap ->
            val avatarBytes = bitmapToByteArray(bitmap)
            writer.writeBinaryData("avatar", org.bson.BsonBinary(avatarBytes))
        }

        // Serialize the background Bitmap to byte array if not null
        value.background?.let { bitmap ->
            val backgroundBytes = bitmapToByteArray(bitmap)
            writer.writeBinaryData("background", org.bson.BsonBinary(backgroundBytes))
        }

        // Write the id if available
        value.id?.let { id ->
            writer.writeObjectId("_id", org.bson.types.ObjectId(id))
        }

        writer.writeEndDocument()
    }

    override fun decode(reader: BsonReader, decoderContext: DecoderContext): UserProfile {
        reader.readStartDocument()

        // Initialize fields
        var uname: String? = null
        var fname: String? = null
        var lname: String? = null
        var email: String? = null
        var location: String? = null
        var description: String? = null
        var hashedPassword: String? = null
        var age: Int = 0
        var avatar: Bitmap? = null
        var background: Bitmap? = null
        var id: String? = null

        // Read fields
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            val fieldName = reader.readName()
            when (fieldName) {
                "uname" -> uname = reader.readString()
                "fname" -> fname = reader.readString()
                "lname" -> lname = reader.readString()
                "email" -> email = reader.readString()
                "location" -> location = reader.readString()
                "description" -> description = reader.readString()
                "hashedPassword" -> hashedPassword = reader.readString()
                "age" -> age = reader.readInt32()
                "avatar" -> avatar = reader.readBinaryData().data.let { byteArrayToBitmap(it) }
                "background" -> background = reader.readBinaryData().data.let { byteArrayToBitmap(it) }
                "_id" -> id = reader.readObjectId().toHexString()
                else -> reader.skipValue() // Skip any unrecognized fields
            }
        }

        reader.readEndDocument()

        return UserProfile(
            uname = uname!!,
            fname = fname!!,
            lname = lname!!,
            email = email!!,
            location = location!!,
            description = description,
            avatar = avatar,
            background = background,
            age = age,
            hashedPassword = hashedPassword!!,
            id = id
        )
    }

    override fun getEncoderClass(): Class<UserProfile> = UserProfile::class.java

    // Utility method to convert Bitmap to ByteArray
    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    // Utility method to convert ByteArray back to Bitmap
    private fun byteArrayToBitmap(bytes: ByteArray): Bitmap {
        return BitmapFactory.decodeStream(ByteArrayInputStream(bytes))
    }
}
