package controllers

import anorm.Row
import anorm.SQL

import play.api.db.DB
import play.api.libs.json.Json
import play.api.libs.json.JsArray
import play.api.libs.json.JsNumber
import play.api.libs.json.JsNull
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.Play.current

import utils.Resource

class StatRecordInfo(
  val generation: Byte,
  val tier: String, 
  val rating: Int, 
  val year: Short, 
  val month: Short, 
  val statRecordId: Int
)

object Pokemon extends Controller {

  def get(pokemon: String) = Action { request =>
    DB.withConnection { implicit c =>
      SQL("SELECT id from pokemon where name='"+pokemon+"';")().toList match {
        case Nil => NotFound(Resource.errorStructure(List("No pokemon named '"+pokemon+"' found!")))
        case row :: Nil => getPokemonInfoFromRow(row)
        case _ => InternalServerError(Resource.errorStructure(List("Multiple pokemon found with name '"+pokemon+"'! My bad.")))
      }
    }
  }

  def query(generation: Int) = Action { request =>
    Ok(
      Json.toJson(
        DB.withConnection { implicit c =>
          SQL("""SELECT pokemon.name
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
      case Row(id: Int) => Ok(getPokemonInfoFromId(id))
    }
  }

  private def getPokemonInfoFromId(pokemon_id: Int) = {
    val statRecords: List[StatRecordInfo] = DB.withConnection { implicit c => 
      SQL("""SELECT tiers.name,generations.number,months.number,years.number,tier_ratings.rating,stat_records.id
             FROM stat_records 
             INNER JOIN tier_ratings ON stat_records.tier_rating_id = tier_ratings.id
             INNER JOIN tier_months ON tier_ratings.tier_month_id = tier_months.id 
             INNER JOIN months ON tier_months.month_id = months.id
             INNER JOIN years ON months.year_id = years.id
             INNER JOIN tiers ON tier_months.tier_id = tiers.id
             INNER JOIN generations ON tiers.generation_id = generations.id
             WHERE stat_records.pokemon_id="""+pokemon_id
      )().toList.map {
        case Row(tier: String, generation: Byte, month: Short, year: Short, rating: Int, stat_record_id: Int) => {
          new StatRecordInfo(generation, tier, rating, year, month, stat_record_id)
        }
      }
    }

    Json.toJson(
      statRecords.groupBy(statRecord => statRecord.generation.toString).mapValues ( genStatRecords => 
        Json.toJson(
          genStatRecords.groupBy(genStatRecord => genStatRecord.tier).mapValues( tierStatRecords => 
            Json.toJson(
              tierStatRecords.groupBy(tierStatRecord => tierStatRecord.rating.toString).mapValues( ratingStatRecords => 
                Json.toJson(
                  ratingStatRecords.map(monthStatRecord => monthStatRecord.month + "/" + monthStatRecord.year -> getInfoForStatRecord(monthStatRecord)).toMap
                )
              )
            )
          )
        )
      )
    )

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

  private def getAbilitiesFromStatRecord(statRecordId: Int) = {
    Json.toJson(
      DB.withConnection { implicit c => 
        SQL("""SELECT abilities.name,ability_records.number
               FROM ability_records
               INNER JOIN abilities ON ability_records.ability_id = abilities.id
               WHERE ability_records.stat_record_id="""+statRecordId
        )().toList.map {
          case Row(ability: String, value: Double) => {
            ability -> JsNumber(value)
          }
        }.toMap
      }
    )
  }

  private def getMovesFromStatRecord(statRecordId: Int) = {
    Json.toJson(
      DB.withConnection { implicit c =>
        SQL("""SELECT moves.name,move_records.number
               FROM move_records
               INNER JOIN moves ON move_records.move_id = moves.id
               WHERE move_records.stat_record_id="""+statRecordId
        )().toList.map {
          case Row(move: String, value: Double) => {
            move -> JsNumber(value)
          }
        }.toMap
      }
    )
  }

  private def getItemsFromStatRecord(statRecordId: Int) = {
    Json.toJson(
      DB.withConnection { implicit c =>
        SQL("""SELECT items.name,item_records.number
               FROM item_records
               INNER JOIN items ON item_records.item_id = items.id
               WHERE item_records.stat_record_id="""+statRecordId
        )().toList.map {
          case Row(item: String, value: Double) => {
            item -> JsNumber(value)
          }
        }.toMap
      }
    )
  }

  private def getTeammatesFromStatRecord(statRecordId: Int) = {
    Json.toJson(
      DB.withConnection { implicit c =>
        SQL("""SELECT pokemon.name,teammate_records.number
               FROM teammate_records
               INNER JOIN pokemon ON teammate_records.pokemon_id = pokemon.id
               WHERE teammate_records.stat_record_id="""+statRecordId
        )().toList.map {
          case Row(pokemon: String, value: Double) => {
            pokemon -> JsNumber(value)
          }
        }.toMap
      }
    )
  }

  private def getSpreadsFromStatRecord(statRecordId: Int) = {
    Json.toJson(
      DB.withConnection { implicit c =>
        SQL("""SELECT natures.name,ev_spreads.hp,ev_spreads.attack,ev_spreads.defence,ev_spreads.spa,ev_spreads.spd,ev_spreads.speed,spread_records.number
               FROM spread_records
               INNER JOIN natures on spread_records.nature_id = natures.id
               INNER JOIN ev_spreads on spread_records.ev_spread_id = ev_spreads.id
               WHERE spread_records.stat_record_id="""+statRecordId
        )().toList.map {
          case Row(nature: String, hp: Short, att: Short, defence: Short, spa: Short, spd: Short, spe: Short, value: Double) => {
            nature + ":" + hp + "/" + att + "/" + defence + "/" + spa + "/" + spd + "/" + spe + "/" -> JsNumber(value)
          }
        }.toMap
      }
    )
  }

  private def generateUsage(statRecordId: Int) = {
    DB.withConnection { implicit c =>
      SQL("""SELECT tier_ratings.no_of_battles,stat_records.raw_usage
             FROM stat_records
             INNER JOIN tier_ratings on stat_records.tier_rating_id = tier_ratings.id
             WHERE stat_records.id="""+statRecordId
      )().toList match {
        case row :: Nil => {
          row match {
            case Row(noBattles: Int, rawCount: Int) => {
              JsNumber(100 * (rawCount.toFloat / (12 * noBattles.toFloat)))
            }
          }
        }
        case _ => Resource.errorStructure(List("Incorrect number of Rows"))
      }
    }
  }
}