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

package iht.views.filter

import iht.constants.IhtProperties
import iht.views.ViewTestHelper
import iht.views.html.filter.agent_view
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
/**
  * Created by adwelly on 21/10/2016.
  */
class AgentViewTest extends ViewTestHelper {

  val fakeRequest = createFakeRequest(isAuthorised = false)

  "FilterView" must {

    "have no message keys in html" in {
      val result = agent_view()(fakeRequest, applicationMessages)
      val view = asDocument(contentAsString(result)).toString
      noMessageKeysShouldBePresent(view)
    }

    "generate appropriate content for the title" in {
      val result = agent_view()(fakeRequest, applicationMessages)
      val doc = asDocument(contentAsString(result))
      val titleElement = doc.getElementsByTag("h1").first

      titleElement.text should include(messagesApi("iht.noChangeToHowReportToHMRC"))
    }

    "generate appropriate content for the browser title" in {
      val result = agent_view()(fakeRequest, applicationMessages)
      val doc = asDocument(contentAsString(result))
      val browserTitleElement = doc.getElementsByTag("title").first

      browserTitleElement.text should include(messagesApi("iht.noChangeToHowReportToHMRC"))
    }

    "generate content text informing the agent that there is no change" in {
      val result = agent_view()(fakeRequest, applicationMessages)
      val doc = asDocument(contentAsString(result))
      val contentPara = doc.getElementById("agent-content")

      contentPara.text should be(messagesApi("page.iht.filter.agent.content"))
    }

    "contain a link with the button class with the text 'Exit to GOV.UK'" in {
      val result = agent_view()(fakeRequest, applicationMessages)
      val doc = asDocument(contentAsString(result))
      val button = doc.select("a.button").first

      button.text() should be(messagesApi("iht.exitToGovUK"))
    }

    "contain a link with a button class with the correct exit link" in {
      val result = agent_view()(fakeRequest, applicationMessages)
      val doc = asDocument(contentAsString(result))
      val button = doc.select("a.button").first

      button.attr("href") should be(IhtProperties.linkExitToGovUKIHTForms)
    }

    "contain a link with id 'back' with the text 'Back'" in {
      val result = agent_view()(fakeRequest, applicationMessages)
      val doc = asDocument(contentAsString(result))
      val button = doc.getElementById("back")

      button.text() should be(messagesApi("iht.back"))
    }


    "contain a link with id 'back' with the href that points to the main filter page" in {
      val result = agent_view()(fakeRequest, applicationMessages)
      val doc = asDocument(contentAsString(result))
      val button = doc.getElementById("back")

      button.attr("href") should be(iht.controllers.filter.routes.FilterController.onPageLoad().url)
    }

  }
}
