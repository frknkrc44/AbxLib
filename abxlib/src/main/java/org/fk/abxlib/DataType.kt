/*
 *  This file is part of abxlib.
 *
 *  abxlib is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  abxlib is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *   along with abxlib.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.fk.abxlib

@Suppress("unused")
object DataType {
    const val NULL: Int = 1 shl 4
    const val STRING: Int = 2 shl 4
    const val STRING_INTERNED: Int = 3 shl 4
    const val BYTES_HEX: Int = 4 shl 4
    const val BYTES_BASE64: Int = 5 shl 4
    const val INT: Int = 6 shl 4
    const val INT_HEX: Int = 7 shl 4
    const val LONG: Int = 8 shl 4
    const val LONG_HEX: Int = 9 shl 4
    const val FLOAT: Int = 10 shl 4
    const val DOUBLE: Int = 11 shl 4
    const val BOOLEAN_TRUE: Int = 12 shl 4
    const val BOOLEAN_FALSE: Int = 13 shl 4

    fun getDataTypeString(type: Int): String? {
        for (f in DataType::class.java.declaredFields) {
            try {
                val o = f.getInt(null)
                if (o == type) {
                    return f.name
                }
            } catch (ignored: Throwable) {}
        }

        return null
    }
}