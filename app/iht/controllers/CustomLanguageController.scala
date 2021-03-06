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

package iht.controllers

import javax.inject.{Inject, _}

import iht.config.ApplicationConfig
import play.api.Play
import play.api.i18n.{Lang, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call}
import uk.gov.hmrc.play.config.RunMode
import uk.gov.hmrc.play.language.{LanguageController, LanguageUtils}


@Singleton
class CustomLanguageController @Inject()(implicit val messagesApi: MessagesApi) extends LanguageController with RunMode {
  /** Converts a string to a URL, using the route to this controller. **/
  val englishLang = Lang("en")

  def langToCall(lang: String): Call = {
    if(ApplicationConfig.isWelshEnabled) {
      iht.controllers.routes.CustomLanguageController.switchToLanguage(lang)
    } else {
      iht.controllers.routes.CustomLanguageController.switchToLanguage("english")
    }
  }

  override def switchToLanguage(language: String): Action[AnyContent] =  Action { implicit request =>
    val lang =
      if(ApplicationConfig.isWelshEnabled) {
        languageMap.getOrElse(language, LanguageUtils.getCurrentLang)
      } else {
        englishLang
      }
    val redirectURL = request.headers.get(REFERER).getOrElse(fallbackURL)

    Redirect(redirectURL).withLang(Lang.apply(lang.code)).flashing(LanguageUtils.FlashWithSwitchIndicator)
  }

  /** Provides a fallback URL if there is no referer in the request header. **/
  override protected def fallbackURL: String = Play.current.configuration.getString(s"$env.language.fallbackUrl").getOrElse("/")

  /** Returns a mapping between strings and the corresponding Lang object. **/
  override def languageMap: Map[String, Lang] = Map("english" -> Lang("en"),
    "cymraeg" -> Lang("cy"))
}
