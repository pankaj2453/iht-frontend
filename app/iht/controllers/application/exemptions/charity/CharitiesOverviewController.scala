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

package iht.controllers.application.exemptions.charity

import javax.inject.{Inject, Singleton}

import iht.controllers.application.ApplicationController
import iht.utils.CommonHelper
import play.api.i18n.MessagesApi

import scala.concurrent.Future

@Singleton
class CharitiesOverviewController @Inject()(val messagesApi: MessagesApi) extends ApplicationController {
  def onPageLoad() = authorisedForIht {
    implicit user => implicit request => {

      withApplicationDetails {
        rd => ad => {

          val isAssetLeftToCharity: Option[Boolean] = ad.allExemptions.flatMap(_.charity).flatMap(_.isSelected)

          Future.successful(Ok(iht.views.html.application.exemption.charity.charities_overview(ad.charities,
            rd,
            CommonHelper.getOrException(isAssetLeftToCharity))))
        }
      }
    }
  }
}
