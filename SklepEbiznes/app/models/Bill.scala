package models

import play.api.libs.json.Json

case class Bill(id_bill: Long, name: String, sumOf: Double, userId: Long, created_at: String, is_open: Int, payment_method: Int)


object Bill {
  implicit val billFormat = Json.format[Bill]
}
