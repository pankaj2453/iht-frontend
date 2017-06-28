/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package iht.controllers.application.exemptions.qualifyingBody

import javax.inject.{Inject, Singleton}

import iht.constants.IhtProperties
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms.qualifyingBodyValueForm
import iht.models._
import iht.models.application.ApplicationDetails
import iht.models.application.exemptions._
import iht.utils.CommonHelper
import iht.views.html.application.exemption.qualifyingBody.qualifying_body_value
import play.api.i18n.MessagesApi
import play.api.mvc.{Call, Request}
import uk.gov.hmrc.play.frontend.auth.AuthContext

import scala.concurrent.Future

@Singleton
class QualifyingBodyValueController @Inject()(val messagesApi: MessagesApi, val ihtProperties: IhtProperties, val applicationForms: ApplicationForms) extends EstateController {

  val submitUrl = CommonHelper.addFragmentIdentifier(routes.QualifyingBodyValueController.onSubmit(),
    Some(ihtProperties.ExemptionsOtherValueID))
  val cancelUrl = routes.QualifyingBodyDetailsOverviewController.onPageLoad()

  private def editCancelUrl(id: String) = routes.QualifyingBodyDetailsOverviewController.onEditPageLoad(id)
  private def editSubmitUrl(id: String) = CommonHelper.addFragmentIdentifier(routes.QualifyingBodyValueController.onEditSubmit(id),
    Some(ihtProperties.ExemptionsOtherValueID))

  def locationAfterSuccessfulSave(optionID: Option[String]) = CommonHelper.getOrException(
    optionID.map(id => routes.QualifyingBodyDetailsOverviewController.onEditPageLoad(id)))

  val updateApplicationDetails: (ApplicationDetails, Option[String], QualifyingBody) => (ApplicationDetails, Option[String]) =
    (appDetails, id, qualifyingBody) => {
      val seekID = id.getOrElse("")
      val qbList = appDetails.qualifyingBodies

      val updatedQBTuple: (Seq[QualifyingBody], String) = qbList.find(_.id.getOrElse("") equals seekID) match {
        case None =>
          id.fold {
            val nextID = nextId(qbList)
            (qbList :+ qualifyingBody.copy(id = Some(nextID)), nextID)
          } { reqId => throw new RuntimeException("Id " + reqId + " can not be found") }
        case Some(matchedQualifyingBody) =>
          val updatedQualifyingBody: QualifyingBody = matchedQualifyingBody.copy(totalValue = qualifyingBody.totalValue)
          (qbList.updated(qbList.indexOf(matchedQualifyingBody), updatedQualifyingBody), seekID)
      }
      (appDetails.copy(qualifyingBodies = updatedQBTuple._1), Some(updatedQBTuple._2))
    }

  def onPageLoad = authorisedForIht {
    implicit user =>
      implicit request => {
        withRegistrationDetails { regDetails =>
          Future.successful(Ok(
            iht.views.html.application.exemption.qualifyingBody.qualifying_body_value(qualifyingBodyValueForm,
              regDetails,
              submitUrl,
              cancelUrl)))
        }
      }
  }

  def onEditPageLoad(id: String) = authorisedForIht {
    implicit user =>
      implicit request => {
        estateElementOnEditPageLoadWithNavigation[QualifyingBody](qualifyingBodyValueForm,
          qualifying_body_value.apply,
          retrieveQualifyingBodyDetailsOrExceptionIfInvalidID(id),
          editSubmitUrl(id),
          editCancelUrl(id))
      }
  }

  def onSubmit = authorisedForIht {
    implicit user =>
      implicit request => {
        doSubmit(
          submitUrl = submitUrl,
          cancelUrl = cancelUrl)
      }
  }

  def onEditSubmit(id: String) = authorisedForIht {
    implicit user =>
      implicit request => {
        doSubmit(
          submitUrl = editSubmitUrl(id),
          cancelUrl = editCancelUrl(id),
          Some(id))
      }
  }

  private def doSubmit(submitUrl: Call,
                       cancelUrl: Call,
                       charityId: Option[String] = None)(
                        implicit user: AuthContext, request: Request[_]) = {
    estateElementOnSubmitWithIdAndNavigation[QualifyingBody](
      qualifyingBodyValueForm,
      qualifying_body_value.apply,
      updateApplicationDetails,
      (_, updatedQualifyingBodyID) => locationAfterSuccessfulSave(updatedQualifyingBodyID),
      None,
      charityId,
      submitUrl,
      cancelUrl
    )
  }
}
