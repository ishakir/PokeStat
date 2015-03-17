package controllers

import anorm.Row

import play.api.libs.json.JsNumber
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue

object CheckRecord extends REST {

  val tableName: String = "check_records"

  val parameters: Map[String, (String, String) => ValidationResult] = Map(
    "matchup_occurences"     -> validateInt,
    "kos_or_switches_caused" -> validateFloat,
    "kos_or_switches_stddev" -> validateFloat,
    "pokemon_id"             -> validateInt,
    "stat_record_id"         -> validateInt
  )

  protected def single(row: Row): JsValue = {
    row match {
      case Row(id: Long, matchup_occurences: Int, kos_or_switches_caused: Double, kos_or_switches_stddev: Double, pokemon_id: Int, stat_record_id: Int) => {
        JsObject(
          "id"                     -> JsNumber(id) ::
          "matchup_occurences"     -> JsNumber(matchup_occurences) ::
          "kos_or_switches_caused" -> JsNumber(kos_or_switches_caused) ::
          "kos_or_switches_stddev" -> JsNumber(kos_or_switches_stddev) ::
          "pokemon_id"             -> JsNumber(pokemon_id) ::
          "stat_record_id"         -> JsNumber(stat_record_id) ::
          Nil
        )
      }
      case _ => {
        println(row)
        throw new IllegalArgumentException("Row provided is invalid!") 
      }
    }
  }

}