package in.spann

import java.util.regex.Pattern

object FindAllAzkabanCommands {

  var commands: List[String] = List()

  def main(args: Array[String]) {
    val files = Common.getAllFiles
    files.foreach { file =>
      println("Processing file: " + file)

      val inputFileLines = Common.getInputFile(file).getLines().toList
      commands = commands ++ getAllCommandUsed(inputFileLines)
    }

    commands.groupBy(x => x).foreach { in =>
      val (command, list) = in

      println(command + " " + list.size)
    }

  }

  def getAllCommandUsed(lines: List[String]): List[String] = {

    lines.flatMap { line =>
      val extractor = Pattern.compile("command(.*?): (\"|\')(.*?) ").matcher(line)
      if (extractor.find()) {
        val name = extractor.group(3).trim
        Some(name)
      } else {
        None
      }
    }
  }
}
