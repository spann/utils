package in.spann

import java.sql.Timestamp
import java.util.Calendar
import java.util.regex.Pattern

object ReplaceValuesWithNames {

  val regexs: List[Regex] = List(
    Regex("\\{staged_dir(.*?)", "staged_dir(.*?)('|\"| )", "\\${staged_dir"),
    Regex("\\{log_dir", "log_dir(.*?)('|\"| )", "\\${log_dir"),
    Regex("\\{raw_dir", "raw_dir(.*?)('|\"| )", "\\${raw_dir")
  )

  def main(args: Array[String]) {
    val files = Common.getAllFiles

    val fileAndVariables = files.map { file =>
      println("\n\n\n\n\n\n\nFile: " + file)

      val lines = Common.getInputFile(file).getLines().toList

      val requiredVariables = lines.flatMap { line =>
        regexs.flatMap { regex =>
          val extractor = Pattern.compile(regex.patternToFind).matcher(line)
          if (extractor.find()) {
            val extractor2 = Pattern.compile(regex.patternToExtract).matcher(line)
            extractor2.find()
            val path = (regex.pathPrefix ++ extractor2.group(1)).trim
            print(path + " : ")
            val pathVariable = "a" //Console.readLine()
            Some(Output(pathVariable, path))
          } else {
            None
          }
        }
      }

      (file, requiredVariables)
    }

    fileAndVariables.foreach { case (file, variables) =>
      val now = Calendar.getInstance().getTime().getTime.toString
      val fileName = now + ".txt"
        val lines = List(file) ++ variables.map(x => x.variableName + ": " + x.path)
      Common.writeToOutputFile(lines, fileName)
    }

    fileAndVariables.foreach { case (file, variables) =>
      val lines = Common.getInputFile(file).getLines().toList
      val finalLines = lines.map { line =>
        var result = line
        variables.foreach { variable =>
          if (line.indexOf(variable.path) >= 0) {
            result = line.replaceAllLiterally(variable.path, "\\${" + variable.variableName + "}")
          }
        }

        result

        //      variables.foreach(println)
      }

      Common.writeToOutputFile(finalLines, file)
    }
  }

  case class Regex(patternToFind: String, patternToExtract: String, pathPrefix: String)

  case class Output(variableName: String, path: String)

}
