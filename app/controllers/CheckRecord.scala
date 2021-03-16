package controllers

import anorm.Row
import play.api.libs.json.{JsNumber, JsObject, JsValue}

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
      case Row(id: Long, matchup_occurences: Long, kos_or_switches_caused: Float, kos_or_switches_stddev: Float, pokemon_id: Long, stat_record_id: Long) => {
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