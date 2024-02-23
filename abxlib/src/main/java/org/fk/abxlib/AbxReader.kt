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

import java.io.EOFException
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.util.Base64

@Suppress("unused")
class AbxReader {
    private val internedStrings = ArrayList<String>()
    private val inputStream: InputStream
    private var tree: AbxXMLElement? = null

    constructor(fileName: String) {
        inputStream = FileInputStream(fileName)
    }

    constructor(file: File) {
        inputStream = FileInputStream(file)
    }

    constructor(stream: InputStream) {
        inputStream = stream
    }

    companion object {
       const val START_MAGIC: String = "ABX\u0000"
    }

    @Throws(IOException::class)
    fun startRead() {
        if (!isAbx) {
            throw IOException("Invalid file")
        }

        tree = null
        internedStrings.clear()
        var docOpen = false
        var rootClosed = false
        val elementStack = ArrayList<AbxXMLElement>()
        var root: AbxXMLElement? = null

        loop@ while (true) {
            try {
                val token = readByte().toInt()
                val tType = token and 0x0f
                val dType = token and 0xf0
                when (tType /* Token type */) {
                    TokenType.START_DOCUMENT -> {
                        if (dType != DataType.NULL || docOpen) {
                            throw IOException("START_DOCUMENT with an invalid data type")
                        }
                        docOpen = true
                    }

                    TokenType.END_DOCUMENT -> {
                        if (dType != DataType.NULL || elementStack.isNotEmpty()) {
                            throw IOException("END_DOCUMENT with an invalid data type")
                        }
                        break@loop
                    }

                    TokenType.START_TAG -> {
                        if (dType != DataType.STRING_INTERNED || !docOpen || rootClosed) {
                            throw IOException("START_TAG with an invalid data type")
                        }

                        val int1 = readInternedString()
                        val subElement = AbxXMLElement(int1)
                        if (elementStack.isNotEmpty()) {
                            elementStack.last().subElements.add(subElement)
                        }
                        elementStack.add(subElement)
                    }

                    TokenType.END_TAG -> {
                        if (dType != DataType.STRING_INTERNED || !docOpen || rootClosed || elementStack.isEmpty()) {
                            throw IOException("END_TAG with an invalid data type")
                        }

                        val int2 = readInternedString()
                        val last1 = elementStack.last()
                        if (last1.tagName != int2) {
                            throw IOException("START_TAG and END_TAG mismatch")
                        }

                        elementStack.remove(last1)
                        if (elementStack.isEmpty()) {
                            rootClosed = true
                            root = last1
                        }
                    }

                    TokenType.TEXT -> {
                        val raw1 = readString()
                        val last2 = elementStack.last()
                        last2.text = if (last2.text == null) raw1 else (last2.text + raw1)
                    }

                    TokenType.ATTRIBUTE -> {
                        if (elementStack.size < 1) {
                            throw IOException("ATTRIBUTE without any elements left")
                        }

                        val last3 = elementStack[elementStack.size - 1]
                        val attrName = readInternedString()
                        if (last3.attributes.containsKey(attrName)) {
                            throw IOException("ATTRIBUTE name $attrName already in target element")
                        }

                        var data: String? = null
                        when (dType) {
                            DataType.NULL -> {}
                            DataType.BOOLEAN_TRUE -> data = true.toString()
                            DataType.BOOLEAN_FALSE -> data = false.toString()
                            DataType.INT -> data = readInt().toString()
                            DataType.INT_HEX -> data = Integer.toHexString(readInt())
                            DataType.LONG -> data = readLong().toString()
                            DataType.LONG_HEX -> data =
                                java.lang.Long.toHexString(readLong())

                            DataType.FLOAT -> data = readFloat().toString()
                            DataType.DOUBLE -> data = readDouble().toString()
                            DataType.STRING -> data = readString()
                            DataType.STRING_INTERNED -> data = readInternedString()
                            DataType.BYTES_HEX -> {
                                val buf1 = readRaw(readShort().toInt())
                                val sb = StringBuilder()
                                for (b in buf1) {
                                    sb.append(String.format("%02x", b))
                                }
                                data = sb.toString()
                            }

                            DataType.BYTES_BASE64 -> {
                                val buf2 = readRaw(readShort().toInt())
                                data = String(Base64.getDecoder().decode(buf2))
                            }

                            else -> throw IOException(String.format("Invalid data type %x", dType))
                        }
                        last3.attributes[attrName] = AbxXMLAttribute(dType, data)
                    }

                    else -> System.out.printf("Invalid token type %x %x%n", tType, dType)
                }
            } catch (e: EOFException) {
                break
            }
        }


        if (!rootClosed) {
            throw IOException("Elements still in the stack when completing the document")
        }

        tree = root
    }

    @Throws(IOException::class)
    private fun readRaw(length: Int): ByteArray {
        val buf = ByteArray(length)
        val len = inputStream.read(buf)
        if (len != length) {
            throw EOFException()
        }

        return buf
    }

    @Throws(IOException::class)
    private fun readRawString(length: Int) = String(readRaw(length))

    @Throws(IOException::class)
    private fun readByteBuffer(length: Int) = ByteBuffer.wrap(readRaw(length))

    @Throws(IOException::class)
    private fun readByte() = readRaw(1).first()

    @Throws(IOException::class)
    private fun readShort() = readByteBuffer(2).getShort()

    @Throws(IOException::class)
    private fun readInt() = readByteBuffer(4).getInt()

    @Throws(IOException::class)
    private fun readLong() = readByteBuffer(8).getLong()

    @Throws(IOException::class)
    private fun readFloat() = readByteBuffer(4).getFloat()

    @Throws(IOException::class)
    private fun readDouble() = readByteBuffer(8).getDouble()

    @Throws(IOException::class)
    private fun readString(): String {
        val length = readShort()
        if (length < 0) {
            throw IOException("Negative string length")
        }

        return readRawString(length.toInt())
    }

    @Throws(IOException::class)
    private fun readInternedString(): String {
        val ref = readShort()

        return if (ref < 0) {
            val str = readString()
            internedStrings.add(str)
            str
        } else {
            internedStrings[ref.toInt()]
        }
    }

    @get:Throws(IOException::class)
    private val isAbx: Boolean
        get() = START_MAGIC == readRawString(4)

    fun getTree() = tree
}
