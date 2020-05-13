package controllers

import java.util.Calendar

import javax.inject.{Inject, Singleton}
import models.{Bill, BillRepository, PaymentMethodRepository, Payment_method, User, UserRepository}
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import play.api.data.format.Formats._
import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents}

import scala.util.{Failure, Success}
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class BillController @Inject()(userRepo: UserRepository, billRepo: BillRepository, paymentRepo: PaymentMethodRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val billForm: Form[CreateBillForm] = Form {
    mapping(
      "name" -> nonEmptyText,
      "sumOf" ->  of[Double],
      "user_id" -> longNumber,
      "created_at" -> ignored(Calendar.getInstance.getTime.toString),
      "is_open" -> number(max=0,min=1),
      "payment_id" -> number,

    )(CreateBillForm.apply)(CreateBillForm.unapply)
  }
  val updateBillForm: Form[UpdateBillForm] = Form {
    mapping(
      "id" -> longNumber,
      "name" -> nonEmptyText,
      "sumOf" ->  of[Double],
      "user_id" -> longNumber,
      "created_at" -> ignored(Calendar.getInstance.getTime.toString),
      "is_open" -> number(max=0,min=1),
      "payment_id" -> number,
    )(UpdateBillForm.apply)(UpdateBillForm.unapply)
  }

  def getBills: Action[AnyContent] = Action.async { implicit request =>
    val bills = billRepo.list()
    bills.map(bills => Ok(views.html.bills(bills)))
  }

  def getBill(id: Long): Action[AnyContent] = Action.async { implicit request =>
    val bill = billRepo.getByIdOption(id)
    bill.map(bill => bill match {
      case Some(b) => Ok(views.html.bill(b))
      case None => Redirect(routes.BillController.getBills())
    })
  }

  def updateBill(id: Long): Action[AnyContent] = Action.async { implicit reqest: MessagesRequest[AnyContent] =>
    var us: Seq[User] = Seq[User]()
    val users = userRepo.list().onComplete {
      case Success(users) => us = users
      case Failure(_) => print("fail")
    }
    var pay: Seq[Payment_method] = Seq[Payment_method]()
    val payments = paymentRepo.list().onComplete {
      case Success(paym) => pay = paym
      case Failure(_) => print("fail")
    }
    val bill = billRepo.getById(id)
    bill.map(bill => {
      val billForm = updateBillForm.fill(UpdateBillForm(bill.id_bill,bill.name,bill.sumOf,bill.userId,bill.created_at,bill.is_open,bill.payment_method))
      Ok(views.html.billupdate(billForm, us, pay))

    })
  }

  def updateBillHandle = Action.async { implicit reqest =>
    var us: Seq[User] = Seq[User]()
    val users = userRepo.list().onComplete {
      case Success(users) => us = users
      case Failure(_) => print("fail")
    }
    var pay: Seq[Payment_method] = Seq[Payment_method]()
    val payments = paymentRepo.list().onComplete {
      case Success(paym) => pay = paym
      case Failure(_) => print("fail")
    }
    updateBillForm.bindFromRequest().fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.billupdate(errorForm, us,pay))
        )
      },
      bill => {
        billRepo.update(bill.id_bill, Bill(bill.id_bill, bill.name,bill.sumOf,bill.userId,bill.created_at,bill.is_open,bill.payment_method)).map { _ =>
          Redirect(routes.BillController.updateBill(bill.id_bill)).flashing("success" -> "bill updated")
        }
      }
    )
  }


  def addBill: Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    var us: Seq[User] = Seq[User]()
    val users = userRepo.list().onComplete {
      case Success(users) => us = users
      case Failure(_) => print("fail")
    }
    val payments = paymentRepo.list()

      payments.map(pay=>Ok(views.html.billadd(billForm, us, pay)))

  }

  def addBillHandle = Action.async { implicit request =>
    var us: Seq[User] = Seq[User]()
    val users = userRepo.list().onComplete {
      case Success(users) => us = users
      case Failure(_) => print("fail")
    }
    var pay: Seq[Payment_method] = Seq[Payment_method]()
    val payments = paymentRepo.list().onComplete {
      case Success(paym) => pay = paym
      case Failure(_) => print("fail")
    }


    billForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.billadd(errorForm, us, pay))
        )
      },
      bill => {
        billRepo.create(bill.name,bill.sumOf,bill.userId,bill.created_at,bill.is_open,bill.payment_method).map { _ =>
          Redirect(routes.BillController.addBill()).flashing("success" -> "bill.created")
        }
      }
    )
  }

  def delete(id: Long): Action[AnyContent] = Action {
    billRepo.delete(id)
    Redirect("/bills")
  }

}

case class CreateBillForm(name: String, sumOf: Double, userId: Long, created_at: String, is_open: Int, payment_method: Int)

case class UpdateBillForm(id_bill: Long, name: String, sumOf: Double, userId: Long, created_at: String, is_open: Int, payment_method: Int)