package controllers

import anorm.Row
import anorm.SQL

import play.api.db.DB
import play.api.libs.json.Json
import play.api.libs.json.JsNull
import play.api.libs.json.JsNumber
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.JsValue
import play.api.libs.json.JsUndefined
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.Play.current

import utils.Resource

trait ValidationResult

class ValidationSuccess(val name: String, val value: String) extends ValidationResult

trait ValidationFailure extends ValidationResult {
  val failureMessage: String
}

class NotProvided(val name: String) extends ValidationFailure {
  val failureMessage: String = "Missing json object parameter '"+name+"'"
}

class InvalidType(val name: String, val value: JsValue) extends ValidationFailure {
  val failureMessage: String = "Value '"+value+"' supplied for "+name+" is invalid"
}

class ByteTooSmall(val name: String, val actual: String) extends ValidationFailure {
  val failureMessage: String = name + " number '" + actual + "' is too low, try 0 <= " + name + " < 256"
}

class ByteTooLarge(val name: String, val actual: String) extends ValidationFailure {
  val failureMessage: String = name + " number '" + actual + "' is too high, try 0 <= " + name + " < 256"
}

class ShortTooSmall(val name: String, val actual: String) extends ValidationFailure {
  val failureMessage: String = name + " number '" + actual + "' is too low, try 0 <= " + name + " < 32768"
}

class ShortTooLarge(val name: String, val actual: String) extends ValidationFailure {
  val failureMessage: String = name + " number '" + actual + "' is too high, try 0 <= " + name + " < 32768"
}

class IntTooSmall(val name: String, val actual: String) extends ValidationFailure {
  val failureMessage: String = name + " number '" + actual + "' is too low, try 0 <= " + name + " < " + Int.MaxValue
}

class ExpectedOneValue(val name: String, val actual: Seq[String]) extends ValidationFailure {
  val failureMessage: String = "Found sequence of values " + actual.mkString(",") + " rather than a single value for '" + name + "'"
}

trait REST extends Controller {
  protected val parameters: Map[String, (String, String) => ValidationResult]
  protected val tableName: String
  protected def single(row: Row): JsValue

  // Controller Routes
  def create = Action { request =>
    request.body.asJson.map { json =>
      val (successes: List[ValidationSuccess], failures: List[ValidationFailure]) = allValidationResultsFromBody(json)

      if(failures.isEmpty) {
        val insertQuery = formInsertQuery(successes)
        try {
          DB.withConnection { implicit c =>
            SQL(insertQuery).executeInsert() match {
              case Some(id) => {
                val selectQuery = getByIdQuery(id)
                formatUnique(SQL(selectQuery)()) match {
                  case Some(json) => Ok(json)
                  case None       => InternalServerError(Resource.errorStructure(List("Create failed for an unknown reason")))
                }
              }
              case None     => InternalServerError(Resource.errorStructure(List("Create failed for an unknown reason")))
            }
          }
        } catch {
          case e: Exception => BadRequest(Resource.errorStructure(List(e.getMessage)))
        }
      } else {
        BadRequest(Resource.errorStructure(failures.map(fail => fail.failureMessage)))
      }
    }.getOrElse {
      BadRequest(Resource.errorStructure(List("Expecting Json data, try setting the 'Content-Type' header to 'application/json'")))
    }
  }

  def update(id: Int) = Action { request =>
    request.body.asJson.map { json =>
      val (successes: List[ValidationSuccess], failures: List[ValidationFailure]) = allValidationResultsFromBody(json)

      if(!successes.isEmpty) {
        val updateQuery = formUpdateQuery(id, successes)
        try {
          DB.withConnection { implicit c =>
            val result: Int = SQL(updateQuery).executeUpdate()
            result match {
              case 0 => NotFound(Resource.errorStructure(List("Not found with id "+id)))
              case res => formatUnique(SQL(getByIdQuery(id))()) match {
                case Some(json) => Ok(json)
                case None => InternalServerError(Resource.errorStructure(List("Unable to retrieve updated row")))
              }
            }
          }
        } catch {
          case e: Exception => BadRequest(Resource.errorStructure(List(e.getMessage)))
        }
      } else {
        BadRequest(Resource.errorStructure("Unable to find one parameter specified correctly - nothing to update" :: failures.map(fail => fail.failureMessage)))
      }
    }.getOrElse {
      BadRequest(Resource.errorStructure(List("Expecting Json data, try setting the 'Content-Type' header to 'application/json")))
    }
  }

  def getWithParams = Action { request =>
    val queryParams = request.queryString
    val (successes: List[ValidationSuccess], failures: List[ValidationFailure]) = allVaidationResultsFromQueryParams(queryParams)

    val selectQuery = formSelectQuery(successes)
    DB.withConnection { implicit c =>
      Ok(formatMany(SQL(selectQuery)()))
    }
  }

