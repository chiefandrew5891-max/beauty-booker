package com.beautyplanner.client.navigation

object Routes {
    const val AUTH = "auth"
    const val COMPLETE_PROFILE = "complete_profile"
    const val DISCOVER = "discover"
    const val MASTER_PROFILE = "master_profile/{masterId}"
    const val SERVICES = "services/{masterId}"
    const val BOOKING_CALENDAR = "booking_calendar/{masterId}/{serviceId}"
    const val BOOKING_DAY = "booking_day/{masterId}/{serviceId}/{date}"
    const val BOOKING_FORM = "booking_form/{masterId}/{serviceId}/{dateTime}"
    const val BOOKING_CONFIRMATION = "booking_confirmation/{bookingId}"
    const val REVIEWS = "reviews/{masterId}"
    const val LEAVE_REVIEW = "leave_review/{masterId}/{appointmentId}"

    fun masterProfile(masterId: String) = "master_profile/$masterId"

    fun services(masterId: String) = "services/$masterId"

    fun bookingCalendar(masterId: String, serviceId: String) =
        "booking_calendar/$masterId/$serviceId"

    fun bookingDay(masterId: String, serviceId: String, date: String) =
        "booking_day/$masterId/$serviceId/$date"

    fun bookingForm(masterId: String, serviceId: String, dateTime: String): String {
        val encoded = encodePathSegment(dateTime)
        return "booking_form/$masterId/$serviceId/$encoded"
    }

    fun bookingConfirmation(bookingId: String) = "booking_confirmation/$bookingId"

    fun reviews(masterId: String) = "reviews/$masterId"

    fun leaveReview(masterId: String, appointmentId: String) =
        "leave_review/$masterId/$appointmentId"

    fun decodeBookingDateTime(encodedDateTime: String): String = decodePathSegment(encodedDateTime)
}

private val UNRESERVED =
    "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~"

private fun encodePathSegment(value: String): String {
    val bytes = value.encodeToByteArray()
    val encoded = StringBuilder(bytes.size)
    for (b in bytes) {
        val c = (b.toInt() and 0xFF).toChar()
        if (UNRESERVED.indexOf(c) >= 0) {
            encoded.append(c)
        } else {
            encoded.append('%')
            encoded.append((b.toInt() and 0xFF).toString(16).uppercase().padStart(2, '0'))
        }
    }
    return encoded.toString()
}

private fun decodePathSegment(value: String): String {
    if ('%' !in value) return value

    val out = mutableListOf<Byte>()
    var index = 0
    while (index < value.length) {
        val char = value[index]
        if (char == '%' && index + 2 < value.length) {
            val hex = value.substring(index + 1, index + 3)
            val byte = hex.toIntOrNull(16)
            if (byte != null) {
                out.add(byte.toByte())
                index += 3
                continue
            }
        }
        out.add(char.code.toByte())
        index++
    }
    return out.toByteArray().decodeToString()
}
