package controllers

import anorm.Row

import play.api.libs.json.JsNumber
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue

object StatRecord extends REST {

  val tableName: String = "stat_records"

  val parameters: Map[String, (String, String) => ValidationResult] = Map(
    "raw_usage"      -> validateInt,
    "pokemon_id"     -> validateInt,
    "tier_rating_id" -> validateInt
  )

  protected def single(row: Row): JsValue = {
    row match {
      case Row(id: Long, rawUsage: Int, pokemon_id: Int, tier_rating_id: Int) => {
        JsObject(
          "id"             -> JsNumber(id) ::
          "raw_usage"      -> JsNumber(rawUsage) ::
          "pokemon_id"     -> JsNumber(pokemon_id) ::
          "tier_rating_id" -> JsNumber(tier_rating_id) ::
          Nil
        )
      }
      case _ => throw new IllegalArgumentException("Row provided is invalid!") 
    }
  }

}