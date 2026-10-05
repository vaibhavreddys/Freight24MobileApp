package com.freight24.rest

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object IntToDoubleSerializer : KSerializer<Double> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("IntToDouble", PrimitiveKind.DOUBLE) //

    override fun serialize(
        encoder: Encoder,
        value: Double,
    ) {
        encoder.encodeDouble(value)
    }

    override fun deserialize(decoder: Decoder): Double {
        // This will decode any number (int or double) from the JSON as a Double
        return decoder.decodeDouble()
    }
}
