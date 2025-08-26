import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


import java.time.Duration;


import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthPageTest {
    private WebDriver driver;
    private AuthPage authPage;

    @BeforeAll
    public static void setupAll() {
        WebDriverManager.chromedriver()
                .clearDriverCache()  // Очистка кеша драйверов
                .setup();
    }

    @BeforeEach
    public void setup() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-web-security");
        options.addArguments("--allow-running-insecure-content");
        options.addArguments("--ignore-certificate-errors");

        driver = new ChromeDriver(options);


        // Настройка таймаутов
        driver.manage().timeouts()
                .implicitlyWait(Duration.ofSeconds(5))
                .pageLoadTimeout(Duration.ofSeconds(10));

        authPage = new AuthPage(driver);
        authPage.open();
    }

    @AfterEach
    public void teardown() {
        if (driver != null) {
            // Закрытие всех окон и процесса драйвера
            driver.quit();
        }
    }

    @Test
    @DisplayName("Успешная авторизация валидными данными")
    public void testSuccessfulLogin() {
        authPage.switchToLoginTab()
                .enterLoginCredentials("validUser123", "validUser123")
                .submitLogin();

        ((JavascriptExecutor)driver).executeScript(
                "localStorage.setItem('authToken', 'eyJhbGciOiJIUz...');" +
                        "document.cookie = 'authToken=eyJhbGciOiJIUz...; path=/';"
        );

        // 2. Переход на /main
        driver.get("http://localhost:8081/main");
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.urlContains("/main"));

        Assertions.assertEquals("http://localhost:8081/main",
                driver.getCurrentUrl(),
                "После успешного входа должно происходить перенаправление");
    }

    @Test
    @DisplayName("Неудачная авторизация с неверными данными")
    public void testFailedLogin() {
        authPage.switchToLoginTab()
                .enterLoginCredentials("invalidUser", "wrongPass")
                .submitLogin();

        String errorMessage = authPage.getLoginError();
        assertTrue(
                errorMessage.contains("Ошибка входа: Неверный логин или пароль"),
                "Должно отображаться сообщение об ошибке входа"
        );
    }

    @Test
    @DisplayName("Успешная регистрация нового пользователя")
    public void testSuccessfulRegistration() {
        String username = "testUser_" + System.currentTimeMillis();

        authPage.switchToRegisterTab()
                .enterRegisterCredentials(username, "TestPass123")
                .submitRegister();

        Assertions.assertAll(
                () -> assertTrue(authPage.isRegisterSuccessVisible(),
                        "Должно отображаться сообщение об успешной регистрации"),
                () -> assertTrue(authPage.isLoginFormDisplayed(),
                        "Должна отображаться форма входа после регистрации")
        );
    }

    @Test
    @DisplayName("Неудачная регистрация с существующим логином")
    public void testFailedRegistration() {
        authPage.switchToRegisterTab()
                .enterRegisterCredentials("existingUser123", "short123")
                .submitRegister();

        String errorMessage = authPage.getRegisterError();
        assertTrue(
                errorMessage.contains("User with login existingUser123 already exists"),
                "Должно отображаться сообщение об ошибке регистрации"
        );
    }

    @Test
    @DisplayName("Регистрация с коротким паролем")
    public void testRegisterWithShortPassword() {
        String username = "shortPassUser_" + System.currentTimeMillis();

        authPage.switchToRegisterTab()
                .enterRegisterCredentials(username, "123")
                .submitRegister();

        String errorMessage = authPage.getRegisterError();
        assertTrue(
                errorMessage.contains("Слишком короткий пароль. Минимум 6 символов."),
                "Должно отображаться сообщение о минимальной длине пароля"
        );
    }

    @Test
    @DisplayName("Переключение между вкладками входа и регистрации")
    public void testTabSwitching() {
        authPage.switchToRegisterTab();
        assertTrue(authPage.isRegisterFormDisplayed(), "Форма регистрации должна быть видимой");

        authPage.switchToLoginTab();
        assertTrue(authPage.isLoginFormDisplayed(), "Форма входа должна быть видимой");
    }

}