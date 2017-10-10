package in.spann

import java.sql.Timestamp
import java.util.Calendar
import java.util.regex.Pattern

object ReplaceValuesWithNames {

  val regexs: List[Regex] = List(
    Regex("\\{staged_dir(.*?)", "staged_dir(.*?)('|\"| |\\\\|\\|)", "\\${staged_dir"),
    Regex("\\{log_dir", "log_dir(.*?)('|\"| |\\\\|\\|)", "\\${log_dir"),
    Regex("\\{raw_dir", "raw_dir(.*?)('|\"| |\\\\|\\|)", "\\${raw_dir")
  )
//  val regexs: List[Regex] = List(
//    Regex("\\{staged_dir(.*?)", "staged_dir(.*?)('|\"| |\\\\|\\|)", "\\${staged_dir"),
//    Regex("\\{log_dir", "log_dir(.*?)('|\"| |\\\\|\\|)", "\\${log_dir"),
//    Regex("\\{raw_dir", "raw_dir(.*?)('|\"| |\\\\|\\|)", "\\${raw_dir")
//  )

  def main(args: Array[String]) {
    val files = Common.getAllFiles()

    val fileAndVariables = files.map { file =>
//      println("\n\n\n\n\n\n\nFile: " + file)

      val lines = Common.getInputFile(file).getLines().toList

      val requiredVariables = lines.flatMap { line =>
        regexs.flatMap { regex =>
          val extractor = Pattern.compile(regex.patternToFind).matcher(line)
          if (extractor.find()) {
            val extractor2 = Pattern.compile(regex.patternToExtract).matcher(line)
            extractor2.find()
            val path = (regex.pathPrefix ++ extractor2.group(1)).trim
//            print(path + " : ")
            val pathVariable = path.replaceAll("/","_").replaceAll("}","").replaceAllLiterally("\\${","").replaceAll("-","_").replaceAllLiterally(".", "_").replaceAllLiterally("*", "").replaceAll("([a-z])([A-Z]+)","$1_$2").stripPrefix("_").stripSuffix("_").toLowerCase.replaceAll("staged_dir_","").replaceAll("raw_dir_","core_raw_")
            Some(Output(pathVariable, path))
          } else {
            None
          }
        }
      }

      (file, requiredVariables)
    }

    val writableLines = fileAndVariables.flatMap { case (file, variables) =>
      variables.map{x =>
        val formattedPath = x.path.replaceAllLiterally("\\${","{{").replaceAllLiterally("}","}}")
        x.variableName + ": \"" + formattedPath + "\""
      }
    }.toList

    val now = Calendar.getInstance().getTime.getTime.toString
    val fileName = now + ".txt"
    Common.writeToOutputFile(writableLines.distinct, fileName)

    fileAndVariables.foreach { case (file, variables) =>
      val lines = Common.getInputFile(file).getLines().toList
      val finalLines = lines.map { line =>
        var result = line
        variables.foreach { variable =>
          val index = line.indexOf(variable.path)
          if (index >= 0) {
            val nextChar = line(index + variable.path.length)
            if (nextChar == ' '|| nextChar == '"' || nextChar == '\'' || nextChar == '|' || nextChar == '\\') {
              result = line.replaceAllLiterally(variable.path, "\\${" + variable.variableName + "}")
            }
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
