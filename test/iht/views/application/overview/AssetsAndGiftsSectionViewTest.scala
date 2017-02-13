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

package iht.views.application.overview

import iht.viewmodels.application.overview.{AssetsAndGiftsSectionViewModel, NotStarted, OverviewRow, OverviewRowWithoutLink}
import iht.views.HtmlSpec
import iht.views.html.application.overview.assets_and_gifts_section
import iht.{FakeIhtApp, TestUtils}
import org.jsoup.select.Elements
import org.scalatest.BeforeAndAfter
import org.scalatest.mock.MockitoSugar
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.mvc.Call
import uk.gov.hmrc.play.test.UnitSpec

class AssetsAndGiftsSectionViewTest extends UnitSpec with FakeIhtApp with MockitoSugar with TestUtils with HtmlSpec with BeforeAndAfter {
  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  def dummyOverviewRow = OverviewRow("", "", "", NotStarted, Call("", ""), "")
  def dummyTotalRow = OverviewRowWithoutLink("", "", "", "")

  val dummyAssetsAndGiftsSection = AssetsAndGiftsSectionViewModel(
    behaveAsIncreasingTheEstateSection = false,
    assetRow = dummyOverviewRow,
    giftRow = dummyOverviewRow,
    totalRow = dummyTotalRow
  )

  "assets and gifts section" must {

    "show the correct title when asked to" in {
      implicit val request = createFakeRequest()
      val viewModel: AssetsAndGiftsSectionViewModel = dummyAssetsAndGiftsSection copy (behaveAsIncreasingTheEstateSection = true)

      val view = assets_and_gifts_section(viewModel).toString
      val doc = asDocument(view)
      val header = doc.getElementsByTag("h2")
      header.text() should include(Messages("page.iht.application.estateOverview.totalAddedToTheEstateValue"))
    }

    "not show a title when asked not to" in {
      implicit val request = createFakeRequest()
      val viewModel = dummyAssetsAndGiftsSection copy (behaveAsIncreasingTheEstateSection = false)
      val view = assets_and_gifts_section(viewModel).toString
      val doc = asDocument(view)
      doc.getElementsByTag("h2").first.classNames contains "visually-hidden"
    }

    "contain the Assets row" in {
      implicit val request = createFakeRequest()

      val viewModel = dummyAssetsAndGiftsSection copy (
        assetRow = OverviewRow(id = "assets", label = Messages("iht.estateReport.assets.inEstate"),
          completionStatus = NotStarted,
          value = "",
          linkUrl = Call("Get","localhost"),
          qualifyingText = ""))

      val view = assets_and_gifts_section(viewModel).toString
      val doc = asDocument(view)
      view should include(Messages("iht.estateReport.assets.inEstate"))
      assertRenderedById(doc, "assets-row")
    }

    "contain the Gifts row" in {
      implicit val request = createFakeRequest()

      val viewModel = dummyAssetsAndGiftsSection copy (
        giftRow = OverviewRow(id = "gifts", label = Messages("iht.estateReport.gifts.givenAway.title"),
          completionStatus = NotStarted,
          value = "",
          linkUrl = Call("Get","localhost"),
          qualifyingText = "")
        )

      val view = assets_and_gifts_section(viewModel).toString
      val doc = asDocument(view)
      view should include(Messages("iht.estateReport.gifts.givenAway.title"))
      assertRenderedById(doc, "gifts-row")
    }

    "contain a Total row" in {
      implicit val request = createFakeRequest()

      val viewModel = dummyAssetsAndGiftsSection copy (
        totalRow = OverviewRowWithoutLink(id = "assetsGiftsTotal",
          label = "",
          value = "£1,234.56",
          qualifyingText = "")
        )

      val view = assets_and_gifts_section(viewModel).toString()
      view should include(Messages("£1,234.56"))
    }
  }
}
