var selenium = require('selenium-webdriver');
var AxeBuilder = require('axe-webdriverjs');
var By = selenium.By, until = selenium.until;
var colors = require('colors');
var TestReporter = require('../../../../spec-helpers/reporter');
var Browser = require('../../../../spec-helpers/browser');
var accessibilityhelper = require('../../../../spec-helpers/check-accessibility-helper');
var loginhelper = require('../../../../spec-helpers/login-helper');
var actionHelper = require('../../../../spec-helpers/action-helper');
var behaves = require('../../../../spec-helpers/behaviour');
var Reporter = new TestReporter();

jasmine.DEFAULT_TIMEOUT_INTERVAL = 60000;
jasmine.getEnv().clearReporters();
jasmine.getEnv().addReporter(Reporter.reporter);


describe('Money (Assets) accessibility : ', function() {
    var driver;

    beforeEach(function(done) {
      driver = Browser.startBrowser();

      loginhelper.authenticate(done, driver, 'report')
    });

    afterEach(function(done) {
      driver.quit().then(function () {
          done();
      });
    });


    function fillMoneyOwned(done, driver){
        driver.get(Browser.baseUrl + '/estate-report/own-money-owned')
        driver.findElement(By.css('#yes-label')).click();
        driver.findElement(By.name("value")).sendKeys('5000');
        actionHelper.submitPageHelper(done, driver);
    }
    function fillMoneyJointlyOwned(done, driver){
        driver.get(Browser.baseUrl + '/estate-report/money-jointly-owned')
        driver.findElement(By.css('#yes-label')).click();
        driver.findElement(By.name("shareValue")).sendKeys('8000');
        actionHelper.submitPageHelper(done, driver);
    }


    it('money overview', function (done) {
         behaves.actsAsBasicPage(done, driver, {
             url: Browser.baseUrl + '/estate-report/money-owned',
             pageTitle: "Money"
         })
    });

    it('money overview, filled', function (done) {
        fillMoneyOwned(done, driver);
        fillMoneyJointlyOwned(done, driver);

        behaves.actsAsBasicPage(done, driver, {
            url: Browser.baseUrl + '/estate-report/money-owned',
            pageTitle: "Money"
        })
    });

    it('money owned by deceased', function (done) {
        behaves.actsAsYesNoWithValue(done, driver, {
            url: Browser.baseUrl + '/estate-report/own-money-owned',
            pageTitle: "Own money owned"
        })
    });

    it('money owned jointly', function (done) {
        behaves.actsAsYesNoWithValue(done, driver, {
            url: Browser.baseUrl + '/estate-report/money-jointly-owned',
            pageTitle: "Joint money owned"
        })
    });


});
