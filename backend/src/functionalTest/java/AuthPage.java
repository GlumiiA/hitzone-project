import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;

public class AuthPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    // Локаторы элементов с явным ожиданием
    private final By loginTab = By.xpath("//button[contains(., 'Вход')]");
    private final By registerTab = By.xpath("//button[contains(., 'Регистрация')]");
    private final By loginUsername = By.id("login-username");
    private final By loginPassword = By.id("login-password");
    private final By registerUsername = By.id("register-username");
    private final By registerPassword = By.id("register-password");
    private final By loginSubmit = By.cssSelector("form button[type='submit']");
    private final By registerSubmit = By.cssSelector("form button[type='submit']");
    private final By loginError = By.cssSelector(".login-form .error-message");
    private final By registerError = By.cssSelector(".register-form .error-message");
    private final By registerSuccess = By.cssSelector(".register-form .success-message");

    public AuthPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public AuthPage open() {
        driver.get("http://localhost:8081/auth");
        waitForPageLoad();
        return this;
    }

    // Форма входа (Fluent Interface)
    public AuthPage switchToLoginTab() {
        clickWithWait(loginTab);
        return this;
    }

    public AuthPage enterLoginCredentials(String username, String password) {
        enterText(loginUsername, username);
        enterText(loginPassword, password);
        return this;
    }

    public AuthPage submitLogin() {
        clickWithWait(loginSubmit);
        return this;
    }

    // Форма регистрации (Fluent Interface)
    public AuthPage switchToRegisterTab() {
        clickWithWait(registerTab);
        return this;
    }

    public AuthPage enterRegisterCredentials(String username, String password) {
        enterText(registerUsername, username);
        enterText(registerPassword, password);
        return this;
    }

    public AuthPage submitRegister() {
        clickWithWait(registerSubmit);
        return this;
    }

    // Проверки состояния
    public String getLoginError() {
        return getElementText(loginError);
    }

    public String getRegisterError() {
        return getElementText(registerError);
    }

    public boolean isRegisterSuccessVisible() {
        return isElementVisible(registerSuccess);
    }

    public boolean isLoginFormDisplayed() {
        return isElementVisible(loginUsername);
    }

    public boolean isRegisterFormDisplayed() {
        return driver.findElement(By.cssSelector(".register-form")).isDisplayed();
    }

    // Вспомогательные методы
    private void waitForPageLoad() {
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(webDriver -> ((JavascriptExecutor) webDriver)
                        .executeScript("return document.readyState").equals("complete"));
    }

    private void clickWithWait(By locator) {
        WebElement element = wait.until(driver -> {
            WebElement el = driver.findElement(locator);
            return el.isDisplayed() && el.isEnabled() ? el : null;
        });

        // Прокрутка к элементу
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);

        // Клик через JavaScript для обхода возможных overlay
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    private void enterText(By locator, String text) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        element.clear();
        element.sendKeys(text);
    }

    private String getElementText(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).getText();
    }

    private boolean isElementVisible(By locator) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).isDisplayed();
        } catch (TimeoutException | NoSuchElementException e) {
            return false;
        }
    }
}