  def getById(id: Int) = Action {
    DB.withConnection { implicit c =>
      formatUnique(SQL(getByIdQuery(id))()) match {
        case Some(json) => Ok(json)
        case None => NotFound(Resource.errorStructure(List("No generation found")))
      }
    }
  }

  def delete(id: Int) = Action {
    DB.withConnection { implicit c =>
      val result: Int = SQL(deleteByIdQuery(id)).executeUpdate()
      result match {
        case 0   => NotFound(Resource.errorStructure(List("No generation found")))
        case res => Ok(Resource.successStructureWithoutData)
      }
    }
  }

  // Validation
  private def separateIntoSuccessesAndFailures(validationResults: Set[ValidationResult]) = {
    validationResults.foldLeft[(List[ValidationSuccess], List[ValidationFailure])]((Nil, Nil))((lists, result) => {
      val (succs: List[ValidationSuccess], fails: List[ValidationFailure]) = lists
      result match {
        case success: ValidationSuccess => (success :: succs, fails)
        case failure: ValidationFailure => (succs, failure :: fails)
      }
    })
  }

  private def allVaidationResultsFromQueryParams(queryParams: Map[String, Seq[String]]) = {
    val validationResults = parameters.keySet.map( parameter => 
      queryParams.get(parameter) match {
        case Some(list) => {
          if(list.size == 1) parameters.get(parameter).get(parameter, list.head)
          else new ExpectedOneValue(parameter, list)
        }
        case None        => new NotProvided(parameter)
      }
    )

    separateIntoSuccessesAndFailures(validationResults)
  }

  private def allValidationResultsFromBody(json: JsValue) = {
    val validationResults = parameters.keySet.map( parameter => 
      (json \ parameter) match {
        case string: JsString => parameters.get(parameter).get(parameter, string.value)
        case undef: JsUndefined => new NotProvided(parameter)
        case value => new InvalidType(parameter, value)
      }
    )

    separateIntoSuccessesAndFailures(validationResults)
  }

  protected def validateByte(name: String, number: String) = number.toInt match {
    case num if num < 1   => new ByteTooSmall(name, num.toString)
    case num if num > 255 => new ByteTooLarge(name, num.toString)
    case num              => new ValidationSuccess(name, num.toString)
  }

  protected def validateShort(name: String, number: String) = number.toInt match {
    case num if num < 1     => new ShortTooSmall(name, num.toString)
    case num if num > 32767 => new ShortTooLarge(name, num.toString)
    case num                => new ValidationSuccess(name, num.toString)
  }

  protected def validateInt(name: String, number: String) = number.toInt match {
    case num if num < 1 => new IntTooSmall(name, num.toString)
    case num            => new ValidationSuccess(name, num.toString)
  }

  protected def allStringsValidator(name: String, value: String) = new ValidationSuccess(name, "'" + value + "'")

  // Resource formatting
  private def formatUnique(rows: Seq[Row]): Option[JsValue] = {
    rows.toList match {
      case Nil         => None
      case List(value) => Some(Resource.successStructureWithData(single(value)))
      case _ => throw new IllegalStateException("Expecting 0 or 1 elements!")
    }
  }

  private def formatMany(rows: Seq[Row]): JsValue = {
    val result: Seq[JsValue] = rows.map { row =>
      single(row)
    }
    Resource.successStructureWithData(Json.toJson(result))
  }

  // Query Stuff
  private def formInsertQuery(params: Seq[ValidationSuccess]) = {
    val columns = params.map(param => param.name)
    val values  = params.map(param => param.value)
    "INSERT INTO " + tableName + " (" + columns.mkString(",") + ") VALUES (" + values.mkString(",") + ");"
  }

  private def formSelectQuery(params: Seq[ValidationSuccess]) = {
    if(params.isEmpty) "SELECT * FROM " + tableName + ";"
    else {
      val newValues = params.map(param => param.name + "=" + param.value)
      "SELECT * FROM " + tableName + " WHERE " + newValues.mkString(" AND ") + ";"
    }
  }

  private def getByIdQuery(id: Long) = {
    "SELECT * FROM " + tableName + " WHERE id=" + id + ";"
  }

  private def formUpdateQuery(id: Int, params: Seq[ValidationSuccess]) = {
    val newValues = params.map(param => param.name + "=" + param.value)
    "UPDATE " + tableName + " SET " + newValues.mkString(",") + " WHERE id=" + id + ";"
  }

  private def deleteByIdQuery(id: Long) = {
    "DELETE FROM generations WHERE id=" + id + ";"
  }

}