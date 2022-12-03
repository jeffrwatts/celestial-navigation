package com.jeffrwatts.celestialnavigation.utils

import java.math.RoundingMode
import kotlin.math.*

object CelNavUtils {
    const val North = 1
    const val South = -1
    const val East = 1
    const val West = -1

    const val equatorialRadiusMoon = 1738.1
    const val equatorialRadiusEarth = 6378.0
    const val equatorialRadiusSun = 695700.0

    enum class ICDirection {
        Off,
        On
    }

    enum class Limb {
        Center,
        Lower,
        Upper
    }

    enum class LOPDirection {
        Towards,
        Away
    }

    private fun nauticalMilesToKm(nauticalMiles: Double):Double {
        return nauticalMiles * 1.852
    }

    fun radians (degrees: Double): Double {
        return degrees * PI / 180.0
    }

    private fun degrees (radians: Double): Double {
        return radians * 180.0 / PI
    }

    fun angle (degrees: Int, minutes: Double, sign: Int = 1): Double {
        return (degrees + minutes / 60.0) * sign
    }

    fun degreesMinutesSign (angle: Double, minutePrecision: Int = 2): Triple<Int, Double, Int> {
        val sign = if (angle < 0) -1 else 1
        val angleUnsigned = angle * sign
        val degrees = floor(angleUnsigned).toInt()
        val minutes = roundToPrecision((angleUnsigned - degrees) * 60, minutePrecision)
        return Triple(degrees, minutes, sign)
    }

    fun angleAdd (angle1: Double, angle2: Double): Double {
        val sum = angle1 + angle2
        return if (sum >= 360.0) sum-360.0 else sum
    }

    fun roundToPrecision(value: Double, precision: Int): Double {
        return value.toBigDecimal().setScale(precision, RoundingMode.HALF_UP).toDouble()
    }

    fun calculateDipCorrection (heightEyeFt: Int): Double {
        return roundToPrecision(0.97 * sqrt(heightEyeFt.toDouble()), 1) * -1
    }

    fun calculateRefractionCorrection (Ha: Double): Double {
        val refraction = 1.0 / tan(radians(Ha + (7.31/(Ha+4.4))))
        return roundToPrecision(refraction, 1) * -1
    }

    fun calculateSemiDiameterCorrection(Ha: Double, distance: Double, equatorialRadius: Double, limb: Limb): Double {
        val sd = degrees(asin(equatorialRadius / distance))
        var sdMinutes = roundToPrecision(sd * 60, 1)

        if (limb == Limb.Upper) {
            sdMinutes *= -1.0
        }

        return sdMinutes
    }

    fun calculateHorizontalParallaxCorrection(Ha: Double, distance: Double, latitude: Double, isMoon: Boolean): Double {
        var hp = asin(equatorialRadiusEarth / distance)

        if (isMoon) {
            val hpCorrection = hp * sin(radians((latitude))).pow(2) / 298.3
            hp -= hpCorrection
        }

        // Calculate Horizontal Parallax in Altitude
        hp *= cos(radians(Ha))
        return roundToPrecision(degrees(hp) * 60, 1)
    }

    fun calculateLocalHourAngle (GHA: Double, lon: Double): Double {
        var localHourAngle = if (lon < 0) {
            // Western hemisphere.  LHA = GHA - lon
            GHA - (lon* West)
        } else {
            // Easter hemisphere.  LHA = GHA + lon
            GHA + (lon* East)
        }

        if (localHourAngle < 0) {
            localHourAngle += 360.0
        } else if (localHourAngle >= 360.0) {
            localHourAngle -= 360.0
        }

        return localHourAngle
    }

    fun calculateSightReduction(declination: Double, latitude: Double, LHA: Double) : Triple<Double, Double, Double> {
        var decRads = radians(declination)
        var latRads = radians(latitude)
        val LHARads = radians(LHA)

        // Sign rules: 1) All angles are treated as positive, regardless of hemisphere, except where latitude and
        // declination are in different hemispheres (i.e. contrary).  In the contrary case, make declination negative,
        // and keep latitude positive.  2) If the final Z is negative then add 180°, regardless of hemisphere.
        if ((decRads < 0) and (latRads < 0)) {
            // If dec and lat are in the same hemisphere, then both values should be positive.
            decRads = abs(decRads)
            latRads = abs(latRads)
        } else if ((decRads > 0) and (latRads < 0)) {
            // If dec and lat are in different hemispheres, then dec should be made negative.
            decRads *= -1
            latRads *= -1
        }

        // Calculate Hc
        val HcRads = asin(sin(latRads) * sin(decRads) + cos(latRads) * cos(decRads) * cos(LHARads))
        val Hc = degrees(HcRads)

        // Calculate Z
        val ZRads = acos((sin(decRads) - sin(HcRads) * sin(latRads)) / (cos(HcRads) * cos(latRads)))
        var Z = degrees(ZRads)
        if (Z < 0) {
            // If Z is negative add 180
            Z+=180.0
        }

        // Calculate Zn.  The rule here is based on latitude and LHA values.
        var Zn = if (latitude >= 0) {
            // Northern Latitude adjustment
            if (LHA >= 180.0) Z else (360.0 - Z)
        } else {
            // Southern Latitude adjustment
            if (LHA >= 180.0) (180.0 - Z) else (180.0 + Z)
        }

        if (Zn >= 360.0)
            Zn-=360.0
        if (Zn < 0)
            Zn += 360.0

        return Triple(Hc, Z, Zn)
    }

    fun calculateDestination(latitude: Double, longitude: Double, bearing: Double, distance: Double):Pair<Double, Double> {
        val( destLatitude, destLongitude) = calculateDestinationRadians(radians(latitude), radians(longitude), radians(bearing), distance)
        return Pair(degrees(destLatitude), degrees(destLongitude))
    }

    private fun calculateDestinationRadians(latitude: Double, longitude: Double, bearing: Double, distance: Double):Pair<Double, Double> {
        val distanceKm = nauticalMilesToKm(distance)
        val angularDistance = distanceKm / equatorialRadiusEarth

        val destLatitude = asin(sin(latitude) * cos(angularDistance) + cos(latitude) * sin(angularDistance) * cos(bearing))
        val deltaLongitude = atan2(sin(bearing) * sin(angularDistance) * cos(latitude), cos(angularDistance) - sin(latitude) * sin(destLatitude))
        val destLongitude = longitude + deltaLongitude
        return Pair(destLatitude, destLongitude)
    }

    // Test Data
    //const val utc: String = "1978/7/25 04:07:02"
    val altairAntaresLat = angle(44, 36.0, North)
    val altairAntaresLon = angle(122, 14.0, West)

    val altairHs = angle(30, 35.4)
    val altairGHA = angle(66, 55.19)
    val altairDec = angle(8, 48.87, North)
    val altairDistance = 158695484120317.78

    val antaresHs = angle(18, 54.3)
    val antaresGHA = angle(117, 19.98)
    val antaresDec = angle(26, 23.05, South)
    val antaresDistance = 5714217650853814.0

    val konaLat = angle(19, 39.51, North)
    val konaLon = angle(155, 59.77, West)
}