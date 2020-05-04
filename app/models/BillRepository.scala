package models

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class BillRepository @Inject()(dbConfigProvider: DatabaseConfigProvider, UserRepo: UserRepository, PaymentRepo:PaymentMethodRepository)(implicit ec: ExecutionContext) {
  protected val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class BillTable(tag: Tag) extends Table[Bill](tag,"bills") {
    def id_bill = column[Long]("id_bill",O.PrimaryKey,O.AutoInc)
    def name = column[String]("name")
    def sum_of = column[Double]("sum_of")
    def user_id = column[Long]("user_id")
    def user_fk = foreignKey("user_id_fk",user_id, userTab)(_.id_user)
    def created_at = column[String]("created_at")
    def is_open = column[Int]("is_open")
    def payment_id = column[Int]("payment_method_id")
    def payment_fk = foreignKey("payment_id_fk",payment_id, payTab)(_.id_payment)
    def * = (id_bill,name,sum_of,user_id,created_at,is_open,payment_id)<>((Bill.apply _).tupled,Bill.unapply)
  }
  import UserRepo.UserTable
  import PaymentRepo.PaymentMethodTable

  val bill = TableQuery[BillTable]
  val payTab = TableQuery[PaymentMethodTable]
  val userTab = TableQuery[UserTable]

  def create(name: String, sumOf: Double, userId: Long, created_at: String, is_open: Int, payment_method: Int): Future[Bill] = db.run {
    (bill.map(b =>(b.name,b.sum_of,b.user_id,b.created_at,b.is_open,b.payment_id))
      returning( bill.map(_.id_bill))
      into {case((name,sumOf,userId,created_at,is_open,payment_method),id) => Bill(id,name,sumOf,userId,created_at,is_open,payment_method)}
      )+=(name,sumOf,userId,created_at,is_open,payment_method)
  }


  def list(): Future[Seq[Bill]] = db.run {
    bill.result
  }

  def getById(id: Long): Future[Bill] = db.run {
    bill.filter(_.id_bill === id).result.head
  }

  def getByIdOption(id: Long): Future[Option[Bill]] = db.run {
    bill.filter(_.id_bill === id).result.headOption
  }

  def update(id: Long, new_bill: Bill): Future[Unit] = {
    val billToUpdate: Bill = new_bill.copy(id)
    db.run {
      (bill.filter(_.id_bill === id).update(billToUpdate)).map(_ => ())
    }
  }

  def delete(id: Long): Future[Unit] = db.run {
    (bill.filter(_.id_bill === id).delete).map(_ => ())
  }

}


