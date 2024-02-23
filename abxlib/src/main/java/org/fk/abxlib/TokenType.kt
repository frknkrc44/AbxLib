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

object TokenType {
    const val START_DOCUMENT: Int = 0
    const val END_DOCUMENT: Int = 1
    const val START_TAG: Int = 2
    const val END_TAG: Int = 3
    const val TEXT: Int = 4

    /*
     CDSECT = 5,
     ENTITY_REF = 6,
     IGNORABLE_WHITESPACE = 7,
     PROCESSING_INSTRUCTION = 8,
     COMMENT = 9,
     DOCDECL = 10,
     */
    const val ATTRIBUTE: Int = 15
}