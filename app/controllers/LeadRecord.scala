package controllers

import anorm.Row
import play.api.libs.json.{JsNumber, JsObject, JsValue}

object LeadRecord extends REST {

  val tableName: String = "lead_records"

  val parameters: Map[String, (String, String) => ValidationResult] = Map(
    "number"         -> validateFloat,
    "stat_record_id" -> validateInt
  )

  protected def single(row: Row): JsValue = {
    row match {
      case Row(id: Long, number: Float, stat_record_id: Long) => {
        JsObject(
          "id"             -> JsNumber(id) ::
          "number"         -> JsNumber(number) ::
          "stat_record_id" -> JsNumber(stat_record_id) ::
          Nil
        )
      }
      case _ => throw new IllegalArgumentException("Row provided is invalid!") 
    }
  }

}