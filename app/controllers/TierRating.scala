package controllers

import anorm.Row

import play.api.libs.json.JsNumber
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue

object TierRating extends REST {

  val tableName: String = "tier_ratings"

  val parameters: Map[String, (String, String) => ValidationResult] = Map(
    "rating"        -> validateInt,
    "tier_month_id" -> validateInt
  )

  protected def single(row: Row): JsValue = {
    row match {
      case Row(id: Int, rating: Int, tier_month_id: Int) => {
        JsObject(
          "id"            -> JsNumber(id) ::
          "rating"        -> JsNumber(rating) ::
          "tier_month_id" -> JsNumber(tier_month_id) ::
          Nil
        )
      }
      case _ => throw new IllegalArgumentException("Row provided is invalid!") 
    }
  }

}