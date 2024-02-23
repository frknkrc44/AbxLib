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

class AbxXMLElement(val tagName: String) {
    var text: String? = null
    val attributes: HashMap<String, AbxXMLAttribute> = LinkedHashMap()
    val subElements: ArrayList<AbxXMLElement> = ArrayList()

    private fun parseElement(element: AbxXMLElement, sb: StringBuilder) {
        sb.append('<').append(element.tagName)
        if (element.attributes.isNotEmpty()) {
            for (str in element.attributes.keys) {
                sb.append(' ')
                    .append(str)
                    .append("=\"")
                    .append(element.attributes[str])
                    .append('"')
            }
        }

        if (element.text != null) {
            if (element.subElements.isNotEmpty()) {
                throw RuntimeException("element.text != null && element.subElements.size() > 0")
            }

            sb.append('>')
            sb.append(element.text)
            sb.append("</").append(element.tagName).append('>')
        }

        if (element.subElements.isNotEmpty()) {
            sb.append(">\n")
            for (sub in element.subElements) {
                parseElement(sub, sb)
            }
            sb.append("</").append(element.tagName).append(">\n")
        } else {
            sb.append("/>\n")
        }
    }

    override fun toString(): String {
        val sb = StringBuilder()
        parseElement(this, sb)
        sb.delete(sb.length - 1, sb.length)
        return sb.toString()
    }
}