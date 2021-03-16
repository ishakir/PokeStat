package utils.controllers

import play.api.mvc.{Action, AnyContent, Request, Result}

object CORSAction {
  def apply(block: Request[AnyContent] => Result): Action[AnyContent] = (
    Action { request =>
      block(request).withHeaders("Access-Control-Allow-Origin" -> "*")
    }
  )
}

class TierMonthInfo(
  val generation: Long,
  val tier: String, 
  val rating: Long, 
  val year: Long, 
  val month: Long
)

class StatRecordInfo(
  generation: Long,
  tier: String, 
  rating: Long, 
  year: Long, 
  month: Long, 
  val statRecordId: Long
) extends TierMonthInfo(generation, tier, rating, year, month)