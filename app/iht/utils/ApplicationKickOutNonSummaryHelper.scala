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

package iht.utils

import iht.constants.IhtProperties
import iht.models._
import iht.models.application.ApplicationDetails
import iht.models.application.assets._
import iht.models.application.basicElements.ShareableBasicEstateElement
import iht.utils.ApplicationKickOutHelper._
import iht.utils.CommonHelper._
import iht.utils.KickOutReason._
import iht.utils.{ApplicationStatus => AppStatus, KickOutReason => KickOut}
import org.joda.time.LocalDate
import play.api.Logger
import play.api.i18n.Messages

import scala.collection.immutable.ListMap

object ApplicationKickOutNonSummaryHelper {

  /**
    * Get sequence of values for section.
    */
  def getSectionTotal(applicationSection: Option[String], applicationID: Option[String], ad: ApplicationDetails): Seq[BigDecimal] = {
    def shareableValues: Option[ShareableBasicEstateElement] => Seq[BigDecimal] =
      _.fold[Seq[BigDecimal]](Nil)(xx => Seq(getOrZero(xx.value), getOrZero(xx.shareValue)))

    def stockAndShareValues: Option[StockAndShare] => Seq[BigDecimal] =
      _.fold[Seq[BigDecimal]](Nil)(xx => Seq(getOrZero(xx.valueListed), getOrZero(xx.valueNotListed)))

    applicationSection.fold[Seq[BigDecimal]](Nil) {
      case ApplicationSectionProperties => applicationID.map(id => ad.propertyList.filter(_.id == applicationID).map(_.value.getOrElse(BigDecimal(0))).sum)
        .fold[Seq[BigDecimal]](Nil)(xx => Seq(xx))
      case ApplicationSectionAssetsMoneyDeceasedOwned => Seq(getOrZero(ad.allAssets.flatMap(_.money).flatMap(xx => xx.value)))
      case ApplicationSectionAssetsMoneyJointlyOwned => Seq(getOrZero(ad.allAssets.flatMap(_.money).flatMap(xx => xx.shareValue)))
      case ApplicationSectionAssetsHouseholdDeceasedOwned => Seq(getOrZero(ad.allAssets.flatMap(_.household).flatMap(xx => xx.value)))
      case ApplicationSectionAssetsHouseholdJointlyOwned => Seq(getOrZero(ad.allAssets.flatMap(_.household).flatMap(xx => xx.shareValue)))
      case ApplicationSectionAssetsVehiclesDeceasedOwned => Seq(getOrZero(ad.allAssets.flatMap(_.vehicles).flatMap(xx => xx.value)))
      case ApplicationSectionAssetsVehiclesJointlyOwned => Seq(getOrZero(ad.allAssets.flatMap(_.vehicles).flatMap(xx => xx.shareValue)))
      case ApplicationSectionAssetsPensionsValue => Seq(getOrZero(ad.allAssets.flatMap(_.privatePension).flatMap(xx => xx.value)))
      case ApplicationSectionAssetsStocksAndSharesListed => stockAndShareValues(ad.allAssets.flatMap(_.stockAndShare))
      case ApplicationSectionAssetsStocksAndSharesNotListed => stockAndShareValues(ad.allAssets.flatMap(_.stockAndShare))
      case ApplicationSectionAssetsInsurancePoliciesJointlyOwned => Seq(getOrZero(ad.allAssets.flatMap(_.insurancePolicy).flatMap(xx => xx.shareValue)))
      case ApplicationSectionAssetsInsurancePoliciesOwnedByDeceased => Seq(getOrZero(ad.allAssets.flatMap(_.insurancePolicy).flatMap(xx => xx.value)))
      case ApplicationSectionAssetsBusinessInterests => Seq(getOrZero(ad.allAssets.flatMap(_.businessInterest).flatMap(xx => xx.totalValue)))
      case ApplicationSectionAssetsNominatedAssets => Seq(getOrZero(ad.allAssets.flatMap(_.nominated).flatMap(xx => xx.totalValue)))
      case ApplicationSectionAssetsInTrust => Seq(getOrZero(ad.allAssets.flatMap(_.heldInTrust).flatMap(xx => xx.totalValue)))
      case ApplicationSectionAssetsForeign => Seq(getOrZero(ad.allAssets.flatMap(_.foreign).flatMap(xx => xx.totalValue)))
      case ApplicationSectionAssetsMoneyOwed => Seq(getOrZero(ad.allAssets.flatMap(_.moneyOwed).flatMap(xx => xx.value)))
      case ApplicationSectionAssetsOther => Seq(getOrZero(ad.allAssets.flatMap(_.other).flatMap(xx => xx.totalValue)))
      case ApplicationSectionGiftDetails => Seq(getOrZero(applicationID.flatMap(id => ad.giftsList.map(xx => xx.filter(_.yearId == applicationID)
        .map(_.value.fold(BigDecimal(0))(identity)).sum))))
      case _ => Nil
    }
  }

