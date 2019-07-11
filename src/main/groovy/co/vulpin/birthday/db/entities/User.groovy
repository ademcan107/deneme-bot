package co.vulpin.birthday.db.entities


import groovy.transform.InheritConstructors

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

@InheritConstructors
class User {

    Long birthdayEpochSeconds
    Integer gmtOffset

    boolean hasBirthday() {
        return birthdayEpochSeconds != null && gmtOffset != null
    }

    boolean isBirthday() {
        if(!hasBirthday())
            return false

        def start = birthdayStart
        def end = birthdayEnd

        def now = OffsetDateTime.now()

        while(now.isAfter(end)) {
            start = start.plusYears(1)
            end = end.plusYears(1)
        }

        return now.isAfter(start)
    }

    OffsetDateTime getBirthdayStart() {
        if(!hasBirthday())
            return null

        def instant = Instant.ofEpochSecond(birthdayEpochSeconds)
        return OffsetDateTime.ofInstant(instant, zoneOffset)
    }

    OffsetDateTime getBirthdayEnd() {
        return birthdayStart?.plusDays(1)
    }

    ZoneOffset getZoneOffset() {
        if(gmtOffset == null)
            return null

        return ZoneOffset.ofTotalSeconds(gmtOffset)
    }

}
