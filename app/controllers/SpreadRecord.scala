package controllers

import anorm.Row

import play.api.libs.json.JsNumber
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue

object SpreadRecord extends REST {

  val tableName: String = "spread_records"

  val parameters: Map[String, (String, String) => ValidationResult] = Map(
    "number"         -> validateFloat,
    "ev_spread_id"   -> validateInt,
    "nature_id"      -> validateInt,
    "stat_record_id" -> validateInt
  )

  protected def single(row: Row): JsValue = {
    row match {
      case Row(id: Long, number: Double, ev_spread_id: Int, nature_id: Int, stat_record_id: Int) => {
        JsObject(
          "id"             -> JsNumber(id) ::
          "number"         -> JsNumber(number) ::
          "ev_spread_id"   -> JsNumber(ev_spread_id) ::
          "nature_id"      -> JsNumber(nature_id) ::
          "stat_record_id" -> JsNumber(stat_record_id) ::
          Nil
        )
      }
      case _ => throw new IllegalArgumentException("Row provided is invalid!") 
    }
  }

}