  /**
    * First paragraph of the "Next steps" section.
    */
  def nextSteps1(implicit messages:Messages) = ListMap(
    /* Assets */
    TrustsMoreThanOne -> messages("iht.estateReport.kickout.nextSteps"),
    ForeignAssetsValueMoreThanMax -> messages("iht.estateReport.kickout.nextSteps"),
    TrustValueMoreThanMax -> messages("iht.estateReport.kickout.nextSteps"),
    AnnuitiesOnInsurance -> messages("iht.estateReport.kickout.nextSteps"),
    PensionDisposedLastTwoYears -> messages("iht.estateReport.kickout.nextSteps"),
    PensionsValueMoreThanMax -> messages("iht.estateReport.kickout.nextSteps"),
    InTrustLessThanSevenYears -> messages("iht.estateReport.kickout.nextSteps"),
    SingleSectionMoreThanMax -> messages("iht.estateReport.kickout.nextSteps"),
    AssetsTotalValueMoreThanMax -> messages("page.iht.application.assets.kickout.assetsTotalValueMoreThanMax.nextSteps1"),
    InsuranceMoreThanMax -> messages("iht.estateReport.kickout.nextSteps"),
    AssetsMoneyOwed -> messages("iht.estateReport.kickout.nextSteps"),
    AssetsDeceasedMoneyOwed -> messages("iht.estateReport.kickout.nextSteps"),
    AssetsMoneyJointlyOwed -> messages("iht.estateReport.kickout.nextSteps"),
    AssetsHouseholdDeceasedOwed -> messages("iht.estateReport.kickout.nextSteps"),
    AssetsHouseholdJointlyOwed -> messages("iht.estateReport.kickout.nextSteps"),
    AssetsVehiclesDeceasedOwned -> messages("iht.estateReport.kickout.nextSteps"),
    AssetsVehiclesJointlyOwned -> messages("iht.estateReport.kickout.nextSteps"),

    /* Gifts */
    GiftsWithReservationOfBenefit -> messages("iht.estateReport.kickout.nextSteps"),
    GiftsGivenInPast -> messages("iht.estateReport.kickout.nextSteps"),
    GiftsToTrust -> messages("iht.estateReport.kickout.nextSteps"),
    GiftsMaxValue -> messages("iht.estateReport.kickout.nextSteps"),

    /* Exemptions */
    PartnerHomeInUK -> messages("iht.estateReport.kickout.nextSteps"),

    /* Pre-pre TNRB Eligibility */
    TnrbEstateMoreThanThreshold -> messages("iht.estateReport.kickout.nextSteps"),

    /* Pre-TNRB Eligibility */
    WidowedCheckNotWidowed -> messages("iht.estateReport.kickout.nextSteps"),
    PartnerDiedBeforeMinDate -> messages("iht.estateReport.kickout.nextSteps"),

    /* Pre-TNRB Eligibility */
    PartnerDiedBeforeMinDateOpc -> messages("iht.estateReport.kickout.nextSteps"),

    /* TNRB Eligibility */
    PartnerNotLivingInUk -> messages("iht.estateReport.tnrb.kickout.nextSteps"),
    GiftMadeBeforeDeath -> messages("iht.estateReport.tnrb.kickout.nextSteps"),
    StateClaimAnyBusiness -> messages("iht.estateReport.tnrb.kickout.nextSteps"),
    PartnerGiftWithResToOther -> messages("iht.estateReport.tnrb.kickout.nextSteps"),
    PartnerBenFromTrust -> messages("iht.estateReport.tnrb.kickout.nextSteps"),
    EstateBelowIhtThresholdApplied -> messages("iht.estateReport.tnrb.kickout.nextSteps"),
    JointAssetPassed -> messages("iht.estateReport.tnrb.kickout.nextSteps"),

    /* Back-end kickouts */
    ExemptionEstateValueIsMoreThanMaximum -> messages("page.iht.application.exemptions.kickout.estateMoreThanThreshold.nextSteps"),
    EstateValueIsMoreThanMaximumLimit -> messages("page.iht.application.tnrb.kickout.estateValueNotInLimit.nextSteps"),
    AssetsTotalValueMoreThanThresholdAfterExemption -> messages("iht.estateReport.kickout.nextSteps"),

    /* These ones could possibly be removed as they don't seem to be called. */
    TnrbEstateValueIsMoreThanMaximum -> messages("page.iht.application.tnrb.kickout.estateValueNotInLimit.nextSteps"),
    PremiumsPaidForOtherPersonsPolicy -> messages("page.iht.application.assets.kickout.premiumsPaidForOtherPersonsPolicy.nextSteps1"),
    NonJavaScript -> messages("iht.estateReport.tnrb.kickout.nextSteps"),
    IHTHome -> messages("page.iht.application.home.kickout.nextSteps")
  )

