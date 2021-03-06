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

import iht.connector.IhtConnectors
import iht.controllers.application.EstateController
import iht.metrics.Metrics
import iht.models.application.ApplicationDetails
import iht.models.application.assets.HeldInTrust
import iht.utils.{ApplicationKickOutHelper, CommonHelper, StringHelper}
import play.api.Play.current
import play.api.i18n.Messages.Implicits._

/**
  * Created by jennygj on 30/06/16.
  */

object TrustsOverviewController extends TrustsOverviewController with IhtConnectors {
  def metrics: Metrics = Metrics
}

trait TrustsOverviewController extends EstateController {
  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionAssetsInTrust)

  def onPageLoad = authorisedForIht {
    implicit user =>
      implicit request => {
        withRegistrationDetails { registrationDetails =>
          for {
            applicationDetails: Option[ApplicationDetails] <- ihtConnector.getApplication(
              StringHelper.getNino(user),
              CommonHelper.getOrExceptionNoIHTRef(registrationDetails.ihtReference),
              registrationDetails.acknowledgmentReference
            )
            trusts: Option[HeldInTrust] = applicationDetails.flatMap(_.allAssets.flatMap(_.heldInTrust))
          } yield {
            Ok(iht.views.html.application.asset.trusts.trusts_overview(trusts, registrationDetails))
          }
        }
      }
  }
}
