package models

import play.api.libs.json.Json


case class User(id_user: Long, email: String, password: String, reset_question: String, reset_answer: String, created_at: String,updated_at: String,is_admin:Int)

object User{
  implicit val userFormat = Json.format[User]
}



