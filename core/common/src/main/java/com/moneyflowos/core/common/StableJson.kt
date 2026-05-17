package com.moneyflowos.core.common

object StableJson {
  fun obj(values: Map<String, Any?>): String {
    val keys = values.keys.sorted()
    val sb = StringBuilder()
    sb.append('{')
    for ((index, key) in keys.withIndex()) {
      if (index > 0) sb.append(',')
      sb.append('"').append(escape(key)).append('"').append(':').append(valueToJson(values[key]))
    }
    sb.append('}')
    return sb.toString()
  }

  private fun valueToJson(value: Any?): String {
    return when (value) {
      null -> "null"
      is Number -> value.toString()
      is Boolean -> if (value) "true" else "false"
      else -> "\"${escape(value.toString())}\""
    }
  }

  private fun escape(text: String): String {
    val sb = StringBuilder(text.length + 8)
    for (ch in text) {
      when (ch) {
        '\\' -> sb.append("\\\\")
        '"' -> sb.append("\\\"")
        '\n' -> sb.append("\\n")
        '\r' -> sb.append("\\r")
        '\t' -> sb.append("\\t")
        else -> sb.append(ch)
      }
    }
    return sb.toString()
  }
}

