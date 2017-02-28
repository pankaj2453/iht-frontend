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

package iht.views.application.exemption.qualifyingBody

import iht.testhelpers.CommonBuilder
import iht.views.html.application.exemption.qualifyingBody.qualifying_body_delete_confirm
import iht.views.{ExitComponent, GenericNonSubmittablePageBehaviour}
import play.api.i18n.Messages.Implicits._
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import scala.concurrent.ExecutionContext.Implicits.global

class QualifyingBodyDeleteConfirmViewTest extends GenericNonSubmittablePageBehaviour {
  implicit def request: FakeRequest[AnyContentAsEmpty.type] = createFakeRequest()

  override def guidanceParagraphs = Set.empty

  override def pageTitle = messagesApi("iht.estateReport.exemptions.qualifyingBodies.confirmDeleteQualifyingBody")

  override def browserTitle = messagesApi("page.iht.application.exemptions.qualifyingBodyDelete.browserTitle")

  def exitComponent = Some(
    ExitComponent(
      iht.controllers.application.exemptions.qualifyingBody.routes.QualifyingBodiesOverviewController.onPageLoad(),
      messagesApi("iht.estateReport.exemptions.qualifyingBodies.returnToAssetsLeftToQualifyingBodies")
    )
  )

  val nameOfQualifyingBody = CommonBuilder.qualifyingBody.map(_.name).fold("")(identity)

  def view = qualifying_body_delete_confirm(nameOfQualifyingBody, CommonBuilder.DefaultCall1).toString

  "Delete qualifying body confirmation page Question View" must {
    behave like nonSubmittablePage()

    "show submit button with correct target and text" in {
      doc.getElementsByTag("form").attr("action") shouldBe CommonBuilder.DefaultCall1.url
      val submitButton = doc.getElementById("confirm-delete")
      submitButton.text() shouldBe messagesApi("site.button.confirmDelete")
    }

    "show the name of the qualifying body" in {
      val nameParagraph = doc.getElementById("qualifying-body-name")
      nameParagraph.text shouldBe nameOfQualifyingBody
    }
  }
}