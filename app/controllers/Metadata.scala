package controllers

import anorm.{Row, SQL}
import play.api.Play.current
import play.api.db.DB
import play.api.mvc.Controller
import utils.Resource
import utils.controllers.{CORSAction, TierMonthInfo}

object Metadata extends Controller {

	def get() = CORSAction { request =>
		val tierMonths: List[TierMonthInfo] = DB.withConnection { implicit x => 
			SQL("""SELECT tiers.name,generations.number,months.number,years.number,tier_ratings.rating
             		FROM tier_ratings
             		INNER JOIN tier_months ON tier_ratings.tier_month_id = tier_months.id 
             		INNER JOIN months ON tier_months.month_id = months.id
             		INNER JOIN years ON months.year_id = years.id
             		INNER JOIN tiers ON tier_months.tier_id = tiers.id
             		INNER JOIN generations ON tiers.generation_id = generations.id"""
            )().toList.map {
				case Row(tier: String, generation: Long, month: Long, year: Long, rating: Long) => {
					new TierMonthInfo(generation, tier, rating, year, month)
				}
            }
        }

        Ok(Resource.tierMonthsInfosToJson(tierMonths, None))
	}

}