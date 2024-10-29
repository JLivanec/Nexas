package com.example.nexas.model

import org.bson.BsonReader
import org.bson.BsonType
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext

class UserProfileCodec : Codec<UserProfile> {
    override fun encode(writer: BsonWriter, value: UserProfile, encoderContext: EncoderContext) {
        writer.writeStartDocument()

        // Write the fields of the UserProfile
        writer.writeString("userID", value.userID)
        writer.writeString("uname", value.uname)
        writer.writeString("fname", value.fname)
        writer.writeString("lname", value.lname)
        writer.writeString("email", value.email)
        writer.writeString("hashedPassword", value.hashedPassword)

        writer.writeEndDocument()
    }

    override fun decode(reader: BsonReader, decoderContext: DecoderContext): UserProfile {
        reader.readStartDocument()

        // Initialize fields
        var userID: String? = null
        var uname: String? = null
        var fname: String? = null
        var lname: String? = null
        var email: String? = null
        var hashedPassword: String? = null
        var id: String? = null

        // Read fields
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            val fieldName = reader.readName()
            when (fieldName) {
                "userID" -> userID = reader.readString()
                "uname" -> uname = reader.readString()
                "fname" -> fname = reader.readString()
                "lname" -> lname = reader.readString()
                "email" -> email = reader.readString()
                "hashedPassword" -> hashedPassword = reader.readString()
                "_id" -> id = reader.readObjectId().toHexString() // Read the _id field
                else -> reader.skipValue() // Skip any unrecognized fields
            }
        }

        reader.readEndDocument()

        // Return the UserProfile with the potentially set _id
        return UserProfile(userID!!, uname!!, fname!!, lname!!, email!!, hashedPassword!!, id)
    }

    override fun getEncoderClass(): Class<UserProfile> = UserProfile::class.java
}
