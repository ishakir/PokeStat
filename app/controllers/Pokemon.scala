package controllers

import anorm.{Row, SQL}
import play.api.Play.current
import play.api.db.DB
import play.api.libs.json.{JsNumber, JsObject, Json}
import play.api.mvc.Controller
import utils.Resource
import utils.controllers.{CORSAction, StatRecordInfo}

object Pokemon extends Controller {

  def get(pokemon: String) = CORSAction { request =>
    DB.withConnection { implicit c =>
      SQL("SELECT id from pokemon where name='"+pokemon+"';")().toList match {
        case Nil => NotFound(Resource.errorStructure(List("No pokemon named '"+pokemon+"' found!")))
        case row :: Nil => getPokemonInfoFromRow(row)
        case _ => InternalServerError(Resource.errorStructure(List("Multiple pokemon found with name '"+pokemon+"'! My bad.")))
      }
    }
  }

  def query(generation: Int) = CORSAction { request =>
    Ok(
      Json.toJson(
        DB.withConnection { implicit c =>
          SQL("""SELECT DISTINCT pokemon.name
                 FROM stat_records
                 INNER JOIN tier_ratings ON stat_records.tier_rating_id = tier_ratings.id
                 INNER JOIN tier_months ON tier_ratings.tier_month_id = tier_months.id
                 INNER JOIN tiers ON tier_months.tier_id = tiers.id
                 INNER JOIN generations ON tiers.generation_id = generations.id
                 INNER JOIN pokemon ON stat_records.pokemon_id = pokemon.id
                 WHERE generations.number = """+generation
          )().toList.map {
            case Row(pokemon: String) => pokemon
          }
        }
      )
    )
  }

  private def getPokemonInfoFromRow(row: Row) = {
    row match {
      case Row(id: Long) => Ok(getPokemonInfoFromId(id))
    }
  }

  private def getPokemonInfoFromId(pokemon_id: Long) = {
    val statRecords: List[StatRecordInfo] = DB.withConnection { implicit c => 
      SQL(s"""SELECT tiers.name,generations.number,months.number,years.number,tier_ratings.rating,stat_records.id
             FROM stat_records 
             INNER JOIN tier_ratings ON stat_records.tier_rating_id = tier_ratings.id
             INNER JOIN tier_months ON tier_ratings.tier_month_id = tier_months.id 
             INNER JOIN months ON tier_months.month_id = months.id
             INNER JOIN years ON months.year_id = years.id
             INNER JOIN tiers ON tier_months.tier_id = tiers.id
             INNER JOIN generations ON tiers.generation_id = generations.id
             WHERE stat_records.pokemon_id=$pokemon_id
             ORDER BY years.number ASC, months.number ASC"""
      )().toList.map {
        case Row(tier: String, generation: Long, month: Long, year: Long, rating: Long, stat_record_id: Long) => {
          new StatRecordInfo(generation, tier, rating, year, month, stat_record_id)
        }
      }
    }

    Resource.tierMonthsInfosToJson(statRecords, Some(getInfoForStatRecord _))

  }

  private def getInfoForStatRecord(statRecord: StatRecordInfo) = {
    JsObject(
      "usage"     -> generateUsage(statRecord.statRecordId)              ::
      "abilities" -> getAbilitiesFromStatRecord(statRecord.statRecordId) ::
      "moves"     -> getMovesFromStatRecord(statRecord.statRecordId)     ::
      "items"     -> getItemsFromStatRecord(statRecord.statRecordId)     ::
      "teammates" -> getTeammatesFromStatRecord(statRecord.statRecordId) ::
      "spreads"   -> getSpreadsFromStatRecord(statRecord.statRecordId)   ::
      Nil
    )
  }

  private def getAbilitiesFromStatRecord(statRecordId: Long) = {
    Json.toJson(
      DB.withConnection { implicit c => 
        SQL("""SELECT abilities.name,ability_records.number
               FROM ability_records
               INNER JOIN abilities ON ability_records.ability_id = abilities.id
               WHERE ability_records.stat_record_id="""+statRecordId
        )().toList.map {
          case Row(ability: String, value: Float) => {
            ability -> JsNumber(value)
          }
        }.toMap
      }
    )
  }

  private def getMovesFromStatRecord(statRecordId: Long) = {
    Json.toJson(
      DB.withConnection { implicit c =>
        SQL("""SELECT moves.name,move_records.number
               FROM move_records
               INNER JOIN moves ON move_records.move_id = moves.id
               WHERE move_records.stat_record_id="""+statRecordId
        )().toList.map {
          case Row(move: String, value: Float) => {
            move -> JsNumber(value)
          }
        }.toMap
      }
    )
  }

  private def getItemsFromStatRecord(statRecordId: Long) = {
    Json.toJson(
      DB.withConnection { implicit c =>
        SQL("""SELECT items.name,item_records.number
               FROM item_records
               INNER JOIN items ON item_records.item_id = items.id
               WHERE item_records.stat_record_id="""+statRecordId
        )().toList.map {
          case Row(item: String, value: Float) => {
            item -> JsNumber(value)
          }
        }.toMap
      }
    )
  }

  private def getTeammatesFromStatRecord(statRecordId: Long) = {
    Json.toJson(
      DB.withConnection { implicit c =>
        SQL("""SELECT pokemon.name,teammate_records.number
               FROM teammate_records
               INNER JOIN pokemon ON teammate_records.pokemon_id = pokemon.id
               WHERE teammate_records.stat_record_id="""+statRecordId
        )().toList.map {
          case Row(pokemon: String, value: Float) => {
            pokemon -> JsNumber(value)
          }
        }.toMap
      }
    )
  }

  private def getSpreadsFromStatRecord(statRecordId: Long) = {
    Json.toJson(
      DB.withConnection { implicit c =>
        SQL("""SELECT natures.name,ev_spreads.hp,ev_spreads.attack,ev_spreads.defence,ev_spreads.spa,ev_spreads.spd,ev_spreads.speed,spread_records.number
               FROM spread_records
               INNER JOIN natures on spread_records.nature_id = natures.id
               INNER JOIN ev_spreads on spread_records.ev_spread_id = ev_spreads.id
               WHERE spread_records.stat_record_id="""+statRecordId
        )().toList.map {
          case Row(nature: String, hp: Long, att: Long, defence: Long, spa: Long, spd: Long, spe: Long, value: Float) => {
            nature + ":" + hp + "/" + att + "/" + defence + "/" + spa + "/" + spd + "/" + spe + "/" -> JsNumber(value)
          }
        }.toMap
      }
    )
  }

  private def generateUsage(statRecordId: Long) = {
    DB.withConnection { implicit c =>
      SQL("""SELECT tier_ratings.no_of_battles,stat_records.raw_usage
             FROM stat_records
             INNER JOIN tier_ratings on stat_records.tier_rating_id = tier_ratings.id
             WHERE stat_records.id="""+statRecordId
      )().toList match {
        case row :: Nil => {
          row match {
            case Row(noBattles: Long, rawCount: Long) => {
              JsNumber(100 * (rawCount.toFloat / (12 * noBattles.toFloat)))
            }
          }
        }
        case _ => Resource.errorStructure(List("Incorrect number of Rows"))
      }
    }
  }
}