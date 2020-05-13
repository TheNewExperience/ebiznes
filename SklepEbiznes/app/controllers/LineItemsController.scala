package controllers



import javax.inject.{Inject, Singleton}
import models.{Bill, BillRepository,LineItems,LineItemsRepository,Product,ProductRepository}
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import play.api.data.format.Formats._
import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents}

import scala.util.{Failure, Success}
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._

@Singleton
class LineItemsController @Inject()(lineItRepo:LineItemsRepository, billRepo:BillRepository,productRepo:ProductRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val lineForm: Form[CreateLineItemsForm] = Form {
    mapping(
      "is_billed" -> number,
      "unit_price" ->  of[Double],
      "quantity" -> number,
      "item_name" -> nonEmptyText,
      "price" -> of[Double],
      "product_id" -> longNumber,
      "bill_id" ->longNumber

    )(CreateLineItemsForm.apply)(CreateLineItemsForm.unapply)
  }
  val updateLineForm: Form[UpdateLineItemsForm] = Form {
    mapping(
      "id" -> longNumber,
      "is_billed" -> number,
      "unit_price" ->  of[Double],
      "quantity" -> number,
      "item_name" -> nonEmptyText,
      "price" -> of[Double],
      "product_id" -> longNumber,
      "bill_id" ->longNumber
    )(UpdateLineItemsForm.apply)(UpdateLineItemsForm.unapply)
  }

  def getLineItems: Action[AnyContent] = Action.async { implicit request =>
    val lineItems = lineItRepo.list()
    lineItems.map(lines => Ok(views.html.lines(lines)))
  }

  def getById(id: Long): Action[AnyContent] = Action.async { implicit request =>
    val line = lineItRepo.getByIdOption(id)
    line.map(line => line match {
      case Some(l) => Ok(views.html.line(l))
      case None => Redirect(routes.LineItemsController.getLineItems())
    })
  }

  def updateLine(id: Long): Action[AnyContent] = Action.async { implicit reqest: MessagesRequest[AnyContent] =>
    var bs: Seq[Bill] = Seq[Bill]()
    val bills = billRepo.list().onComplete {
      case Success(bills) => bs = bills
      case Failure(_) => print("fail")
    }
    var prod: Seq[Product] = Seq[Product]()
    val products = productRepo.list().onComplete {
      case Success(pro) => prod = pro
      case Failure(_) => print("fail")
    }
    val line = lineItRepo.getById(id)
    line.map(line => {
      val lineForm = updateLineForm.fill(UpdateLineItemsForm(line.id,line.is_billed,line.unit_price,line.quantity,line.item_name,line.price,line.product_id,line.bill_id))
      Ok(views.html.lineupdate(lineForm,  prod,bs))

    })
  }

  def updateLineHandle = Action.async { implicit reqest =>
    var bs: Seq[Bill] = Seq[Bill]()
    val bills = billRepo.list().onComplete {
      case Success(bills) => bs = bills
      case Failure(_) => print("fail")
    }
    var prod: Seq[Product] = Seq[Product]()
    val products = productRepo.list().onComplete {
      case Success(pro) => prod = pro
      case Failure(_) => print("fail")
    }
    updateLineForm.bindFromRequest().fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.lineupdate(errorForm,prod,bs))
        )
      },
      line => {
        lineItRepo.update(line.id, LineItems(line.id,line.is_billed,line.unit_price,line.quantity,line.item_name,line.price,line.product_id,line.bill_id)).map { _ =>
          Redirect(routes.LineItemsController.updateLine(line.id)).flashing("success" -> "line updated")
        }
      }
    )
  }


  def addLine: Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    var bs: Seq[Bill] = Seq[Bill]()
    val bills = billRepo.list().onComplete {
      case Success(bills) => bs = bills
      case Failure(_) => print("fail")
    }

    val products = productRepo.list()




      products.map(prod=>Ok(views.html.lineadd(lineForm, prod,bs)))

  }

  def addLineHandle = Action.async { implicit request =>
    var bs: Seq[Bill] = Seq[Bill]()
    val bills = billRepo.list().onComplete {
      case Success(bills) => bs = bills
      case Failure(_) => print("fail")
    }
    var prod: Seq[Product] = Seq[Product]()
    val products = productRepo.list().onComplete {
      case Success(pro) => prod = pro
      case Failure(_) => print("fail")
    }

    lineForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.lineadd(errorForm,prod,bs))
        )
      },
      line => {
        lineItRepo.create(line.is_billed,line.unit_price,line.quantity,line.item_name,line.price,line.product_id,line.bill_id).map { _ =>
          Redirect(routes.LineItemsController.addLine()).flashing("success" -> "line.created")
        }
      }
    )
  }

  def delete(id: Long): Action[AnyContent] = Action {
    lineItRepo.delete(id)
    Redirect("/lines")
  }

  //JSONS

  def getLinesItemsJson() = Action.async{implicit request  =>
    val linesItems = lineItRepo.list()
    linesItems.map(linIt => Ok(Json.toJson(linIt.toArray)))
  }


  def getLineItemsJson(id: Long) = Action.async({ implicit  request =>
    val lineItem = lineItRepo.getById(id)
    lineItem.map(lineIt => Ok(Json.toJson(lineIt)))

  })


  def addLineItemsJson() = Action.async { implicit  request =>
    lineForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest("something went wrong please try again later")
        )
      },
      lineIt => {
        lineItRepo.create(lineIt.is_billed,lineIt.unit_price,lineIt.quantity,lineIt.item_name,lineIt.price,lineIt.product_id,lineIt.bill_id).map { lineItem =>
          Ok(Json.toJson(lineItem))
        }
      }
    )

  }

  def updateLineItemsJson(id: Long) = Action.async { implicit  request =>

    updateLineForm.bindFromRequest().fold(
      errorForm => {
        Future.successful(
          BadRequest("something went wrong please try again later")
        )
      },
      lineIt => {
        lineItRepo.update(id, LineItems(id, lineIt.is_billed,lineIt.unit_price,lineIt.quantity,lineIt.item_name,lineIt.price,lineIt.product_id,lineIt.bill_id)).map { _ =>
          Ok(Json.toJson(LineItems(id, lineIt.is_billed,lineIt.unit_price,lineIt.quantity,lineIt.item_name,lineIt.price,lineIt.product_id,lineIt.bill_id)))
        }
      }
    )
  }

  def deleteLineItemsJson(id:Long) = Action { implicit  request =>

    lineItRepo.delete(id)
    Ok("Deleted product.")
  }

}

case class CreateLineItemsForm( is_billed:Int,unit_price:Double,quantity: Int,item_name:String,price: Double,product_id: Long, bill_id:Long)

case class UpdateLineItemsForm(id:Long,is_billed:Int,unit_price:Double,quantity: Int,item_name:String,price: Double,product_id: Long, bill_id:Long)