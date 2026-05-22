package com.shrimpadvisor.plcycle.ui

import android.content.Context
import com.shrimpadvisor.plcycle.data.DailyReading
import com.shrimpadvisor.plcycle.data.PondCycle
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object XlsxExporter {

    fun export(context: Context, cycle: PondCycle, readings: List<DailyReading>): File {
        val safeName = cycle.pondName.replace(Regex("[^A-Za-z0-9_-]"), "_")
        val file = File(context.cacheDir, "${safeName}_readings.xlsx")
        ZipOutputStream(file.outputStream().buffered()).use { zip ->
            writeEntry(zip, "[Content_Types].xml", contentTypesXml())
            writeEntry(zip, "_rels/.rels", relsXml())
            writeEntry(zip, "xl/workbook.xml", workbookXml())
            writeEntry(zip, "xl/_rels/workbook.xml.rels", workbookRelsXml())
            writeEntry(zip, "xl/styles.xml", stylesXml())
            writeEntry(zip, "xl/worksheets/sheet1.xml", sheetXml(cycle, readings))
        }
        return file
    }

    private fun writeEntry(zip: ZipOutputStream, name: String, content: String) {
        zip.putNextEntry(ZipEntry(name))
        zip.write(content.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }

    private fun contentTypesXml() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
  <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
</Types>"""

    private fun relsXml() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""

    private fun workbookXml() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
          xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets>
    <sheet name="Daily Readings" sheetId="1" r:id="rId1"/>
  </sheets>
</workbook>"""

    private fun workbookRelsXml() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>"""

    private fun stylesXml() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <fonts count="1"><font><sz val="11"/><name val="Calibri"/></font></fonts>
  <fills count="2">
    <fill><patternFill patternType="none"/></fill>
    <fill><patternFill patternType="gray125"/></fill>
  </fills>
  <borders count="1"><border><left/><right/><top/><bottom/><diagonal/></border></borders>
  <cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
  <cellXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/></cellXfs>
</styleSheet>"""

    private fun sheetXml(cycle: PondCycle, readings: List<DailyReading>): String {
        val sorted = readings.sortedBy { it.pondAge }
        val headers = listOf(
            "Day (DOC)", "Survival %", "DO (mg/L)", "TAN (mg/L)", "pH",
            "Temp (°C)", "ABW (g)", "Feed Given (kg)", "Notes"
        )
        return buildString {
            appendLine("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
            appendLine("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">""")
            appendLine("""  <cols>""")
            appendLine("""    <col min="1" max="1" width="8" customWidth="1"/>""")
            appendLine("""    <col min="2" max="8" width="13" customWidth="1"/>""")
            appendLine("""    <col min="9" max="9" width="22" customWidth="1"/>""")
            appendLine("""  </cols>""")
            appendLine("""  <sheetData>""")

            // Header row at row 1
            append("""    <row r="1">""")
            headers.forEachIndexed { i, h -> append(strCell("${colLetter(i)}1", h)) }
            appendLine("</row>")

            // Data rows from row 2
            sorted.forEachIndexed { i, r ->
                val row = i + 2
                append("""    <row r="$row">""")
                append(numCell("A$row", r.pondAge.toDouble()))
                append(numCell("B$row", r.survivalPct))
                append(numCell("C$row", r.doLevel))
                append(numCell("D$row", r.tanLevel))
                append(numCell("E$row", r.ph))
                append(numCell("F$row", r.temp))
                append(numCell("G$row", r.abw))
                append(numCell("H$row", r.feedGiven))
                if (r.notes.isNotBlank()) append(strCell("I$row", r.notes))
                appendLine("</row>")
            }

            appendLine("""  </sheetData>""")
            append("""</worksheet>""")
        }
    }

    // Converts 0-based column index to Excel letter(s): 0→A, 25→Z, 26→AA
    private fun colLetter(index: Int): String {
        var n = index + 1
        val sb = StringBuilder()
        while (n > 0) {
            n--
            sb.insert(0, ('A' + n % 26).toChar())
            n /= 26
        }
        return sb.toString()
    }

    private fun numCell(ref: String, value: Double): String {
        val formatted = if (value == Math.floor(value) && value.isFinite()) value.toLong().toString()
                        else value.toString()
        return """<c r="$ref"><v>$formatted</v></c>"""
    }

    private fun strCell(ref: String, text: String) =
        """<c r="$ref" t="inlineStr"><is><t>${xmlEscape(text)}</t></is></c>"""

    private fun xmlEscape(s: String) = s
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
}
