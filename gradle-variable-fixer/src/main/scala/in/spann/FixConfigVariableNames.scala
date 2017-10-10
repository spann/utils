package in.spann

import java.util.regex.Pattern

import in.spann.ReplaceValuesWithNames.Regex

object FixConfigVariableNames {
  val excluded = List("log_dir =")

  def main(args: Array[String]) {
    val file = "/Users/tkmacgf/sunil/projects/bigdata-recommendation-engine-config/config/ddh/dev-2.6/variables.yml"
    val lines = Common.getInputFile(file).getLines().toList
    val newLines = lines.map { line =>
      if (line.contains("_dir") && (line.contains("staged") || line.contains("raw") || line.contains("log")) && !line.startsWith("log_dir") && !line.startsWith("staged_dir") && !line.startsWith("raw_dir")
        && !line.startsWith("core_root")
        && !line.startsWith("customer_root")
        && !line.startsWith("similar_root")
        && !line.startsWith("search_root")
        && !line.startsWith("hbase_root")
        && !line.startsWith("trending_root")
        && !line.startsWith("lookup_root")
        && !line.startsWith("frequent_root")
      ) {
        val pattern = ": (.*?)$"
        val extractor = Pattern.compile(pattern).matcher(line)
        if (extractor.find()) {
          val path = extractor.group(1).trim
          val pathVariable = path.replaceAllLiterally("\"", "").replaceAllLiterally("/", "_").replaceAllLiterally("}", "").replaceAllLiterally("{", "").replaceAllLiterally("\\${", "").replaceAll("-", "_").replaceAllLiterally(".", "_").replaceAllLiterally("*", "").replaceAll("([a-z])([A-Z]+)", "$1_$2").stripPrefix("_").stripSuffix("_").toLowerCase.replaceAllLiterally("staged_dir_", "").replaceAllLiterally("raw_dir_", "core_raw_")

          val extractor2 = Pattern.compile("^(.*?):").matcher(line)
          extractor2.find()
          val oldVariableName = extractor2.group(1).trim

          if (oldVariableName != pathVariable) {
            println(oldVariableName + " = " + pathVariable + " = " + path)
          }
          val newLine = pathVariable + ": " + path
          newLine
        } else {
          line
        }
      } else {
        line
      }
    }

//    println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n")
//            newLines.foreach(println)
    Common.writeToOutputFile(newLines, file)
  }
}
