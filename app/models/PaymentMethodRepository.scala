package models

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class PaymentMethodRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class PaymentMethodTable(tag: Tag) extends Table[Payment_method](tag, "payment_methods") {
    def id_payment = column[Int]("id_payment_method", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def * = (id_payment, name) <> ((Payment_method.apply _).tupled, Payment_method.unapply)

  }

  val payment= TableQuery[PaymentMethodTable]

  def create(p_name: String): Future[Payment_method] = db.run {
    (payment.map(p => (p.name))
      returning payment.map(_.id_payment)
      into ((name, id) => Payment_method(id, name))
      ) += p_name


  }

  def list(): Future[Seq[Payment_method]] = db.run {
    payment.result
  }

  def getById(id: Int): Future[Payment_method] = db.run {
    payment.filter(_.id_payment === id).result.head
  }

  def getByIdOption(id: Int): Future[Option[Payment_method]] = db.run {
    payment.filter(_.id_payment === id).result.headOption
  }

  def update(id: Int, new_pay: Payment_method): Future[Unit] = {
    val paymentToUpdate: Payment_method = new_pay.copy(id)
    db.run {
      (payment.filter(_.id_payment === id).update(paymentToUpdate)).map(_ => ())
    }
  }

  def delete(id: Int): Future[Unit] = db.run {
    (payment.filter(_.id_payment === id).delete).map(_ => ())
  }

}