  /**
    * Second paragraph of the "Next steps" section.
    */
  def nextSteps2(implicit messages:Messages) = ListMap(
    /* Assets */
    TrustsMoreThanOne -> messages("iht.ifYouWantToChangeYourAnswer"),
    ForeignAssetsValueMoreThanMax -> messages("iht.ifYouWantToChangeValue"),
    TrustValueMoreThanMax -> messages("iht.ifYouWantToChangeValue"),
    AnnuitiesOnInsurance -> messages("iht.ifYouWantToChangeYourAnswer"),
    PensionDisposedLastTwoYears -> messages("iht.ifYouWantToChangeYourAnswer"),
    PensionsValueMoreThanMax -> messages("page.iht.application.assets.kickout.assetsSingleSectionMoreThanMax.nextSteps2"),
    InTrustLessThanSevenYears -> messages("iht.ifYouWantToChangeYourAnswer"),
    SingleSectionMoreThanMax -> messages("page.iht.application.assets.kickout.assetsSingleSectionMoreThanMax.nextSteps2"),
    AssetsTotalValueMoreThanMax -> messages("iht.estateReport.kickout.returnToEstateOverview"),
    InsuranceMoreThanMax -> messages("iht.ifYouWantToChangeYourAnswer"),
    AssetsMoneyOwed -> messages("page.iht.application.assets.kickout.assetsSingleSectionMoreThanMax.nextSteps2"),
    AssetsDeceasedMoneyOwed -> messages("page.iht.application.assets.kickout.assetsSingleSectionMoreThanMax.nextSteps2"),
    AssetsMoneyJointlyOwed -> messages("page.iht.application.assets.kickout.assetsSingleSectionMoreThanMax.nextSteps2"),
    AssetsHouseholdDeceasedOwed -> messages("page.iht.application.assets.kickout.assetsSingleSectionMoreThanMax.nextSteps2"),
    AssetsHouseholdJointlyOwed -> messages("page.iht.application.assets.kickout.assetsSingleSectionMoreThanMax.nextSteps2"),
    AssetsVehiclesDeceasedOwned -> messages("page.iht.application.assets.kickout.assetsSingleSectionMoreThanMax.nextSteps2"),
    AssetsVehiclesJointlyOwned -> messages("page.iht.application.assets.kickout.assetsSingleSectionMoreThanMax.nextSteps2"),

    /* Exemptions */
    PartnerHomeInUK -> messages("iht.ifYouWantToChangeYourAnswer"),

    /* Gifts */
    GiftsWithReservationOfBenefit -> messages("iht.ifYouWantToChangeYourAnswer"),
    GiftsGivenInPast -> messages("iht.ifYouWantToChangeYourAnswer"),
    GiftsToTrust -> messages("iht.ifYouWantToChangeYourAnswer"),
    GiftsMaxValue -> messages("page.iht.application.gifts.kickout.maxValue.nextSteps2"),

    /* Pre-pre TNRB Eligibility */
    TnrbEstateMoreThanThreshold -> messages("iht.estateReport.kickout.returnToEstateOverview"),

    /* Pre-TNRB Eligibility */
    WidowedCheckNotWidowed -> messages("iht.estateReport.kickout.returnToEstateOverview"),
    PartnerDiedBeforeMinDate -> messages("iht.estateReport.kickout.returnToEstateOverview"),

    /* Pre-TNRB Eligibility */
    PartnerDiedBeforeMinDateOpc -> messages("page.iht.application.tnrb.kickout.partnerDiedBeforeMinDateOPC.nextSteps2"),

    /* TNRB Eligibility */
    PartnerNotLivingInUk -> messages("iht.ifYouWantToChangeYourAnswer"),
    GiftMadeBeforeDeath -> messages("iht.ifYouWantToChangeYourAnswer"),
    StateClaimAnyBusiness -> messages("iht.ifYouWantToChangeYourAnswer"),
    PartnerGiftWithResToOther -> messages("iht.ifYouWantToChangeYourAnswer"),
    PartnerBenFromTrust -> messages("iht.ifYouWantToChangeYourAnswer"),
    EstateBelowIhtThresholdApplied -> messages("iht.ifYouWantToChangeYourAnswer"),
    JointAssetPassed -> messages("iht.ifYouWantToChangeYourAnswer"),

    /*Backend */
    AssetsTotalValueMoreThanThresholdAfterExemption -> messages("iht.estateReport.kickout.returnToEstateOverview")
  )

