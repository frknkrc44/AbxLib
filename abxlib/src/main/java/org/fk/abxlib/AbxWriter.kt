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

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.nio.ByteBuffer
import java.util.Base64
import java.util.LinkedList
import java.util.regex.Pattern

@Suppress("unused")
class AbxWriter {
    private val internedStrings = LinkedList<String>()
    private val outputStream: OutputStream

    constructor(outputStream: OutputStream) {
        this.outputStream = outputStream
    }

    @Throws(IOException::class)
    constructor(file: String) : this(File(file))

    @Throws(IOException::class)
    constructor(fileObj: File) {
        if (!fileObj.exists()) {
            val parent = fileObj.parentFile!!
            if (!parent.exists() && !parent.mkdirs()) {
                throw IOException("Parent mkdirs failed!")
            }
        } else if (!fileObj.delete()) {
            throw IOException("Delete file failed!")
        }

        if (!fileObj.createNewFile()) {
            throw IOException("Create new file failed!")
        }
        outputStream = FileOutputStream(fileObj)
    }

    @Throws(IOException::class)
    private fun writeSubElement(element: AbxXMLElement) {
        writeByte(((TokenType.START_TAG or DataType.STRING_INTERNED) and 0xff).toByte())
        writeInternedString(element.tagName)

        if (element.attributes.size > 0) {
            for (str in element.attributes.keys) {
                val attr = element.attributes[str]!!
                writeByte(((TokenType.ATTRIBUTE or attr.dataType) and 0xff).toByte())
                writeInternedString(str)
                when (attr.dataType) {
                    DataType.NULL,
                    DataType.BOOLEAN_TRUE,
                    DataType.BOOLEAN_FALSE -> continue
                    DataType.INT -> writeInt(attr.value.toInt())
                    DataType.INT_HEX -> writeIntHex(attr.value)
                    DataType.LONG -> writeLong(attr.value.toLong())
                    DataType.LONG_HEX -> writeLongHex(attr.value)
                    DataType.FLOAT -> writeFloat(attr.value.toFloat())
                    DataType.DOUBLE -> writeDouble(attr.value.toDouble())
                    DataType.STRING -> writeString(attr.value)
                    DataType.STRING_INTERNED -> writeInternedString(attr.value)
                    DataType.BYTES_HEX -> {
                        val pattern = Pattern.compile(".{1,2}")
                        val matcher = pattern.matcher(attr.value)
                        val byteArrayOutputStream = ByteArrayOutputStream()
                        while (matcher.find()) {
                            val sub = attr.value.substring(matcher.start(), matcher.end())
                            val b = sub.toInt(16)
                            byteArrayOutputStream.write(b)
                        }
                        byteArrayOutputStream.close()
                        writeShort(byteArrayOutputStream.size().toShort())
                        writeRaw(byteArrayOutputStream.toByteArray())
                    }

                    DataType.BYTES_BASE64 -> {
                        val buf = Base64.getEncoder().encode(attr.value.toByteArray())
                        writeShort(buf.size.toShort())
                        writeRaw(buf)
                    }
                }
            }
        }

        if (element.text != null) {
            writeString(element.text!!)
        }

        if (element.subElements.isNotEmpty()) {
            for (sub in element.subElements) {
                writeSubElement(sub)
            }
        }

        writeByte(((TokenType.END_TAG or DataType.STRING_INTERNED) and 0xff).toByte())
        writeInternedString(element.tagName)
    }

    @Throws(IOException::class)
    fun startWrite(element: AbxXMLElement) {
        internedStrings.clear()
        writeRaw(AbxReader.START_MAGIC.toByteArray())
        writeByte(((TokenType.START_DOCUMENT or DataType.NULL) and 0xff).toByte())
        writeSubElement(element)
        writeByte(((TokenType.END_DOCUMENT or DataType.NULL) and 0xff).toByte())
        outputStream.close()
    }

    @Throws(IOException::class)
    private fun writeRaw(buf: ByteArray) = outputStream.write(buf)

    @Throws(IOException::class)
    private fun writeByte(b: Byte) = writeRaw(byteArrayOf(b))

    @Throws(IOException::class)
    private fun writeShort(sh: Short) = writeRaw(ByteBuffer.allocate(2).putShort(sh).array())

    @Throws(IOException::class)
    private fun writeInt(i: Int) = writeRaw(ByteBuffer.allocate(4).putInt(i).array())

    @Throws(IOException::class)
    private fun writeIntHex(str: String) = writeInt(str.toInt(16))

    @Throws(IOException::class)
    private fun writeLong(l: Long) = writeRaw(ByteBuffer.allocate(8).putLong(l).array())

    @Throws(IOException::class)
    private fun writeLongHex(str: String) = writeLong(str.toLong(16))

    @Throws(IOException::class)
    private fun writeFloat(f: Float) = writeRaw(ByteBuffer.allocate(4).putFloat(f).array())

    @Throws(IOException::class)
    private fun writeDouble(d: Double) = writeRaw(ByteBuffer.allocate(8).putDouble(d).array())

    @Throws(IOException::class)
    private fun writeString(str: String) {
        writeShort(str.length.toShort())
        writeRaw(str.toByteArray())
    }

    @Throws(IOException::class)
    private fun writeInternedString(str: String) {
        if (internedStrings.contains(str)) {
            writeShort(internedStrings.indexOf(str).toShort())
        } else {
            internedStrings.add(str)
            writeShort((-1).toShort())
            writeString(str)
        }
    }
}
