package com.example.nexas.model

import org.bson.BsonReader
import org.bson.BsonType
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream



class GroupCodec : Codec<Group> {
    override fun encode(writer: BsonWriter, value: Group, encoderContext: EncoderContext) {
        writer.writeStartDocument()
        writer.writeString("id", value.id)
        writer.writeString("name", value.name)
        // Serialize avatar if present (for example, as a byte array)
        value.avatar?.let { bitmap ->
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray = stream.toByteArray()
            writer.writeBinaryData("avatar", org.bson.BsonBinary(byteArray))
        }
        writer.writeString("location", value.location)
        writer.writeString("description", value.description)
        writer.writeInt32("membersLimit", value.membersLimit)

        // Serialize members (IDs only)
        writer.writeStartArray("members")
        for (member in value?.members?:emptyList()) {
            writer.writeString(member.id) // Store only the member ID
        }
        writer.writeEndArray()

        // Serialize messages (IDs only)
        writer.writeStartArray("messages")
        for (message in value?.messages?:emptyList()) {
            writer.writeString(message.id) // Store only the message ID
        }
        writer.writeEndArray()

        writer.writeEndDocument()
    }

    override fun decode(reader: BsonReader, decoderContext: DecoderContext): Group {
        reader.readStartDocument()

        val id = reader.readString("id")
        val name = reader.readString("name")
        // Decode avatar if present
        val avatar: Bitmap? = if (reader.readBsonType() == BsonType.BINARY) {
            val binaryData = reader.readBinaryData("avatar")
            BitmapFactory.decodeByteArray(binaryData.data, 0, binaryData.data.size)
        } else {
            reader.skipValue()
            null
        }
        val location = reader.readString("location")
        val description = reader.readString("description")
        val membersLimit = reader.readInt32("membersLimit")

        // Decode members (IDs only)
        val members = mutableListOf<UserProfile>()
        reader.readStartArray() // Start reading members array
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            val memberId = reader.readString()
            // Optionally store member IDs, but you will look up full UserProfiles later
            members.add(UserProfile(id = memberId, uname = "", fname = "", lname = "", email = "", location = "", description = "", avatar = null, background = null, age = 0, hashedPassword = ""))
        }
        reader.readEndArray() // End members array

        // Decode messages (IDs only)
        val messages = mutableListOf<Message>()
        reader.readStartArray() // Start reading messages array
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            val messageId = reader.readString()
            // Optionally store message IDs, but you will look up full Message objects later
            messages.add(Message(id = messageId, senderID = "", groupID = "", videoImage = null, videoID = ""))
        }
        reader.readEndArray() // End messages array

        reader.readEndDocument()

        return Group(id, name, avatar, location, description, membersLimit, members, messages)
    }

    override fun getEncoderClass(): Class<Group> = Group::class.java
}
