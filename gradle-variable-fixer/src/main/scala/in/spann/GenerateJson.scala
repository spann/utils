package in.spann

import scala.collection.mutable
import scala.util.parsing.json.{JSONArray, JSONFormat, JSONObject}

object GenerateJson {

  val inputfile = "/Users/tkmacgf/sunil/misc/temp/file.tsv"

  def main(args: Array[String]) {
    val lines: List[String] = io.Source.fromFile(inputfile).getLines().toList

    val linesAndColumns: List[Line] = lines.map { line =>
      val fields = line.split('\t')

      if (fields.size <= 16) {
        println(line)
        sys.exit
      }
      val l = Line(fields(0), fields(1), fields(2), fields(3), fields(4), fields(5), fields(6), fields(7), fields(8), fields(9), fields(10), fields(11), fields(12), fields(13), fields(14), fields(15), fields(16), fields(17), fields(18), fields(19))
      l
    }

    val grouped = linesAndColumns.groupBy { line =>
      Some(line.Flow)
    }

    val requiredLines = grouped.map { in =>
      val (flow, row) = in

      val concatedLines = row.foldLeft(List[String]()) { (a, b) =>

        if (a.isEmpty) {
          List(b.CoordinatorFlow_id, b.Project,  List(b.CoordinatorFlow_id, b.NewPath).mkString("|"),  List(b.CoordinatorFlow_id, b.Path).mkString("|"), b.FullLoad, b.RunAzkabanFlow, List(b.Job, b.SkipJobs).mkString(","), b.TargetProject, b.Flow, b.MarkSuccessfull)
        } else {
          List(List(a.head, b.CoordinatorFlow_id).mkString(","), b.Project, List(a(2), List(b.CoordinatorFlow_id, b.NewPath).mkString("|")).mkString(","), List(a(3), List(b.CoordinatorFlow_id, b.Path).mkString("|")).mkString(","), b.FullLoad, b.RunAzkabanFlow, List(a(6), b.Job, b.SkipJobs).mkString(","), b.TargetProject, b.Flow, b.MarkSuccessfull)
        }
      }

      (flow, concatedLines)
    }.map(_._2)

//    requiredLines.foreach(_.foreach(println))

    val finalOutput = requiredLines.map { fields =>

      val flowIds = fields.head
      val srcProject = fields(1)
      val newPathsMap = mutable.HashMap[String, String]()
      val oldPathsMap = mutable.HashMap[String, String]()

      fields(2).split(",").foreach { map =>
        val values = map.split('|')

        val (flow, path) = (values(0), values(1))
        newPathsMap.put(flow, path)

      }
      val newPaths = new JSONObject(newPathsMap.toMap)

      fields(3).split(",").foreach { map =>
        val values = map.split('|')

        val (flow, path) = (values(0), values(1))
        oldPathsMap.put(flow, path)
      }
      val oldPaths = new JSONObject(oldPathsMap.toMap)

      val fullLoad = fields(4).toBoolean
      val runAzkabanFlow = fields(5).toBoolean
      val skipJobs = fields(6)
      val targetProject = fields(7)
      val flow = fields(8)
      val markSuccessful = fields(9).toBoolean

      println(flow)


      new JSONObject(Map("flowIds" -> new JSONArray(List(flowIds)),
      "srcProject" -> srcProject,
      "newPaths" -> newPaths,
      "paths" -> oldPaths,
      "fullLoad" -> fullLoad,
      "runAzkabanJob" -> runAzkabanFlow,
      "skipJobs" -> new JSONArray(List(skipJobs)),
      "targetProject" -> targetProject,
      "flow" -> flow,
      "mark_successful" -> markSuccessful
      ))
    }


    finalOutput.map(format(_)).map(_.replace("\\","")).foreach(println)

    println(finalOutput.size)

  }

  def format(t: Any, i: Int = 0): String = t match {
    case o: JSONObject =>
      o.obj.map{ case (k, v) =>
        "  "*(i+1) + JSONFormat.defaultFormatter(k) + ": " + format(v, i+1)
      }.mkString("{\n", ",\n", "\n" + "  "*i + "}")

    case a: JSONArray =>
      a.list.map{
        e => "  "*(i+1) + format(e, i+1)
      }.mkString("[\n", ",\n", "\n" + "  "*i + "]")

    case _ => JSONFormat defaultFormatter t
  }
}

case class Line(Project: String,TargetProject: String,bucket: String,Flow: String,Job: String,Path: String,NewPath: String,Table: String,CoordinatorFlow_id: String,SkipJobs: String,Type: String,FirstScheduledToRun: String,NextExecutionTime: String,RepeatsEvery: String,TriggeredBy: String,isActive: String,isL1: String, MarkSuccessfull: String, FullLoad: String, RunAzkabanFlow: String)

case class PathMapping(flow: String, path: String)

case class JsonLine(flowIds: List[String], srcProject: String, newPaths: List[PathMapping], oldPaths: List[PathMapping], fullLoad: Boolean, runAzkabanFlow: Boolean, skipJobs: List[String], targetProject: String, flow: String, markSuccessful: Boolean)
