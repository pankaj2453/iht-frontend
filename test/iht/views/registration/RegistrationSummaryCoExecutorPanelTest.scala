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

package iht.views.registration

import iht.controllers.registration.executor.routes._
import iht.views.html.registration.registration_summary_coexecutor_panel
import iht.{FakeIhtApp, TestUtils}
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class RegistrationSummaryCoExecutorPanelTest extends UnitSpec with FakeIhtApp with MockitoSugar with TestUtils {

  "RegistrationSummaryCoExecutorPanelTest" must {
    implicit val request = createFakeRequest()
    "link to the others applying for probate change" in {
      registration_summary_coexecutor_panel(Seq()).toString should include (OthersApplyingForProbateController.onPageLoadFromOverview().url)
    }
  }
}
