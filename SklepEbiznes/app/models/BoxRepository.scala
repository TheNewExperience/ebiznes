package models

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class BoxRepository @Inject()(dbConfigProvider: DatabaseConfigProvider, UserRepo: UserRepository, ProductRepo:ProductRepository,PaymentRepo: PaymentMethodRepository)(implicit ec: ExecutionContext) {
  protected [models] val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  protected [models] class BoxTable(tag: Tag) extends Table[Box](tag,"Box") {
    def id = column[Long]("box_id",O.PrimaryKey,O.AutoInc)
    def sumOf = column[Double]("sumOf")
    def user_id = column[Long]("user_id")
    private def user_fk = foreignKey("user_id_fk",user_id, userTab)(_.id_user)
    def payment_id = column[Int]("payment_method_id")
    private def payment_fk = foreignKey("payment_id_fk",payment_id, payTab)(_.id_payment)
    def * = (id,sumOf,user_id,payment_id)<>((Box.apply _).tupled,Box.unapply)
  }
  import ProductRepo.ProductTable
  import UserRepo.UserTable
  import PaymentRepo.PaymentMethodTable

  protected [models] val box = TableQuery[BoxTable]
  private val userTab = TableQuery[UserTable]
  private val payTab = TableQuery[PaymentMethodTable]

  def create(sumOf: Double,  user_id:Long, paymentMethod_id:Int): Future[Box] = db.run {
    (box.map(bo =>(bo.sumOf,bo.user_id,bo.payment_id))
      returning( box.map(_.id))
      into {case((sumOf,user_id,payment_id),id) => Box(id,sumOf,user_id,payment_id)}
      )+=(sumOf,user_id,paymentMethod_id)
  }


  def list(): Future[Seq[Box]] = db.run {
    box.result
  }

  def getById(id: Long): Future[Box] = db.run {
    box.filter(_.id === id).result.head
  }

  def getByIdOption(id: Long): Future[Option[Box]] = db.run {
    box.filter(_.id === id).result.headOption
  }

  def update(id: Long, new_Box: Box): Future[Unit] = {
    val boxToUpdate: Box = new_Box.copy(id)
    db.run {
      (box.filter(_.id === id).update(boxToUpdate)).map(_ => ())
    }
  }

  def delete(id: Long): Future[Unit] = db.run {
    (box.filter(_.id === id).delete).map(_ => ())
  }

}


