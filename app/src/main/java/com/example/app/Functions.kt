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

