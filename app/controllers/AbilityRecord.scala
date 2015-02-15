package controllers

import anorm.Row

import play.api.libs.json.JsNumber
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue

object AbilityRecord extends REST {

  val tableName: String = "ability_records"

  val parameters: Map[String, (String, String) => ValidationResult] = Map(
    "number"         -> validateFloat,
    "ability_id"     -> validateInt,
    "stat_record_id" -> validateInt
  )

  protected def single(row: Row): JsValue = {
    row match {
      case Row(id: Long, number: Double, ability_id: Int, stat_record_id: Int) => {
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