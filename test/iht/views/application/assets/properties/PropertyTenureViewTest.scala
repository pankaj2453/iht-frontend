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

package iht.views.application.assets.properties

import iht.forms.ApplicationForms._
import iht.models.application.assets.Property
import iht.testhelpers.CommonBuilder
import iht.views.application.{ApplicationPageBehaviour, CancelComponent, ValueViewBehaviour}
import iht.views.html.application.asset.properties.property_tenure
import play.api.data.Form
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat.Appendable

class PropertyTenureViewTest extends ApplicationPageBehaviour[Property] {
  override def guidanceParagraphs = Set(
    Messages("iht.estateReport.assets.property.youCan"),
    Messages("iht.estateReport.assets.property.findOutFromLandRegistry"),
    Messages("page.iht.application.assets.property.tenure.guidance1b")
  )

  override def pageTitle = Messages("iht.estateReport.assets.properties.freeholdOrLeasehold")

  override def browserTitle = Messages("page.iht.application.assets.property.tenure.browserTitle")

  override def formTarget = Some(CommonBuilder.DefaultCall1)

  override def cancelComponent = Some(
    CancelComponent(
      CommonBuilder.DefaultCall2,
      Messages("iht.estateReport.assets.properties.returnToAddAProperty")
    )
  )

  override val cancelId: String = "cancel-button"

  override def form: Form[Property] = propertyTenureForm

  override def formToView: Form[Property] => Appendable =
    form =>
      property_tenure(form, CommonBuilder.DefaultCall1, CommonBuilder.DefaultCall2)

  "Property Tenure Question View" must {
    behave like applicationPageWithErrorSummaryBox()
  }

  "includes \"freehold\"" in {
    val elementDiv = doc.getElementById("tenure-freehold-main")
    elementDiv.text shouldBe Messages("page.iht.application.assets.tenure.freehold.label")
    val elementSpan = doc.getElementById("tenure-freehold-hint")
    elementSpan.text shouldBe Messages("page.iht.application.assets.tenure.freehold.hint")
  }

}
