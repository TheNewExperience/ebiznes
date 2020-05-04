package models

import play.api.libs.json.Json

case class LineItems(id:Long,is_billed:Int,unit_price:Double,quantity: Int,item_name:String,price: Double,product_id: Long, bill_id:Long)


object LineItems {
  implicit val lineItemsFormat = Json.format[LineItems]
}