  /**
    * Return Link texts for Second paragraph of the "Next steps" section.
    */
  def nextSteps2ReturnLinkText(implicit messages:Messages) = ListMap(
    /* Assets */
    TrustsMoreThanOne -> messages("iht.estateReport.assets.trusts.kickout.returnToHeldInTrust.linkText"),
    ForeignAssetsValueMoreThanMax -> messages("iht.estateReport.assets.kickOut.foreignAssetsValueMoreThanMax.returnLinkText"),
    TrustValueMoreThanMax -> messages("iht.estateReport.assets.trusts.kickout.returnToHeldInTrust.linkText"),
    AnnuitiesOnInsurance -> messages("iht.estateReport.assets.insurancePolicies.kickout.returnToInsurancePolicies.linkText"),
    PensionDisposedLastTwoYears -> messages("iht.estateReport.assets.kickout.pensionDisposedLastTwoYears.returnLinkText"),
    PensionsValueMoreThanMax -> messages("iht.estateReport.assets.kickout.assetsSingleSectionMoreThanMax.returnLinkText"),
    InTrustLessThanSevenYears -> messages("iht.estateReport.assets.insurancePolicies.kickout.returnToInsurancePolicies.linkText"),
    SingleSectionMoreThanMax -> messages("iht.estateReport.assets.kickout.assetsSingleSectionMoreThanMax.returnLinkText"),
    AssetsTotalValueMoreThanMax -> messages("iht.estateReport.kickout.returnToEstateOverview.linkText"),
    InsuranceMoreThanMax -> messages("iht.estateReport.assets.insurancePolicies.kickout.returnToInsurancePolicies.linkText"),
    AssetsMoneyOwed -> messages("iht.estateReport.assets.kickout.assetsSingleSectionMoreThanMax.returnLinkText"),
    AssetsDeceasedMoneyOwed -> messages("iht.estateReport.assets.kickout.assetsSingleSectionMoreThanMax.returnLinkText"),
    AssetsMoneyJointlyOwed -> messages("iht.estateReport.assets.kickout.assetsSingleSectionMoreThanMax.returnLinkText"),
    AssetsHouseholdDeceasedOwed -> messages("iht.estateReport.assets.kickout.assetsSingleSectionMoreThanMax.returnLinkText"),
    AssetsHouseholdJointlyOwed -> messages("iht.estateReport.assets.kickout.assetsSingleSectionMoreThanMax.returnLinkText"),
    AssetsVehiclesDeceasedOwned -> messages("iht.estateReport.assets.kickout.assetsSingleSectionMoreThanMax.returnLinkText"),
    AssetsVehiclesJointlyOwned -> messages("iht.estateReport.assets.kickout.assetsSingleSectionMoreThanMax.returnLinkText"),

    /* Exemptions */
    PartnerHomeInUK -> messages("iht.estateReport.exemptions.kickout.homeNotInUK.returnLinkText"),

    /* Gifts */
    GiftsWithReservationOfBenefit -> messages("iht.estateReport.gifts.kickout.withReservationOfBenefit.returnLinkText"),
    GiftsGivenInPast -> messages("iht.estateReport.gifts.kickout.givenInPreviousYears.returnToGiftsGivenAway.linkText"),
    GiftsToTrust -> messages("iht.estateReport.gifts.kickout.givenInPreviousYears.returnToGiftsGivenAway.linkText"),
    GiftsMaxValue -> messages("iht.estateReport.gifts.kickout.maxValue.returnLinkText"),

    /* Pre-pre TNRB Eligibility */
    TnrbEstateMoreThanThreshold -> messages("iht.estateReport.kickout.returnToEstateOverview.linkText"),

    /* Pre-TNRB Eligibility */
    WidowedCheckNotWidowed -> messages("iht.estateReport.kickout.returnToEstateOverview.linkText"),
    PartnerDiedBeforeMinDate -> messages("iht.estateReport.kickout.returnToEstateOverview.linkText"),

    /* Pre-TNRB Eligibility */
    PartnerDiedBeforeMinDateOpc -> messages("iht.estateReport.tnrb.kickout.partnerDiedBeforeMinDateOPC.returnLinkText"),

    /* TNRB Eligibility */
    PartnerNotLivingInUk -> messages("iht.estateReport.tnrb.kickout.returnToIHTThreshold.linkText"),
    GiftMadeBeforeDeath -> messages("iht.estateReport.tnrb.kickout.returnToIHTThreshold.linkText"),
    StateClaimAnyBusiness -> messages("iht.estateReport.tnrb.kickout.returnToIHTThreshold.linkText"),
    PartnerGiftWithResToOther -> messages("iht.estateReport.tnrb.kickout.returnToIHTThreshold.linkText"),
    PartnerBenFromTrust -> messages("iht.estateReport.tnrb.kickout.returnToIHTThreshold.linkText"),
    EstateBelowIhtThresholdApplied -> messages("iht.estateReport.tnrb.kickout.returnToIHTThreshold.linkText"),
    JointAssetPassed -> messages("iht.estateReport.tnrb.kickout.returnToIHTThreshold.linkText"),

    /*Backend */
    AssetsTotalValueMoreThanThresholdAfterExemption -> messages("iht.estateReport.kickout.returnToEstateOverview.linkText")
  )

