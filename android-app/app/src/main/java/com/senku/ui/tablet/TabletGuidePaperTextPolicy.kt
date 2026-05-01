package com.senku.ui.tablet

internal data class TabletGuideRequiredReadingParts(
    val label: String,
    val id: String,
    val title: String,
)

internal enum class TabletGuideBodyLineKind {
    Skip,
    RequiredReading,
    Danger,
    Section,
    Body,
}

internal fun tabletGuideBodyLineKindForTest(line: String): TabletGuideBodyLineKind =
    tabletGuideBodyLineKind(line)

internal fun tabletGuideBodyTitleLineForSkip(text: String): String =
    text.lineSequence()
        .map { normalizeGuidePaperTokenLine(it) }
        .firstOrNull { line ->
            line.equals("Foundry & Metal Casting", ignoreCase = true)
        }
        .orEmpty()

internal fun tabletGuideBodyLineKind(line: String): TabletGuideBodyLineKind {
    val trimmed = line.trim()
    if (trimmed.isEmpty()) return TabletGuideBodyLineKind.Skip
    val upper = trimmed.uppercase()
    if (upper.startsWith("START WITH ")) return TabletGuideBodyLineKind.Body
    if (upper.startsWith("FIELD MANUAL")) return TabletGuideBodyLineKind.Skip
    if (Regex("^GD-\\d+\\b.*\\bSECTIONS?\\b").containsMatchIn(upper)) return TabletGuideBodyLineKind.Skip
    if (upper.startsWith("REQUIRED READING")) return TabletGuideBodyLineKind.RequiredReading
    if (Regex("^GD-\\d+\\b").containsMatchIn(upper)) return TabletGuideBodyLineKind.RequiredReading
    if (upper.startsWith("DANGER") ||
        upper.contains("MOLTEN METAL") ||
        upper.contains("COMPLETELY DRY")) {
        return TabletGuideBodyLineKind.Danger
    }
    if (upper.startsWith("SEC ") ||
        upper.startsWith("- \u00A7") ||
        upper.startsWith("\u2014 \u00A7") ||
        upper.startsWith("- \u00C2\u00A7") ||
        upper.startsWith("\u00E2\u20AC\u201D \u00C2\u00A7") ||
        upper.startsWith("AREA READINESS")) {
        return TabletGuideBodyLineKind.Section
    }
    return TabletGuideBodyLineKind.Body
}

internal fun normalizeGuidePaperRequiredReadingLine(line: String): String =
    normalizeGuidePaperMojibake(normalizeGuidePaperTokenLine(line))
        .replace(Regex("\\s+"), " ")
        .replace("REQUIRED READING \u00B7", "REQUIRED READING  \u00B7")

internal fun parseGuideRequiredReadingParts(line: String): TabletGuideRequiredReadingParts {
    val normalized = normalizeGuidePaperMojibake(normalizeGuidePaperTokenLine(line))
    val tokens = normalized
        .split('\u00B7')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
    val label = tokens.firstOrNull()?.takeIf { it.equals("REQUIRED READING", ignoreCase = true) }
        ?: "REQUIRED READING"
    val idIndex = tokens.indexOfFirst { Regex("^GD-\\d+\\b", RegexOption.IGNORE_CASE).containsMatchIn(it) }
    val id = tokens.getOrNull(idIndex)?.trim().orEmpty()
    val title = if (idIndex >= 0) {
        tokens.drop(idIndex + 1).joinToString(" \u00B7 ")
    } else {
        tokens.drop(1).joinToString(" \u00B7 ")
    }
    return TabletGuideRequiredReadingParts(
        label = label.uppercase(),
        id = id.uppercase(),
        title = title,
    )
}

internal fun normalizeGuidePaperDangerLine(line: String): String =
    normalizeGuidePaperMojibake(normalizeGuidePaperTokenLine(line))
        .replace(Regex("\\s+"), " ")
        .replace("DANGER \u00B7", "DANGER  \u00B7")

internal fun normalizeGuidePaperSectionLine(line: String): String =
    normalizeGuidePaperMojibake(normalizeGuidePaperTokenLine(line)).replace(Regex("\\s+"), " ")

internal fun normalizeGuidePaperTokenLine(line: String): String =
    line.trim()
        .replace("\u00C2\u00A7", "\u00A7")
        .replace("\u00C2\u00B7", "\u00B7")
        .replace("\u00E2\u20AC\u201D", "\u2014")
        .replace(Regex("\\s+"), " ")

private fun normalizeGuidePaperMojibake(line: String): String =
    line.replace("\u00C2\u00A7", "\u00A7")
        .replace("\u00C2\u00B7", "\u00B7")
        .replace("\u00E2\u20AC\u201D", "\u2014")

internal fun parseGuideSectionAnchorTitle(line: String): String? {
    val match = Regex("^[\\-\u2014]?\\s*\u00A7\\s*\\d+\\s*[\u00B7.:-]\\s*(.+)$").find(line)
    val title = match?.groupValues?.getOrNull(1)?.trim()
        ?: line.takeIf { it.equals("AREA READINESS", ignoreCase = true) }
    return title
        ?.lowercase()
        ?.replace(Regex("\\s+"), " ")
        ?.replaceFirstChar { char -> char.titlecase() }
}
