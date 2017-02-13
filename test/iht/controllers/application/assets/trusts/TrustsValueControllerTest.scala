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

package iht.controllers.application.assets.trusts

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.models.application.assets.HeldInTrust
import iht.testhelpers.CommonBuilder
import iht.testhelpers.MockObjectBuilder._
import play.api.i18n.{Messages, MessagesApi}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.test.Helpers._

class TrustsValueControllerTest extends ApplicationControllerTest{
  implicit val messages: Messages = app.injector.instanceOf[Messages]
  val mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]

  def trustsValueController = new TrustsValueController {
    override val authConnector = createFakeAuthConnector(isAuthorised=true)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def trustsValueControllerNotAuthorised = new TrustsValueController {
    override val authConnector = createFakeAuthConnector(isAuthorised=false)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  "TrustsValueController" must {

    "redirect to login page on PageLoad if the user is not logged in" in {
      val result = trustsValueControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "redirect to login page on Submit if the user is not logged in" in {
      val result = trustsValueController.onSubmit(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "respond with OK on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val result = trustsValueController.onPageLoad (createFakeRequest())
      status(result) shouldBe (OK)
      contentAsString(result) should include (Messages("iht.estateReport.assets.heldInTrust.valueOfTrust"))
    }

    "save the trusts value and go to held in trust overview page on submit" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = Some(CommonBuilder
        .buildAllAssets))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true,
        storeAppDetailsInCache = true)

      val filledHeldInTrustForm = trustsValueForm.fill(HeldInTrust(Some(false), Some(1000), None))
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledHeldInTrustForm.data.toSeq: _*)

      val result = trustsValueController.onSubmit(request)
      status(result) shouldBe (SEE_OTHER)
      redirectLocation(result) should be (Some(routes.TrustsOverviewController.onPageLoad().url))
    }

    "save application and go to kick out page on submit  when user enters value more than £1 M" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = Some(CommonBuilder
        .buildAllAssets))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val filledHeldInTrustForm = trustsValueForm.fill(HeldInTrust(Some(false), Some(560000000), None))
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledHeldInTrustForm.data.toSeq: _*)

      val result = trustsValueController.onSubmit (request)
      status(result) shouldBe (SEE_OTHER)
      redirectLocation(result) should be (Some(iht.controllers.application.routes.KickoutController.onPageLoad.url))
    }

    "respond with bad request when incorrect value are entered on the page" in {
     implicit val fakePostRequest = createFakeRequest().withFormUrlEncodedBody(("value", "utytyyterrrrrrrrrrrrrr"))

     createMockToGetExistingRegDetailsFromCache(mockCachingConnector)

      val result = trustsValueController.onSubmit (fakePostRequest)
      status(result) shouldBe (BAD_REQUEST)
    }
  }
}
