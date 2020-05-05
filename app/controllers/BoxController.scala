package controllers

import java.util.Calendar

import javax.inject.{Inject, Singleton}
import models.{Box, BoxRepository, PaymentMethodRepository, Payment_method, Product, ProductRepository, User, UserRepository}
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import play.api.data.format.Formats._
import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents}

import scala.util.{Failure, Success}
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class BoxController @Inject()(boxRepo: BoxRepository, productRepo:ProductRepository, userRepo:UserRepository,  paymentRepo: PaymentMethodRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val boxForm: Form[CreateBoxForm] = Form {
    mapping(
      "sumOf" ->  of[Double],
      "product_name" -> nonEmptyText,
      "quantity" -> number,
      "price" -> of[Double],
      "product_id" -> longNumber,
      "user_id" -> longNumber,
      "payment_id" -> number,

    )(CreateBoxForm.apply)(CreateBoxForm.unapply)
  }
  val updateBoxForm: Form[UpdateBoxForm] = Form {
    mapping(
      "id" -> longNumber,
      "sumOf" ->  of[Double],
      "product_name" -> nonEmptyText,
      "quantity" -> number,
      "price" -> of[Double],
      "product_id" -> longNumber,
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
    var prod: Seq[Product] = Seq[Product]()
    val products = productRepo.list().onComplete {
      case Success(pro) => prod = pro
      case Failure(_) => print("fail")
    }
    val box = boxRepo.getById(id)
    box.map(box => {
      val boxForm = updateBoxForm.fill(UpdateBoxForm(box.id,box.sumOf,box.productName,box.quantity,box.price,box.product_id,box.user_id,box.payment_id))
      Ok(views.html.boxupdate(boxForm, prod,us,pay))

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
    var prod: Seq[Product] = Seq[Product]()
    val products = productRepo.list().onComplete {
      case Success(pro) => prod = pro
      case Failure(_) => print("fail")
    }
    updateBoxForm.bindFromRequest().fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.boxupdate(errorForm,prod, us,pay))
        )
      },
      box => {
        boxRepo.update(box.id, Box(box.id,box.sumOf,box.productName,box.quantity,box.price,box.product_id,box.user_id,box.payment_id)).map { _ =>
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
    var pay: Seq[Payment_method] = Seq[Payment_method]()
    val payments = paymentRepo.list().onComplete {
      case Success(paym) => pay = paym
      case Failure(_) => print("fail")
    }

    val products = productRepo.list()


      products.map(prod=>Ok(views.html.boxadd(boxForm,prod, us, pay)))

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
    var prod: Seq[Product] = Seq[Product]()
    val products = productRepo.list().onComplete {
      case Success(pro) => prod = pro
      case Failure(_) => print("fail")
    }

    boxForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.boxadd(errorForm,prod, us, pay))
        )
      },
      box => {
        boxRepo.create(box.sumOf,box.productName,box.quantity,box.price,box.product_id,box.user_id,box.payment_id).map { _ =>
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

case class CreateBoxForm( sumOf: Double,productName: String,quantity:Int,price:Double, product_id: Long, user_id:Long, payment_id: Int)

case class UpdateBoxForm(id: Long, sumOf: Double,productName: String,quantity:Int,price:Double, product_id: Long, user_id:Long, payment_id: Int)