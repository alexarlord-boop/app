package com.example.app


fun <T> sortRecordsByHouseNumber(records: List<T>, houseNumberExtractor: (T) -> String): List<T> {

    val nonLetterRegex = Regex("""\P{L}""")
    val separatorRegex = Regex("""[^\p{L}\p{N}]+""")

    return records.sortedWith(
        compareBy(
            /* number part prim */  {houseNumberExtractor(it).split(separatorRegex).getOrNull(0)?.filter { char -> char.isDigit() }?.toIntOrNull()},
            /* number part sec  */  {houseNumberExtractor(it).split(separatorRegex).getOrNull(1)?.toIntOrNull()},
            /* letter part */       {houseNumberExtractor(it).replace(nonLetterRegex, "").filter { char -> char.isLetter() }.lowercase()}
        )
    )
}


fun parseNumberInput(value: String): String {
    // The Regex("[^0-9]") specifies a regular expression pattern that matches any character that is not a digit (0-9).
    // The replace function is then used to replace all occurrences of non-digit characters with an empty string.
    return value.replace(Regex("[^0-9]"), "")
}


