package controllers

import anorm.Row

import play.api.libs.json.JsNumber
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue

object ItemRecord extends REST {

  val tableName: String = "item_records"

  val parameters: Map[String, (String, String) => ValidationResult] = Map(
    "number"         -> validateInt,
    "item_id"        -> validateInt,
    "stat_record_id" -> validateInt
  )

  protected def single(row: Row): JsValue = {
    row match {
      case Row(id: Int, number: Int, item_id: Int, stat_record_id: Int) => {
        JsObject(
          "id"             -> JsNumber(id) ::
          "number"         -> JsNumber(number) ::
          "item_id"        -> JsNumber(item_id) ::
          "stat_record_id" -> JsNumber(stat_record_id) ::
          Nil
        )
      }
      case _ => throw new IllegalArgumentException("Row provided is invalid!") 
    }
  }

}