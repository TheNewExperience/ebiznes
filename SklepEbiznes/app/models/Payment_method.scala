package models

import play.api.libs.json.Json

case class Payment_method(id_payment_method: Int, name:String)

object Payment_method {
  implicit val payment_method = Json.format[Payment_method]
}