  private lazy val checksAllSectionsMaxValue: FunctionListMap = ListMap(
    AssetsTotalValueMoreThanMax -> { (registrationDetails, applicationDetails, sectionTotal) =>
      (applicationDetails.totalAssetsValue + CommonHelper.getOrZero(applicationDetails.totalPastYearsGiftsOption)) > IhtProperties.validationTotalAssetMaxValue
    })

  private lazy val checksActiveSectionOnlyMaxValue: FunctionListMap = ListMap(
    SingleSectionMoreThanMax -> { (registrationDetails, applicationDetails, sectionTotal) =>
      sectionTotal.exists(_ > IhtProperties.validationTotalAssetMaxValue)
    }
  )

  /**
    * The kickout logic for those kickouts belonging to the estate sections of an application, i.e. assets, gifts and
    * exemptions.
    */
  lazy val checksEstate: FunctionListMap = ListMap(
    PensionDisposedLastTwoYears -> { (registrationDetails, applicationDetails, sectionTotal) =>
      applicationDetails.allAssets.flatMap(_.privatePension.flatMap(_.isChanged)).fold(false)(_.booleanValue)
    },
    PensionsValueMoreThanMax -> { (registrationDetails, applicationDetails, sectionTotal) =>
      applicationDetails.allAssets.flatMap(_.privatePension.
        flatMap(_.value)).fold(BigDecimal(0))(identity) > IhtProperties.validationTotalAssetMaxValue
    },
    AnnuitiesOnInsurance -> { (registrationDetails, applicationDetails, sectionTotal) =>
      applicationDetails.allAssets.flatMap(_.insurancePolicy
        .flatMap(_.isAnnuitiesBought)).fold(false)(_.booleanValue)
    },
    InTrustLessThanSevenYears -> { (registrationDetails, applicationDetails, sectionTotal) =>
      applicationDetails.allAssets.flatMap(_.insurancePolicy.flatMap(_.isInTrust)).fold(false)(_.booleanValue) &&
        applicationDetails.allAssets.flatMap(_.insurancePolicy.flatMap(_.isInsurancePremiumsPayedForSomeoneElse))
          .fold(false)(_.booleanValue)
    },
    TrustsMoreThanOne -> { (registrationDetails, applicationDetails, sectionTotal) =>
      applicationDetails.allAssets.flatMap(_.heldInTrust.flatMap(_.isMoreThanOne.map(_.booleanValue)))
        .fold(false)(_.booleanValue)
    },
    ForeignAssetsValueMoreThanMax -> { (registrationDetails, applicationDetails, sectionTotal) =>
      applicationDetails.allAssets.flatMap(_.foreign
        .flatMap(_.value)).fold(BigDecimal(0))(identity) > IhtProperties.validationForeignAssetMaxValue
    },
    AssetsMoneyOwed -> { (registrationDetails, applicationDetails, sectionTotal) =>
      applicationDetails.allAssets.flatMap(_.moneyOwed
        .flatMap(_.value)).fold(BigDecimal(0))(identity) > IhtProperties.validationTotalAssetMaxValue
    },
    AssetsDeceasedMoneyOwed -> { (registrationDetails, applicationDetails, sectionTotal) =>
      applicationDetails.allAssets.flatMap(_.money
        .flatMap(_.value)).fold(BigDecimal(0))(identity) > IhtProperties.validationTotalAssetMaxValue
    },
    AssetsMoneyJointlyOwed -> { (registrationDetails, applicationDetails, sectionTotal) =>
      applicationDetails.allAssets.flatMap(_.money
        .flatMap(_.shareValue)).fold(BigDecimal(0))(identity) > IhtProperties.validationTotalAssetMaxValue
    },
    AssetsHouseholdDeceasedOwed -> { (registrationDetails, applicationDetails, sectionTotal) =>
      applicationDetails.allAssets.flatMap(_.household
        .flatMap(_.value)).fold(BigDecimal(0))(identity) > IhtProperties.validationTotalAssetMaxValue
    },
    AssetsHouseholdJointlyOwed -> { (registrationDetails, applicationDetails, sectionTotal) =>
      applicationDetails.allAssets.flatMap(_.household
        .flatMap(_.shareValue)).fold(BigDecimal(0))(identity) > IhtProperties.validationTotalAssetMaxValue
    },
    AssetsVehiclesDeceasedOwned -> { (registrationDetails, applicationDetails, sectionTotal) =>
      applicationDetails.allAssets.flatMap(_.vehicles
        .flatMap(_.value)).fold(BigDecimal(0))(identity) > IhtProperties.validationTotalAssetMaxValue
    },
    AssetsVehiclesJointlyOwned -> { (registrationDetails, applicationDetails, sectionTotal) =>
      applicationDetails.allAssets.flatMap(_.vehicles
        .flatMap(_.shareValue)).fold(BigDecimal(0))(identity) > IhtProperties.validationTotalAssetMaxValue
    },
    TrustValueMoreThanMax -> { (registrationDetails, applicationDetails, sectionTotal) =>
      applicationDetails.allAssets.flatMap(_.heldInTrust
        .flatMap(_.value)).fold(BigDecimal(0))(identity) > IhtProperties.validationTrustMaxValue
    },
    InsuranceMoreThanMax -> { (registrationDetails, applicationDetails, sectionTotal) =>
      applicationDetails.allAssets.flatMap(_.insurancePolicy.flatMap(_.moreThanMaxValue)).fold(false)(identity)
    },
    GiftsWithReservationOfBenefit -> { (registrationDetails, applicationDetails, sectionTotal) =>
      applicationDetails.allGifts.flatMap(_.isReservation).fold(false)(_.booleanValue)
    },
    GiftsGivenInPast -> { (registrationDetails, applicationDetails, sectionTotal) =>
      applicationDetails.allGifts.flatMap(_.isGivenInLast7Years).fold(false)(_.booleanValue)
    },
    GiftsToTrust -> { (registrationDetails, applicationDetails, sectionTotal) =>
      applicationDetails.allGifts.flatMap(_.isToTrust).fold(false)(_.booleanValue)
    },
    GiftsMaxValue -> { (registrationDetails, applicationDetails, sectionTotal) =>
      applicationDetails.totalPastYearsGiftsValueExcludingExemptions > IhtProperties.giftsMaxValue
    },
    PartnerHomeInUK -> { (registrationDetails, applicationDetails, sectionTotal) =>
      !applicationDetails.allExemptions.flatMap(_.partner.flatMap(_.isPartnerHomeInUK)).fold(true)(identity)
    }
  )

