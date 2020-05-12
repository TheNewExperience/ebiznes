package controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import models.{Manufacturer, ManufacturerRepository}
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import play.api.libs.json._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class ManufacturerController @Inject()(ManufacturerRepo: ManufacturerRepository, cc: MessagesControllerComponents)(implicit  ec: ExecutionContext) extends MessagesAbstractController(cc) {


  val manufacturerForm: Form[CreateManufacturerForm] = Form {
    mapping(
      "name" ->nonEmptyText
    )(CreateManufacturerForm.apply)(CreateManufacturerForm.unapply)
  }

  val updateManufacturerForm: Form[UpdateManufacturerForm] = Form  {
    mapping(
      "id" ->number ,
      "name" ->nonEmptyText,
    )(UpdateManufacturerForm.apply)(UpdateManufacturerForm.unapply)
  }



  def getManufacturers: Action[AnyContent] = Action.async {
    val manufacturers = ManufacturerRepo.list()
    manufacturers.map(manufacturers => Ok(views.html.manufacturers(manufacturers)))
  }

  def getManufacturer(id: Int): Action[AnyContent] = Action.async {
    implicit request =>
      val manufacturer = ManufacturerRepo.getByIdOption(id)
      manufacturer.map(manufacturer => manufacturer match{
        case Some(manuf) =>Ok(views.html.manufacturer(manuf))
        case None => Redirect(routes.ManufacturerController.getManufacturers())
      })
  }

  def delete(id: Int):Action[AnyContent] = Action {
    ManufacturerRepo.delete(id)
    Redirect("/manufacturers")
  }

  def addManufacturer: Action[AnyContent] = Action.async  {implicit request: MessagesRequest[AnyContent] =>
    manufacturerForm.bindFromRequest.fold(
      errorForm=>{
        Future.successful(
                BadRequest(views.html.manufactureradd(manufacturerForm))
              )
      },
      manufacturer => {
        ManufacturerRepo.create(manufacturer.name).map { _ =>
          Redirect(routes.ManufacturerController.addManufacturer()).flashing("success"->"manufacturer.created")
        }
      }
    )



  }


  def updateManufacturer(id: Int): Action[AnyContent] = Action.async { implicit  request: MessagesRequest[AnyContent] =>
   val manufacturer = ManufacturerRepo.getById(id)
    manufacturer.map(manufacturer => {
      val manufacForm =  updateManufacturerForm.fill(UpdateManufacturerForm(manufacturer.id_manufacturer,manufacturer.name_manufacturer))
      Ok(views.html.manufacturerupdate(manufacForm))
    })

  }


  def updateManufacturerHandle = Action.async{implicit request =>
    updateManufacturerForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.manufacturerupdate(errorForm))
        )
      },
      manufacturer=> {
        ManufacturerRepo.update(manufacturer.id, Manufacturer(manufacturer.id,manufacturer.name)).map { _ =>
          Redirect(routes.ManufacturerController.updateManufacturer(manufacturer.id)).flashing("success" -> "manufacturer updated")
        }
      }
    )
  }

  //Json
  def getManufacturersJson() = Action.async{implicit request  =>
    val manufacturers = ManufacturerRepo.list()
    manufacturers.map(manuf => Ok(Json.toJson(manuf.toArray)))
  }


  def getManufacturerJson(id: Int) = Action.async({ implicit  request =>
    val manufaturer = ManufacturerRepo.getById(id)
    manufaturer.map(manuf => Ok(Json.toJson(manuf)))

  })


  def addManufacturerJson() = Action.async { implicit  request =>
    manufacturerForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest("something went wrong please try again later")
        )
      },
      manufacturer => {
        ManufacturerRepo.create(manufacturer.name).map { manuf =>
          Ok(Json.toJson(manuf))
        }
      }
    )

  }

  def updateManufacturerJson(id: Int) = Action.async { implicit  request =>

    updateManufacturerForm.bindFromRequest().fold(
      errorForm => {
        Future.successful(
          BadRequest("something went wrong please try again later")
        )
      },
      manufacturer => {
        ManufacturerRepo.update(id, Manufacturer(id, manufacturer.name)).map { _ =>
          Ok(Json.toJson(Manufacturer(id,manufacturer.name)))
        }
      }
    )
  }

  def deleteManufacturerJson(id:Int) = Action { implicit  request =>

    ManufacturerRepo.delete(id)
    Ok("Deleted product.")
  }

}
case class CreateManufacturerForm(name:String)

case class UpdateManufacturerForm(id: Int, name:String)
