package controllers

import anorm.Row
import play.api.libs.json.{JsNumber, JsObject, JsValue}

object TierMonth extends REST {

  val tableName: String = "tier_months"

  val parameters: Map[String, (String, String) => ValidationResult] = Map(
    "month_id" -> validateInt,
    "tier_id"  -> validateInt
  )

  protected def single(row: Row): JsValue = {
    row match {
      case Row(id: Long, month_id: Long, tier_id: Long) => {
        JsObject(
          "id"       -> JsNumber(id) ::
          "month_id" -> JsNumber(month_id) ::
          "tier_id"  -> JsNumber(tier_id) ::
          Nil
        )
      }
      case _ => throw new IllegalArgumentException("Row provided is invalid!") 
    }
  }

}