package controllers



import javax.inject.{Inject, Singleton}
import models.{Box, BoxRepository, PaymentMethodRepository, Payment_method,  User, UserRepository}
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import play.api.data.format.Formats._
import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents}

import scala.util.{Failure, Success}
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class BoxController @Inject()(boxRepo: BoxRepository,  userRepo:UserRepository,  paymentRepo: PaymentMethodRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val boxForm: Form[CreateBoxForm] = Form {
    mapping(
      "sumOf" ->  of[Double],
      "user_id" -> longNumber,
      "payment_id" -> number,

    )(CreateBoxForm.apply)(CreateBoxForm.unapply)
  }
  val updateBoxForm: Form[UpdateBoxForm] = Form {
    mapping(
      "id" -> longNumber,
      "sumOf" ->  of[Double],
      "user_id" -> longNumber,
      "payment_id" -> number,
    )(UpdateBoxForm.apply)(UpdateBoxForm.unapply)
  }

  def getBoxes: Action[AnyContent] = Action.async { implicit request =>
    val boxes = boxRepo.list()
    boxes.map(boxes => Ok(views.html.boxes(boxes)))
  }

  def getBox(id: Long): Action[AnyContent] = Action.async { implicit request =>
    val box = boxRepo.getByIdOption(id)
    box.map(box => box match {
      case Some(b) => Ok(views.html.box(b))
      case None => Redirect(routes.BoxController.getBoxes())
    })
  }

  def updateBox(id: Long): Action[AnyContent] = Action.async { implicit reqest: MessagesRequest[AnyContent] =>
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

    val box = boxRepo.getById(id)
    box.map(box => {
      val boxForm = updateBoxForm.fill(UpdateBoxForm(box.id,box.sumOf,box.user_id,box.payment_id))
      Ok(views.html.boxupdate(boxForm, us,pay))

    })
  }

  def updateBoxHandle = Action.async { implicit reqest =>
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

    updateBoxForm.bindFromRequest().fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.boxupdate(errorForm, us,pay))
        )
      },
      box => {
        boxRepo.update(box.id, Box(box.id,box.sumOf,box.user_id,box.payment_id)).map { _ =>
          Redirect(routes.BoxController.updateBox(box.id)).flashing("success" -> "box updated")
        }
      }
    )
  }


  def addBox: Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    var us: Seq[User] = Seq[User]()
    val users = userRepo.list().onComplete {
      case Success(users) => us = users
      case Failure(_) => print("fail")
    }

    val payments = paymentRepo.list()

      payments.map(pay=>Ok(views.html.boxadd(boxForm, us, pay)))

  }

  def addBoxHandle = Action.async { implicit request =>
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


    boxForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.boxadd(errorForm,us, pay))
        )
      },
      box => {
        boxRepo.create(box.sumOf,box.user_id,box.payment_id).map { _ =>
          Redirect(routes.BoxController.addBox()).flashing("success" -> "box.created")
        }
      }
    )
  }

  def delete(id: Long): Action[AnyContent] = Action {
    boxRepo.delete(id)
    Redirect("/boxes")
  }

}

case class CreateBoxForm( sumOf: Double, user_id:Long, payment_id: Int)

case class UpdateBoxForm(id: Long, sumOf: Double, user_id:Long, payment_id: Int)