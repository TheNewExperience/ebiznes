package controllers

import javax.inject.{Inject, Singleton}
import models.{Bill, BillRepository, Box, BoxLine, BoxLineRepository, BoxRepository, LineItems, LineItemsRepository, Product, ProductRepository}
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import play.api.data.format.Formats._
import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents}

import scala.util.{Failure, Success}
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class BoxLineController @Inject()(boxLineRepo:BoxLineRepository, productRepo:ProductRepository, boxRepo:BoxRepository,cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val boxLineForm: Form[CreateBoxLineForm] = Form {
    mapping(
      "unit_price" ->  of[Double],
      "quantity" -> number,
      "sumOfLine" -> of[Double],
      "product_name" -> nonEmptyText,
      "product_id" -> longNumber,
      "box_id" ->longNumber

    )(CreateBoxLineForm.apply)(CreateBoxLineForm.unapply)
  }
  val updateBoxLineForm: Form[UpdateBoxLineForm] = Form {
    mapping(
      "id" -> longNumber,
      "unit_price" ->  of[Double],
      "quantity" -> number,
      "sumOfLine" -> of[Double],
      "product_name" -> nonEmptyText,
      "product_id" -> longNumber,
      "box_id" ->longNumber
    )(UpdateBoxLineForm.apply)(UpdateBoxLineForm.unapply)
  }

  def getBoxLines: Action[AnyContent] = Action.async { implicit request =>
    val boxLines = boxLineRepo.list()
    boxLines.map(lines => Ok(views.html.boxlines(lines)))
  }

  def getById(id: Long): Action[AnyContent] = Action.async { implicit request =>
    val boxLine = boxLineRepo.getByIdOption(id)
    boxLine.map(line => line match {
      case Some(l) => Ok(views.html.boxline(l))
      case None => Redirect(routes.BoxLineController.getBoxLines())
    })
  }

  def updateBoxLine(id: Long): Action[AnyContent] = Action.async { implicit reqest: MessagesRequest[AnyContent] =>
    var bx: Seq[Box] = Seq[Box]()
    val boxes = boxRepo.list().onComplete {
      case Success(boxes) => bx = boxes
      case Failure(_) => print("fail")
    }
    var prod: Seq[Product] = Seq[Product]()
    val products = productRepo.list().onComplete {
      case Success(pro) => prod = pro
      case Failure(_) => print("fail")
    }
    val line = boxLineRepo.getById(id)
    line.map(line => {
      val lineForm = updateBoxLineForm.fill(UpdateBoxLineForm(line.id,line.unit_price,line.quantity,line.SumOfLine,line.product_name,line.product_id,line.box_id))
      Ok(views.html.boxlineupdate(lineForm,  prod,bx))

    })
  }

  def updateBoxLineHandle = Action.async { implicit reqest =>
    var bx: Seq[Box] = Seq[Box]()
    val boxes = boxRepo.list().onComplete {
      case Success(boxes) => bx = boxes
      case Failure(_) => print("fail")
    }
    var prod: Seq[Product] = Seq[Product]()
    val products = productRepo.list().onComplete {
      case Success(pro) => prod = pro
      case Failure(_) => print("fail")
    }
    updateBoxLineForm.bindFromRequest().fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.boxlineupdate(errorForm,prod,bx))
        )
      },
      boxLine => {
        boxLineRepo.update(boxLine.id,BoxLine(boxLine.id,boxLine.unit_price,boxLine.quantity,boxLine.SumOfLine,boxLine.product_name,boxLine.product_id,boxLine.box_id)).map { _ =>
          Redirect(routes.BoxLineController.updateBoxLine(boxLine.id)).flashing("success" -> "box line updated")
        }
      }
    )
  }


  def addBoxLine: Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    var bx: Seq[Box] = Seq[Box]()
    val boxes = boxRepo.list().onComplete {
      case Success(boxes) => bx = boxes
      case Failure(_) => print("fail")
    }
    val products = productRepo.list()




      products.map(prod=>Ok(views.html.boxlineadd(boxLineForm, prod,bx)))

  }

  def addBoxLineHandle = Action.async { implicit request =>
    var bx: Seq[Box] = Seq[Box]()
    val boxes = boxRepo.list().onComplete {
      case Success(boxes) => bx = boxes
      case Failure(_) => print("fail")
    }
    var prod: Seq[Product] = Seq[Product]()
    val products = productRepo.list().onComplete {
      case Success(pro) => prod = pro
      case Failure(_) => print("fail")
    }

    boxLineForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.boxlineadd(errorForm,prod,bx))
        )
      },
      boxLine => {
        boxLineRepo.create(boxLine.unit_price,boxLine.quantity,boxLine.SumOfLine,boxLine.product_name,boxLine.product_id,boxLine.box_id).map { _ =>
          Redirect(routes.BoxLineController.addBoxLine()).flashing("success" -> "box line.created")
        }
      }
    )
  }

  def delete(id: Long): Action[AnyContent] = Action {
    boxLineRepo.delete(id)
    Redirect("/boxlines")
  }

}

case class CreateBoxLineForm( unit_price:Double, quantity: Int, SumOfLine:Double, product_name:String, product_id: Long, box_id:Long)

case class UpdateBoxLineForm(id:Long,  unit_price:Double, quantity: Int, SumOfLine:Double, product_name:String, product_id: Long, box_id:Long)