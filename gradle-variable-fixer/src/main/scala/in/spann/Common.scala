package in.spann

import java.io.File

object Common {

  val location = "/Users/tkmacgf/sunil/projects/bigdata-recommendation-engine"
  val filePattern = ".*.gradle$"

  def getAllFiles: Seq[String] = {

    def go(toCheck: List[File], results: List[File]): Seq[File] = toCheck match {
      case head :: tail =>
        val these = head.listFiles
        val directories = these.filter(_.isDirectory)
        val files = these.filter(_.isFile)
        val updated = results ++ files.filter(_.getAbsolutePath.matches(filePattern))
        go(tail ++ directories, updated)
      case _ => results
    }

    go(new File(location) :: Nil, Nil).map(_.getPath)
  }

  def getInputFile(filePath: String) = {
    scala.io.Source.fromFile(filePath)
  }

  def writeToOutputFile(lines: List[String], filePath: String) = {
    def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
      val p = new java.io.PrintWriter(f)
      try {
        op(p)
      } finally {
        p.close()
      }
    }

    printToFile(new File(filePath)) { p =>
      lines.foreach(p.println)
    }
  }
}
