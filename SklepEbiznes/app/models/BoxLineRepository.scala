package models

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class BoxLineRepository @Inject()(dbConfigProvider: DatabaseConfigProvider, BoxRepo:BoxRepository, ProductRepo:ProductRepository)(implicit ec: ExecutionContext) {
  protected [models]val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  protected [models] class BoxLineTable(tag: Tag) extends Table[BoxLine](tag,"box_line") {
    def id = column[Long]("id",O.PrimaryKey,O.AutoInc)
    def unit_price = column[Double]("unit_price")
    def quantity = column[Int]("quantity")
    def sumOfLine = column[Double]("SumOfLine")
    def product_name = column[String]("product_name")
    def product_id = column[Long]("product_id")
    private def product_fk = foreignKey("product_id_fk",product_id,productTab)(_.id_product)
    def box_id = column[Long]("box_id")
    private def bill_fk = foreignKey("box_id_fk",box_id, boxTab)(_.id)
    def * = (id,unit_price,quantity,sumOfLine,product_name,product_id,box_id)<>((BoxLine.apply _).tupled,BoxLine.unapply)
  }
  import BoxRepo.BoxTable
  import ProductRepo.ProductTable

  protected [models] val boxLine = TableQuery[BoxLineTable]
  private val productTab = TableQuery[ProductTable]
  private val boxTab = TableQuery[BoxTable]

  def create(unit_price: Double, quantity: Int,sumOfLine: Double,product_name:String,product_id: Long,box_id:Long): Future[BoxLine] = db.run {
    (boxLine.map(bl =>(bl.unit_price,bl.quantity,bl.sumOfLine,bl.product_name,bl.product_id,bl.box_id))
      returning( boxLine.map(_.id))
      into {case((unit_price,quantity,sumOfLine,product_name,product_id,box_id),id) => BoxLine(id,unit_price,quantity,sumOfLine,product_name,product_id,box_id)}
      )+=(unit_price,quantity,sumOfLine,product_name,product_id,box_id)
  }


  def list(): Future[Seq[BoxLine]] = db.run {
    boxLine.result
  }

  def getById(id: Long): Future[BoxLine] = db.run {
    boxLine.filter(_.id === id).result.head
  }

  def getByIdOption(id: Long): Future[Option[BoxLine]] = db.run {
    boxLine.filter(_.id === id).result.headOption
  }

  def update(id: Long, new_box_line: BoxLine): Future[Unit] = {
    val box_line_to_update: BoxLine = new_box_line.copy(id)
    db.run {
      (boxLine.filter(_.id === id).update(box_line_to_update)).map(_ => ())
    }
  }

  def delete(id: Long): Future[Unit] = db.run {
    (boxLine.filter(_.id === id).delete).map(_ => ())
  }

}