  /**
    * The kickout logic for those kickouts belonging to the widow check section of an application.
    */
  lazy val checksWidow: FunctionListMap = ListMap(
    WidowedCheckNotWidowed -> { (registrationDetails, applicationDetails, sectionTotal) =>
      !applicationDetails.widowCheck.flatMap(_.widowed).fold(true)(identity)
    },
    PartnerDiedBeforeMinDate -> { (registrationDetails, applicationDetails, sectionTotal) => {
      def preDeceasedDiedEligible(x: LocalDate) =
        x.isAfter(IhtProperties.dateOfPredeceasedForTnrbEligibility) ||
          x.isEqual(IhtProperties.dateOfPredeceasedForTnrbEligibility)

      applicationDetails.widowCheck.flatMap(_.dateOfPreDeceased).fold(false) {
        dateOfPreDeceased => !preDeceasedDiedEligible(dateOfPreDeceased)
      }
    }
    }
  )

  /**
    * The kickout logic for those kickouts belonging to the widow check section of an application.
    */
  lazy val checksWidowOpc: FunctionListMap = ListMap(
    PartnerDiedBeforeMinDateOpc -> { (registrationDetails, applicationDetails, sectionTotal) => {
      def preDeceasedDiedEligible(x: LocalDate) =
        x.isAfter(IhtProperties.dateOfPredeceasedForTnrbEligibility) ||
          x.isEqual(IhtProperties.dateOfPredeceasedForTnrbEligibility)

      applicationDetails.widowCheck.flatMap(_.dateOfPreDeceased).fold(false) {
        dateOfPreDeceased => !preDeceasedDiedEligible(dateOfPreDeceased)
      }
    }
    }
  )

