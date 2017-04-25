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

package iht.viewmodels.application.overview

import iht.models.application.assets._
import iht.models.application.debts._
import iht.models.application.exemptions._
import iht.testhelpers.CommonBuilder
import iht.{FakeIhtApp, TestUtils}
import org.scalatest.BeforeAndAfter
import org.scalatest.mock.MockitoSugar
import play.api.i18n.MessagesApi
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.play.test.UnitSpec
import iht.testhelpers.TestHelper._

class ReducingEstateValueSectionViewModelTest
  extends UnitSpec with FakeIhtApp with MockitoSugar with TestUtils with BeforeAndAfter {

  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  val applicationDetails = CommonBuilder.buildApplicationDetails
  val regDetailsSinglePerson = CommonBuilder.buildRegistrationDetails3
  val regDetailsMarriedPerson = CommonBuilder.buildRegistrationDetails4
  val defaultAssetValue = 100

  //region Exemptions
  "Reducing value of estate view model" must {
    "have an id of 'exemptions' for the exemptions row" in {
      val viewModel = ReducingEstateValueSectionViewModel(applicationDetails, regDetailsMarriedPerson)

      viewModel.exemptionRow.id shouldBe EstateExemptionsID
    }

    "have the correct caption for the exemptions row" in {
      val viewModel = ReducingEstateValueSectionViewModel(applicationDetails, regDetailsMarriedPerson)

      viewModel.exemptionRow.label shouldBe messagesApi("iht.estateReport.exemptions.title")
    }

    "have a blank value for exemptions when there are no exemptions" in {
      val viewModel = ReducingEstateValueSectionViewModel(applicationDetails, regDetailsMarriedPerson)

      viewModel.exemptionRow.value shouldBe ""
    }

    "have a blank value for exemptions when there are exemptions but no values have been given" in {
      val appDetails = applicationDetails copy (allExemptions = Some(AllExemptions(
        partner = Some(PartnerExemption(None, None, None, None, None, None, None)),
        charity = None,
        qualifyingBody = None)))
      val viewModel = ReducingEstateValueSectionViewModel(appDetails, regDetailsMarriedPerson)

      viewModel.exemptionRow.value shouldBe ""
    }

    "have a No exemptions message when all exemptions complete without a value given" in {
      val appDetails = CommonBuilder.buildExemptionsWithNoValues(applicationDetails)
      val viewModel = ReducingEstateValueSectionViewModel(appDetails, regDetailsMarriedPerson)

      viewModel.exemptionRow.value shouldBe messagesApi("page.iht.application.estateOverview.exemptions.noExemptionsValue")
    }

    "have the correct value with a negative pound sign for assets where there are some exemptions" in {
      val appDetails = CommonBuilder.buildSomeExemptions(applicationDetails)
      val viewModel = ReducingEstateValueSectionViewModel(appDetails, regDetailsMarriedPerson)

      viewModel.exemptionRow.value shouldBe "-£44.45"
    }

    "show View or Change when all questions are completed and deceased had a marital status other than single" in {
      val appDetails = CommonBuilder.buildExemptionsWithNoValues(applicationDetails)

      val viewModel = ReducingEstateValueSectionViewModel(appDetails, regDetailsMarriedPerson)

      viewModel.exemptionRow.linkText shouldBe messagesApi("iht.viewOrChange")
    }

    "show View or Change when all questions are completed and deceased had a marital of single" in {
      val appDetails = CommonBuilder.buildExemptionsWithNoValues(applicationDetails)

      val viewModel = ReducingEstateValueSectionViewModel(appDetails, regDetailsSinglePerson)

      viewModel.exemptionRow.linkText shouldBe messagesApi("iht.viewOrChange")
    }

    "have the correct text when all answers to the exemptions questions are 'No'" in {
      val appDetails = CommonBuilder.buildExemptionsWithNoValues(applicationDetails)
      val viewModel = ReducingEstateValueSectionViewModel(appDetails, regDetailsSinglePerson)

      viewModel.exemptionRow.value shouldBe messagesApi("page.iht.application.estateOverview.exemptions.noExemptionsValue")
    }

    "show Start when no exemption questions have been answered" in {
      val viewModel = ReducingEstateValueSectionViewModel(applicationDetails, regDetailsMarriedPerson)

      viewModel.exemptionRow.linkText shouldBe messagesApi("iht.start")
    }

    "show Give more details when some exemption questions have been answered" in {
      val appDetails = CommonBuilder.buildSomeExemptions(applicationDetails)
      val viewModel = ReducingEstateValueSectionViewModel(appDetails, regDetailsMarriedPerson)

      viewModel.exemptionRow.linkText shouldBe messagesApi("iht.giveMoreDetails")
    }

    "have the correct URL for the exemptions link" in {
      val appDetails = CommonBuilder.buildSomeExemptions(applicationDetails)
      val viewModel = ReducingEstateValueSectionViewModel(appDetails, regDetailsMarriedPerson)
      viewModel.exemptionRow.linkUrl shouldBe iht.controllers.application.exemptions.routes.ExemptionsOverviewController.onPageLoad()
    }
    //endregion

    //region Debts
    "If there are no exemptions in the application model, there is no debt row" in {
      val viewModel = ReducingEstateValueSectionViewModel(applicationDetails, regDetailsMarriedPerson)

      viewModel.debtRow.isEmpty should be(true)
    }

    "If there are no exemptions, but there are liabilities in the application model there is no debt row" in {
      val appDetails = CommonBuilder.buildApplicationDetails copy (allLiabilities = Some(CommonBuilder.buildSomeLiabilities))
      val viewModel = ReducingEstateValueSectionViewModel(appDetails, regDetailsMarriedPerson)

      viewModel.debtRow.isDefined should be(false)

    }

    "if there are exemptions and no liabilities in the application model there is a debt row" in {
      val appDetails = CommonBuilder.buildSomeExemptions(applicationDetails)
      val viewModel = ReducingEstateValueSectionViewModel(appDetails, regDetailsMarriedPerson)

      viewModel.debtRow.isDefined should be(true)
    }

    "if there are exemptions and liabilities in the application model there is a debt row" in {
      val appDetails = CommonBuilder.buildSomeExemptions(applicationDetails) copy (allLiabilities = Some(CommonBuilder.buildSomeLiabilities))
      val viewModel = ReducingEstateValueSectionViewModel(appDetails, regDetailsMarriedPerson)

      viewModel.debtRow.isDefined should be(true)
    }

    "have an id of 'debts' for the debts row if teh debt row is present" in {
      val appDetails = CommonBuilder.buildSomeExemptions(applicationDetails) copy (allLiabilities = Some(CommonBuilder.buildSomeLiabilities))
      val viewModel = ReducingEstateValueSectionViewModel(appDetails, regDetailsMarriedPerson)

      viewModel.debtRow.get.id shouldBe EstateDebtsID
    }

    "have the correct caption for the debts row" in {
      val appDetails = CommonBuilder.buildSomeExemptions(applicationDetails) copy (allLiabilities = Some(CommonBuilder.buildSomeLiabilities))
      val viewModel = ReducingEstateValueSectionViewModel(appDetails, regDetailsMarriedPerson)

      viewModel.debtRow.get.label shouldBe messagesApi("iht.estateReport.debts.owedFromEstate")
    }

    "have a blank value for debts when there are no debts" in {
      val appDetails = CommonBuilder.buildSomeExemptions(applicationDetails)
      val viewModel = ReducingEstateValueSectionViewModel(appDetails, regDetailsMarriedPerson)

      viewModel.debtRow.get.value shouldBe ""
    }

    "have a blank value for debts when there are debts but no values have been given" in {
      val appDetails = CommonBuilder.buildSomeExemptions(applicationDetails) copy (allLiabilities = Some(buildEveryLiabilityWithNoValues))
      val viewModel = ReducingEstateValueSectionViewModel(appDetails, regDetailsMarriedPerson)

      viewModel.debtRow.get.value shouldBe ""
    }

    "have the correct value with a minus and pound sign for debts where there are some debts" in {
      val appDetails = CommonBuilder.buildSomeExemptions(applicationDetails) copy (allLiabilities = Some(CommonBuilder.buildEveryLiability))
      val viewModel = ReducingEstateValueSectionViewModel(appDetails, regDetailsMarriedPerson)

      viewModel.debtRow.get.value shouldBe "-£500.00"
    }

    "have the correct text when all answers to the debts questions are 'No'" in {
      val appDetails = applicationDetails copy (allLiabilities = Some(CommonBuilder.buildAllLiabilitiesAnsweredNo),
        allExemptions = Some(buildAllExemptionsWithAssets))
      val viewModel = ReducingEstateValueSectionViewModel(appDetails, regDetailsMarriedPerson)

      viewModel.debtRow.get.value shouldBe messagesApi("site.noDebts")
    }

    "show View or Change when all debts are completed" in {
      val appDetails = CommonBuilder.buildSomeExemptions(applicationDetails) copy
        (allLiabilities = Some(CommonBuilder.buildEveryLiability), allAssets = Some(AllAssets(properties = Some(Properties(Some(false))))))
      val viewModel = ReducingEstateValueSectionViewModel(appDetails, regDetailsMarriedPerson)

      viewModel.debtRow.get.linkText shouldBe messagesApi("iht.viewOrChange")
    }

    "show Start when no debts questions have been answered" in {
      val appDetails = CommonBuilder.buildSomeExemptions(applicationDetails)
      val viewModel = ReducingEstateValueSectionViewModel(appDetails, regDetailsMarriedPerson)

      viewModel.debtRow.get.linkText shouldBe messagesApi("iht.start")
    }

    "show Give more details when some debts questions have been answered" in {
      val appDetails = CommonBuilder.buildSomeExemptions(applicationDetails) copy (allLiabilities = Some(CommonBuilder.buildSomeLiabilities))
      val viewModel = ReducingEstateValueSectionViewModel(appDetails, regDetailsMarriedPerson)

      viewModel.debtRow.get.linkText shouldBe messagesApi("iht.giveMoreDetails")
    }

    "have the correct URL for the debts link" in {
      val appDetails = CommonBuilder.buildSomeExemptions(applicationDetails) copy (allLiabilities = Some(CommonBuilder.buildEveryLiability))
      val viewModel = ReducingEstateValueSectionViewModel(appDetails, regDetailsMarriedPerson)

      viewModel.debtRow.get.linkUrl shouldBe iht.controllers.application.debts.routes.DebtsOverviewController.onPageLoad()
    }
    //endregion

    //region Total value
    "have an id of 'total' for the total row" in {
      val viewModel = ReducingEstateValueSectionViewModel(applicationDetails, regDetailsMarriedPerson)

      viewModel.totalRow.id shouldBe "reducing-estate-totals"
    }

    "have the correct caption for the total row" in {
      val viewModel = ReducingEstateValueSectionViewModel(applicationDetails, regDetailsMarriedPerson)

      viewModel.totalRow.label shouldBe messagesApi("page.iht.application.exemptions.total")
    }

    "have a zero value for total when there are no exemptions and no liabilities" in {
      val viewModel = ReducingEstateValueSectionViewModel(applicationDetails, regDetailsMarriedPerson)

      viewModel.totalRow.value shouldBe "£0.00"
    }

    "have the debts excluded from the total when there are no exemptions" in {
      val appDetails = CommonBuilder.buildExemptionsWithNoValues(applicationDetails) copy (allLiabilities = Some(CommonBuilder.buildEveryLiability))
      val viewModel = ReducingEstateValueSectionViewModel(appDetails, regDetailsMarriedPerson)

      viewModel.totalRow.value shouldBe "£0.00"
    }

    "have the exemptions amount as total value when there are no debts" in {
      val appDetails = CommonBuilder.buildSomeExemptions(applicationDetails)
      val viewModel = ReducingEstateValueSectionViewModel(appDetails, regDetailsMarriedPerson)

      viewModel.totalRow.value shouldBe "-£44.45"
    }

    "have the sum of exemptions and debts amounts as total value when there are exemptions and debts values" in {
      val appDetails = CommonBuilder.buildSomeExemptions(applicationDetails) copy (allLiabilities = Some(CommonBuilder.buildEveryLiability))
      val viewModel = ReducingEstateValueSectionViewModel(appDetails, regDetailsMarriedPerson)

      viewModel.totalRow.value shouldBe "-£544.45"
    }

    "have true as renderAsTotalRow" in {
      val viewModel = ReducingEstateValueSectionViewModel(applicationDetails, regDetailsMarriedPerson)

      viewModel.totalRow.renderAsTotalRow shouldBe true
    }

    //endregion

    // TODO: Move these elsewhere if and when common builder is refactored
    lazy val buildAllExemptionsWithAssets = AllExemptions(
      partner = Some(PartnerExemption(None, None, None, None, None, None, Some(BigDecimal(defaultAssetValue)))),
      charity = Some(BasicExemptionElement(isSelected = None)),
      qualifyingBody = Some(BasicExemptionElement(isSelected = None))
    )
  }

  lazy val buildEveryLiabilityWithNoValues = AllLiabilities(
    funeralExpenses = Some(CommonBuilder.buildBasicEstateElementLiabilityWithNoValue),
    trust = Some(CommonBuilder.buildBasicEstateElementLiabilityWithNoValue),
    debtsOutsideUk = Some(CommonBuilder.buildBasicEstateElementLiabilityWithNoValue),
    jointlyOwned = Some(CommonBuilder.buildBasicEstateElementLiabilityWithNoValue),
    other = Some(CommonBuilder.buildBasicEstateElementLiabilityWithNoValue),
    mortgages = None
  )
}
