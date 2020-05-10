package models

import play.api.libs.json.Json

case class Box(id: Long, sumOf: Double, user_id:Long, payment_id: Int)

object Box {
  implicit val boxFormat = Json.format[Box]
}



