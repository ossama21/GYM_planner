package com.H_Oussama.gymplanner.data.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.h_oussama.gymplanner.datastore.WorkoutPlanProto
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object WorkoutPlanSerializer : Serializer<WorkoutPlanProto> {
    // Provide a default instance for when the file is created
    override val defaultValue: WorkoutPlanProto = WorkoutPlanProto.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): WorkoutPlanProto {
        try {
            // readFrom uses Parcelable serialization methods // Correction: uses protobuf parseFrom
            return WorkoutPlanProto.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: WorkoutPlanProto, output: OutputStream) {
        // writeTo uses Parcelable serialization methods // Correction: uses protobuf writeTo
        t.writeTo(output)
    }
}