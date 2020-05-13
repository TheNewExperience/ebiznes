package models

import play.api.libs.json.Json

case class BoxLine(id:Long,  unit_price:Double, quantity: Int, SumOfLine:Double, product_name:String, product_id: Long, box_id:Long)


object BoxLine {
  implicit val boxLineFormat = Json.format[BoxLine]
}


