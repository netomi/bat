package com.github.netomi.bat.smali.disassemble

import java.text.DecimalFormat

internal object Numbers {
    private val CANONICAL_FLOAT_NAN_BITS  = java.lang.Float.floatToRawIntBits(Float.NaN)
    private val MAX_FLOAT_BITS            = java.lang.Float.floatToRawIntBits(Float.MAX_VALUE)
    private val PI_FLOAT_BITS             = java.lang.Float.floatToRawIntBits(Math.PI.toFloat())
    private val E_FLOAT_BITS              = java.lang.Float.floatToRawIntBits(Math.E.toFloat())
    private val CANONICAL_DOUBLE_NAN_BITS = java.lang.Double.doubleToRawLongBits(Double.NaN)
    private val MAX_DOUBLE_BITS           = java.lang.Double.doubleToLongBits(Double.MAX_VALUE)
    private val PI_DOUBLE_BITS            = java.lang.Double.doubleToLongBits(Math.PI)
    private val E_DOUBLE_BITS             = java.lang.Double.doubleToLongBits(Math.E)

    private val format = DecimalFormat("0.####################E0")

    fun isLikelyFloat(value: Int): Boolean {
        // Check for some common named float values
        // We don't check for Float.MIN_VALUE, which has an integer representation of 1
        if (value == CANONICAL_FLOAT_NAN_BITS || value == MAX_FLOAT_BITS || value == PI_FLOAT_BITS || value == E_FLOAT_BITS) {
            return true
        }

        // Check for some named integer values
        if (value == Int.MAX_VALUE || value == Int.MIN_VALUE) {
            return false
        }

        // Check for likely resource id
        val packageId    = value shr 24
        val resourceType = value shr 16 and 0xff
        val resourceId   = value and 0xffff
        if ((packageId == 0x7f || packageId == 1) && resourceType < 0x1f && resourceId < 0xfff) {
            return false
        }

        // a non-canonical NaN is more likely to be an integer
        val floatValue = java.lang.Float.intBitsToFloat(value)
        if (java.lang.Float.isNaN(floatValue)) {
            return false
        }

        // Otherwise, whichever has a shorter scientific notation representation is more likely.
        // Integer wins the tie
        val asInt   = format.format(value.toLong())
        var asFloat = format.format(floatValue.toDouble())

        // try to strip off any small imprecision near the end of the mantissa
        val decimalPoint = asFloat.indexOf('.')
        val exponent = asFloat.indexOf("E")
        val zeros = asFloat.indexOf("000")
        if (zeros > decimalPoint && zeros < exponent) {
            asFloat = asFloat.substring(0, zeros) + asFloat.substring(exponent)
        } else {
            val nines = asFloat.indexOf("999")
            if (nines > decimalPoint && nines < exponent) {
                asFloat = asFloat.substring(0, nines) + asFloat.substring(exponent)
            }
        }
        return asFloat.length < asInt.length
    }

    fun isLikelyDouble(value: Long): Boolean {
        // Check for some common named double values
        // We don't check for Double.MIN_VALUE, which has a long representation of 1
        if (value == CANONICAL_DOUBLE_NAN_BITS || value == MAX_DOUBLE_BITS || value == PI_DOUBLE_BITS || value == E_DOUBLE_BITS) {
            return true
        }

        // Check for some named long values
        if (value == Long.MAX_VALUE || value == Long.MIN_VALUE) {
            return false
        }

        // a non-canonical NaN is more likely to be an long
        val doubleValue = java.lang.Double.longBitsToDouble(value)
        if (java.lang.Double.isNaN(doubleValue)) {
            return false
        }

        // Otherwise, whichever has a shorter scientific notation representation is more likely.
        // Long wins the tie
        val asLong = format.format(value)
        var asDouble = format.format(doubleValue)

        // try to strip off any small imprecision near the end of the mantissa
        val decimalPoint = asDouble.indexOf('.')
        val exponent     = asDouble.indexOf("E")
        val zeros        = asDouble.indexOf("000")
        if (zeros > decimalPoint && zeros < exponent) {
            asDouble = asDouble.substring(0, zeros) + asDouble.substring(exponent)
        } else {
            val nines = asDouble.indexOf("999")
            if (nines > decimalPoint && nines < exponent) {
                asDouble = asDouble.substring(0, nines) + asDouble.substring(exponent)
            }
        }
        return asDouble.length < asLong.length
    }
}