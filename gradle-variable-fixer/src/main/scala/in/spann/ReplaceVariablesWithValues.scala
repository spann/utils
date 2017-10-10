package in.spann

import java.io.File
import java.util.regex.{Matcher, Pattern}

import scala.collection.immutable.{HashSet, HashMap}

object ReplaceVariablesWithValues {

  val customVariables = HashMap(
    "search_personalization.gradle" -> List("by_pri_gender_dir", "by_pri_age_dir", "by_pri_gender_age_dir", "byAtg"),

    "personalization.gradle" -> List("byDeptCat", "byBrand", "byAgeGroup", "bySubType", "byGender", "byStyle", "byPrice", "byAtg"),

    "search_personalization_anony.gradle" -> List("by_pri_gender_dir", "by_pri_age_dir", "by_pri_gender_age_dir", "byAtg"),

    "search_personalization_anony_v2.gradle" -> List("by_pri_gender_dir", "by_pri_age_dir", "by_pri_gender_age_dir", "byAtg", "by_p1_brandName", "by_p1_price", "byDeptCat", "userPropensity"),

    "search_personalization_v2.gradle" -> List("by_pri_gender_dir", "by_pri_age_dir", "by_pri_gender_age_dir", "byAtg", "userPropensity", "by_sports", "by_tenderType", "devicePropensity", "findingMethod", "reactiveCampaign", "recommendationInteraction", "socialChannel", "preferredAddress", "tenderTypePropensity", "visit_time_propensity", "singleTenderTypePropensity", "visit_time_propensity_op", "by_spending_amount", "by_big_ticket_item", "by_aov_zipcode", "by_p1_price", "by_p1_brandName"),

    "jobs.gradle" -> List("productsDataFolder", "sameDeptCatFolder", "diffDeptCatFolder", "testId_default", "search_terms_count", "pv_term_products", "products", "aggregated_out", "formatted_out"),

    "viewview.gradle" -> List("product_details_path"),

    "relatedsearch.gradle" -> List("clk_input_data", "search_terms_count", "clk_related_searches", "search_terms_for_catalog", "output_catalog", "ca_count", "pv_count", "conversion_ratio", "product_p1", "term_p1_count", "clk_catalog_aggregated", "clk_catalog_filtered", "related_search_v2_out", "related_search_v2_hdfs_manual_file", "related_search_v2_manual_out", "related_search_v2_json"),

    "smartsuggest.gradle" -> List("search_p1_weight_output", "search_p2_weight_output", "search_p3_weight_output", "search_gender_weight_output", "search_searchterms", "search_p1_output", "search_gender_p2_output", "search_products", "products", "product_detail", "searchterm_prod_detail")
  )

  val variableListRegex = "def(.*?)=(.*?)(\"|\')(.*?)(\"|\')"

  val variableRegexList = List(
    VariablePattern("def(.*)staged_dir", variableListRegex),
    VariablePattern("def(.*)log_dir", variableListRegex),
    VariablePattern("def(.*)raw_dir", variableListRegex)
  )


  def main(args: Array[String]) {

    val files = Common.getAllFiles()
    files.foreach(println)

    files.foreach { file =>
      println("Processing file: " + file)

      var inputFileLines = Common.getInputFile(file).getLines().toList

      val additionalVariableList = customVariables.getOrElse(file.split("/").takeRight(1)(0), List())


      val variablePatterns = getAllVariableRegex(variableRegexList, additionalVariableList)
      var listOfVariables = getListOfVariables(inputFileLines, variablePatterns)

      do {
        listOfVariables.foreach(println)
        println()
        val finalLines = replaceListOfVariablesWithValues(listOfVariables, inputFileLines)
        Common.writeToOutputFile(finalLines, file)
        inputFileLines = Common.getInputFile(file).getLines().toList
        listOfVariables = getListOfVariables(inputFileLines, variablePatterns)
      } while (listOfVariables.length != 0)

    }
  }

  def getAllVariableRegex(variableRegexList: List[VariablePattern], additionalVariableList: List[String]) = {
    additionalVariableList.map { name =>
      VariablePattern("def " + name, variableListRegex)
    } ++ variableRegexList
  }

  def replaceListOfVariablesWithValues(variables: List[Variable], inputFileLines: List[String]) = {
    inputFileLines.toList.flatMap { line =>
      var result = line

      variables.foreach { variable =>
        val variableRegex = "\\$" + variable.name
        val matcher = Pattern.compile(variableRegex).matcher(line)

        if (matcher.find()) {
          if (variableIsMatchedFully(variable.name, line)) {
            println("Replacing line: " + line + " with " + variableRegex + " value: " + variable.value)
            val newLine = line.replaceAll(variableRegex, Matcher.quoteReplacement(variable.value))
            result = newLine
          }
        }

        if (lineNeedToBeRemoved(line, variable)) {
          result = ""
        }
      }

      Some(result)
    }
  }

  def lineNeedToBeRemoved(line: String, variable: Variable) = {
    val matcher = Pattern.compile(variable.pattern.patternToFind).matcher(line)
    matcher.find()
  }

  def variableIsMatchedFully(name: String, line: String) = {
    val index = line.indexOf("$" + name)
    val nextChar = line.charAt(index + name.length + 1)

    nextChar != '_' && !Character.isLetterOrDigit(nextChar)
  }

  def getListOfVariables(inputFileLines: List[String], variableRegexList: List[VariablePattern]) = {
    inputFileLines.toList.flatMap { line =>
      variableRegexList.map { regex =>
        val matcher = Pattern.compile(regex.patternToFind).matcher(line)

        if (matcher.find()) {
          val (name, path) = getNamePathFromLine(regex.patternToExtract, line)
          Some(Variable(name, path, regex))
        } else {
          None
        }
      }.flatten
    }
  }

  def getNamePathFromLine(pattern: String, line: String) = {
    println("Pattern: " + pattern + " line: " + line)
    val extractor = Pattern.compile(pattern).matcher(line)
    extractor.find()
    val name = extractor.group(1).trim
    val path = extractor.group(4).trim

    if (name.isEmpty || path.isEmpty) {
      println("Name: " + name + " Path: " + path + " is empty")
      sys.exit()
    }

    (name, path)
  }
}

case class VariablePattern(patternToFind: String, patternToExtract: String)

case class Variable(name: String, value: String, pattern: VariablePattern)
