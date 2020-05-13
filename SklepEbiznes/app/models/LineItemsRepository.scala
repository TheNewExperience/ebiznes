package models

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class LineItemsRepository @Inject()(dbConfigProvider: DatabaseConfigProvider,BillRepo:BillRepository, ProductRepo:ProductRepository)(implicit ec: ExecutionContext) {
  protected [models]val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  protected [models] class LineItemsTable(tag: Tag) extends Table[LineItems](tag,"line_items") {
    def id = column[Long]("id",O.PrimaryKey,O.AutoInc)
    def is_billed = column[Int]("is_billed")
    def unit_price = column[Double]("unit_price")
    def quantity = column[Int]("quantity")
    def item_name = column[String]("item_name")
    def price = column[Double]("price")
    def product_id = column[Long]("product_id")
    private def product_fk = foreignKey("product_id_fk",product_id,productTab)(_.id_product)
    def bill_id = column[Long]("bill_id")
    private def bill_fk = foreignKey("payment_id_fk",bill_id, billTab)(_.id_bill)
    def * = (id,is_billed,unit_price,quantity,item_name,price,product_id,bill_id)<>((LineItems.apply _).tupled,LineItems.unapply)
  }
  import ProductRepo.ProductTable
  import BillRepo.BillTable

  protected [models] val lineItems = TableQuery[LineItemsTable]
  private val productTab = TableQuery[ProductTable]
  private val billTab = TableQuery[BillTable]

  def create(is_billed:Int, unit_price: Double, quantity: Int,item_name:String,price: Double,product_id: Long,bill_id:Long): Future[LineItems] = db.run {
    (lineItems.map(l =>(l.is_billed,l.unit_price,l.quantity,l.item_name,l.price,l.product_id,l.bill_id))
      returning( lineItems.map(_.id))
      into {case((is_billed,unit_price,quantity,item_name,price,product_id,bill_id),id) => LineItems(id,is_billed,unit_price,quantity,item_name,price,product_id,bill_id)}
      )+=(is_billed,unit_price,quantity,item_name,price,product_id,bill_id)
  }


  def list(): Future[Seq[LineItems]] = db.run {
    lineItems.result
  }

  def getById(id: Long): Future[LineItems] = db.run {
    lineItems.filter(_.id === id).result.head
  }

  def getByIdOption(id: Long): Future[Option[LineItems]] = db.run {
    lineItems.filter(_.id === id).result.headOption
  }

  def update(id: Long, new_line: LineItems): Future[Unit] = {
    val billToUpdate: LineItems = new_line.copy(id)
    db.run {
      (lineItems.filter(_.id === id).update(billToUpdate)).map(_ => ())
    }
  }

  def delete(id: Long): Future[Unit] = db.run {
    (lineItems.filter(_.id === id).delete).map(_ => ())
  }

}