  /**
    * The kickout logic for those kickouts belonging to the Tnrb section of an application.
    */
  lazy val checksBackend: FunctionListMap = ListMap(
    TnrbEstateMoreThanThreshold -> { (registrationDetails, applicationDetails, sectionTotal) =>
      applicationDetails.netValueAfterExemptionAndDebtsForPositiveExemption >
        2 * IhtProperties.tnrbThresholdLimit
    },
    AssetsTotalValueMoreThanThresholdAfterExemption -> { (registrationDetails, applicationDetails, sectionTotal) =>
      applicationDetails.netValueAfterExemptionAndDebtsForPositiveExemption >
        IhtProperties.exemptionsThresholdValue && applicationDetails.increaseIhtThreshold.isEmpty &&
        RegistrationDetailsHelper.isExemptionsCompleted(registrationDetails, applicationDetails)
    }
  )

  /**
    * The kickout logic for those kickouts belonging to the Tnrb eligibility section of an application.
    */
  lazy val checksTnrbEligibility: FunctionListMap = ListMap(
    PartnerNotLivingInUk -> { (registrationDetails, applicationDetails, sectionTotal) =>
      !applicationDetails.increaseIhtThreshold.flatMap(_.isPartnerLivingInUk).fold(true)(identity)
    },
    GiftMadeBeforeDeath -> { (registrationDetails, applicationDetails, sectionTotal) =>
      applicationDetails.increaseIhtThreshold.flatMap(_.isGiftMadeBeforeDeath).fold(false)(identity)
    },
    StateClaimAnyBusiness -> { (registrationDetails, applicationDetails, sectionTotal) =>
      applicationDetails.increaseIhtThreshold.flatMap(_.isStateClaimAnyBusiness).fold(false)(identity)
    },
    PartnerGiftWithResToOther -> { (registrationDetails, applicationDetails, sectionTotal) =>
      applicationDetails.increaseIhtThreshold.flatMap(_.isPartnerGiftWithResToOther).fold(false)(identity)
    },
    PartnerBenFromTrust -> { (registrationDetails, applicationDetails, sectionTotal) =>
      applicationDetails.increaseIhtThreshold.flatMap(_.isPartnerBenFromTrust).fold(false)(identity)
    },
    EstateBelowIhtThresholdApplied -> { (registrationDetails, applicationDetails, sectionTotal) =>
      applicationDetails.increaseIhtThreshold.flatMap(_.isEstateBelowIhtThresholdApplied).fold(false)(!_)
    },
    JointAssetPassed -> { (registrationDetails, applicationDetails, sectionTotal) =>
      !applicationDetails.increaseIhtThreshold.flatMap(_.isJointAssetPassed).fold(true)(identity)
    }
  )

