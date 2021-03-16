package controllers

import anorm.Row
import play.api.libs.json.{JsNumber, JsObject, JsValue}

object AbilityRecord extends REST {

  val tableName: String = "ability_records"

  val parameters: Map[String, (String, String) => ValidationResult] = Map(
    "number"         -> validateFloat,
    "ability_id"     -> validateInt,
    "stat_record_id" -> validateInt
  )

  protected def single(row: Row): JsValue = {
    row match {
      case Row(id: Long, number: Float, ability_id: Long, stat_record_id: Long) => {
        JsObject(
          "id"             -> JsNumber(id) ::
          "number"         -> JsNumber(number) ::
          "ability_id"     -> JsNumber(ability_id) ::
          "stat_record_id" -> JsNumber(stat_record_id) ::
          Nil
        )
      }
      case _ => throw new IllegalArgumentException("Row provided is invalid!") 
    }
  }

}