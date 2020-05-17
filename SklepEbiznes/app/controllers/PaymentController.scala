package controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import models.{Payment_method, PaymentMethodRepository}
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import play.api.libs.json._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class PaymentController @Inject()(PaymentRepo: PaymentMethodRepository, cc: MessagesControllerComponents)(implicit  ec: ExecutionContext) extends MessagesAbstractController(cc) {


  val paymentForm: Form[CreatePaymentForm] = Form {
    mapping(
      "name" ->nonEmptyText
    )(CreatePaymentForm.apply)(CreatePaymentForm.unapply)
  }

  val updatePaymentForm: Form[UpdatePaymentForm] = Form  {
    mapping(
      "id" ->number ,
      "name" ->nonEmptyText,
    )(UpdatePaymentForm.apply)(UpdatePaymentForm.unapply)
  }




  def getPayments: Action[AnyContent] = Action.async {
    val payments = PaymentRepo.list()
    payments.map(payments => Ok(views.html.payments(payments)))
  }

  def getPayment(id: Int): Action[AnyContent] = Action.async {
    implicit request =>
      val payment = PaymentRepo.getByIdOption(id)
      payment.map(payment => payment match{
        case Some(pay) =>Ok(views.html.payment(pay))
        case None => Redirect(routes.PaymentController.getPayments())
      })
  }

  def delete(id: Int):Action[AnyContent] = Action {
    PaymentRepo.delete(id)
    Redirect("/payments")
  }

  def addPayment: Action[AnyContent] = Action.async  {implicit request: MessagesRequest[AnyContent] =>
    paymentForm.bindFromRequest.fold(
      errorForm=>{
        Future.successful(
                BadRequest(views.html.paymentadd(errorForm))
              )
      },
      category => {
        PaymentRepo.create(category.name).map { _ =>
          Redirect(routes.PaymentController.addPayment()).flashing("success"->"payment.created")
        }
      }
    )



  }



  def updatePayment(id: Int): Action[AnyContent] = Action.async { implicit  request: MessagesRequest[AnyContent] =>
   val payment = PaymentRepo.getById(id)
    payment.map(payment => {
      val payForm =  updatePaymentForm.fill(UpdatePaymentForm(payment.id_payment_method,payment.name))
      Ok(views.html.paymentupdate(payForm))
    })

  }


  def updatePaymentHandle = Action.async{implicit request =>
    updatePaymentForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.paymentupdate(errorForm))
        )
      },
      payment=> {
        PaymentRepo.update(payment.id, Payment_method(payment.id,payment.name)).map { _ =>
          Redirect(routes.PaymentController.updatePayment(payment.id)).flashing("success" -> "payment updated")
        }
      }
    )
  }


  //JSONS

  def getPaymentsJson() = Action.async{implicit request  =>
    val payments = PaymentRepo.list()
    payments.map(paym => Ok(Json.toJson(paym.toArray)))
  }


  def getPaymentJson(id: Int) = Action.async({ implicit  request =>
    val payments = PaymentRepo.getById(id)
    payments.map(paym => Ok(Json.toJson(paym)))

  })


  def addPaymentJson() = Action.async { implicit  request =>
    paymentForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest("something went wrong please try again later")
        )
      },
      payment => {
        PaymentRepo.create(payment.name).map { paym =>
          Ok(Json.toJson(paym))
        }
      }
    )

  }

  def updatePaymentJson(id: Int) = Action.async { implicit  request =>

    updatePaymentForm.bindFromRequest().fold(
      errorForm => {
        Future.successful(
          BadRequest("something went wrong please try again later")
        )
      },
      payment => {
        PaymentRepo.update(id, Payment_method(id, payment.name)).map { _ =>
          Ok(Json.toJson(Payment_method(id, payment.name)))
        }
      }
    )
  }

  def deletePaymentJson(id:Int) = Action { implicit  request =>

    PaymentRepo.delete(id)
    Ok("Deleted product.")
  }


}
case class CreatePaymentForm(name:String)

case class UpdatePaymentForm(id: Int, name:String)