  lazy val checksWidowAndTnrbEligibility = checksWidow ++ checksWidowOpc ++ checksTnrbEligibility

  /**
    * Checks for the specified kickouts, prioritizing the application section if specified, and returns the kickout reason
    * for the first found. The registrationDetails, applicationDetails and sectionTotal objects are passed into each
    * kickout function to assist evaluation.
    */
  def check(checks: FunctionListMap = checksEstate,
            prioritySection: Option[String],
            registrationDetails: RegistrationDetails,
            applicationDetails: ApplicationDetails,
            sectionTotal: Seq[BigDecimal]): Option[String] = {

    def getSection(kickout: String): Option[String] = sections.find(_._1 == kickout).map(_._2)

    def getChecks: FunctionListMap = {
      val lsc = prioritySection.flatMap { lsc => if (lsc.isEmpty) None else Some(lsc) }

      // Divide the checks into separate groups then filter and re-combine them in the correct order.
      val checksActiveSectionOnly = lsc.fold[FunctionListMap](emptyFunctionListMap)(ss =>
        checks.filter(check => getSection(check._1) == prioritySection))

      val checksNonActiveSectionsOnly = lsc.fold[FunctionListMap](checks)(ss =>
        checks.filter(check => getSection(check._1) != prioritySection))

      lsc.map[FunctionListMap](_ => checksActiveSectionOnly ++ checksActiveSectionOnlyMaxValue)
        .fold(emptyFunctionListMap)(identity) ++ checksAllSectionsMaxValue ++ checksNonActiveSectionsOnly
    }

    val kickoutReason = RegistrationDetailsHelper.findFirstTrue(registrationDetails, applicationDetails, sectionTotal, getChecks)
    Logger.debug("Kickout check returns: " + kickoutReason)
    kickoutReason
  }

  /**
    * Checks for application kickouts, prioritizing the application section if specified, and using the
    * (currently either property or gift) ID, if specified, to retrieve a section-specific value.
    * An ApplicationDetails object is then returned, with the kickout reason and status updated.
    * Priority section is irrelevant where no section-related prioritization is necessary for the kickouts to be run.
    * ID for section total is relevant only for kickouts which are to be applied to a specific section for a specific
    * ID, currently only for the kickout logic stored in checksActiveSectionOnlyMaxValue, above.
    */
  def updateKickout(checks: FunctionListMap = ApplicationKickOutNonSummaryHelper.checksEstate,
                    prioritySection: Option[String] = None,
                    registrationDetails: RegistrationDetails,
                    applicationDetails: ApplicationDetails,
                    idForSectionTotal: Option[String] = None): ApplicationDetails = {
    val kickoutReason = ApplicationKickOutNonSummaryHelper.check(checks = checks,
      prioritySection = prioritySection,
      registrationDetails = registrationDetails,
      applicationDetails = applicationDetails,
      sectionTotal = ApplicationKickOutNonSummaryHelper.getSectionTotal(prioritySection, idForSectionTotal, applicationDetails))

    val status = kickoutReason.fold(AppStatus.InProgress)(_ => AppStatus.KickOut)
    applicationDetails copy(status = status, kickoutReason = kickoutReason)
  }
}
