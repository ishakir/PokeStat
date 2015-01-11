package controllers

import play.api._
import play.api.mvc._

object Views extends Controller {

  def usage() = Action {
    Ok(views.html.usage())
  }

  def graph() = Action {
    Ok(views.html.graph())
  }

}