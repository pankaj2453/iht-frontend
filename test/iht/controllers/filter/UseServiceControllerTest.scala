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

package iht.controllers.filter

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.views.HtmlSpec
import play.api.i18n.{Messages, MessagesApi}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.test.Helpers._

/**
  * Created by adwelly on 25/10/2016.
  */
class UseServiceControllerTest extends ApplicationControllerTest with HtmlSpec {

  override implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  val mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]

  def controller = new UseServiceController {
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  "UseServiceController" must {
    "show the 'no change' page when accessed by an unauthorized person" in {
      val result = controller.onPageLoadUnder()(createFakeRequest(isAuthorised = false))
      status(result) should be(OK)

      val doc = asDocument(contentAsString(result))
      val titleElement = doc.getElementsByTag("h1").first
      titleElement.text() should be(messagesApi("iht.shouldUseOnlineService"))
    }

    "show paragraph 0 for the under 325000 estimate" in {
      val result = controller.onPageLoadUnder()(createFakeRequest(isAuthorised = false))
      status(result) should be(OK)

      val doc = asDocument(contentAsString(result))
      val paragraph0 = doc.getElementById("paragraph0")
      paragraph0.text() should be(messagesApi("page.iht.filter.useService.under325000.paragraph0"))
    }

    "show paragraph 0 for the between estimate" in {
      val result = controller.onPageLoadOver()(createFakeRequest(isAuthorised = false))
      status(result) should be(OK)

      val doc = asDocument(contentAsString(result))
      val paragraph0 = doc.getElementById("paragraph0")
      paragraph0.text() should be(messagesApi("page.iht.filter.useService.between325000And1Million.paragraph0"))
    }
  }
}
