import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class MainPageTest {
    private MainPage mainPage;
    private WebDriver driver;
    private AuthPage authPage;

    @BeforeAll
    public static void setupAll() {
        // Настройка автоматического управления драйвером
        WebDriverManager.chromedriver()
                .driverVersion("137.0.7151.41")
                .setup();
    }

    @BeforeEach
    public void setup() {
        // 1. Настройка драйвера
        ChromeOptions options = new ChromeOptions();
        options.addArguments(
                "--start-maximized",
                "--remote-allow-origins=*",
                "--disable-dev-shm-usage",
                "--no-sandbox"
        );
        driver = new ChromeDriver(options);

        // 2. Настройка таймаутов
        driver.manage().timeouts()
                .pageLoadTimeout(Duration.ofSeconds(10))
                .scriptTimeout(Duration.ofSeconds(5));

        // 3. Логика из testSuccessfulLogin()
        authPage = new AuthPage(driver).open();
        authPage.switchToLoginTab()
                .enterLoginCredentials("validUser123", "validUser123")
                .submitLogin();

        // 4. Принудительная установка токена и переход
        ((JavascriptExecutor)driver).executeScript(
                "localStorage.setItem('authToken', 'eyJhbGciOiJIUz...');" +
                        "document.cookie = 'authToken=eyJhbGciOiJIUz...; path=/';"
        );

        // 5. Принудительный переход на /main (как в тесте)
        driver.get("http://localhost:8081/main");

        // 6. Ожидание и проверка
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(d -> {
                    boolean onMainPage = d.getCurrentUrl().contains("/main");
                    Object token = ((JavascriptExecutor)d).executeScript(
                            "return localStorage.getItem('authToken');");
                    return onMainPage && token != null;
                });

        mainPage = new MainPage(driver);
    }

    @AfterEach
    public void tearDown() {
        // Очистка результатов после каждого теста
        if (driver != null) {
            mainPage.clearResults();
            driver.quit();
        }
    }

    @Test
    @DisplayName("Добавление точки через форму")
    public void testAddPointViaForm() {
        mainPage.open()
                .selectRValue("3")
                .selectXValue("2")
                .enterYValue("1.5")
                .submitForm();

        assertEquals(1, mainPage.getDotsCount(), "Точка не добавилась на график");
        assertEquals(1, mainPage.getResultsCount(), "Результат не добавился в таблицу");
    }

    @Test
    @DisplayName("Добавление точки через клик на графике")
    public void testAddPointViaGraphClick() {
        mainPage.open()
                .selectRValue("2")
                .clickOnGraph(150, 150); // Клик примерно в центр графика

        assertEquals(1, mainPage.getDotsCount(), "Точка не добавилась на график после клика");
        assertTrue(mainPage.isResultsTableVisible(), "Таблица результатов не отображается");
    }

    @Test
    @DisplayName("Очистка графика и таблицы результатов")
    public void testClearResults() {
        mainPage.open()
                .selectRValue("3")
                .selectXValue("1")
                .enterYValue("1")
                .submitForm();

        mainPage.clearResults();

        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(d -> mainPage.getDotsCount() == 0);

        assertEquals(0, mainPage.getDotsCount(), "Точки не были очищены с графика");
    }

    @Test
    @DisplayName(" Валидация поля Y: ввод некорректного значения")
    public void testValidationForYValue() {
        mainPage.open()
                .selectRValue("4")
                .selectXValue("0")
                .enterYValue("invalid_value"); // Некорректное значение

        // Запоминаем текущее количество результатов
        int initialCount = mainPage.getResultsCount();

        // Нажимаем кнопку отправки
        mainPage.submitForm();

        // Ожидаем появления alert
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());

            // Проверяем текст alert
            assertEquals("Введите корректное значение Y в диапазоне [-5, 5].", alert.getText());
            alert.accept();

            // Проверяем, что количество результатов не изменилось
            assertEquals(initialCount, mainPage.getResultsCount(),
                    "Количество результатов не должно измениться при невалидном Y");
        } catch (TimeoutException e) {
            fail("Alert с сообщением о валидации не появился");
        }
    }

//    @Test
//    public void testLogout() {
//        MainPage mainPage = new MainPage(driver);
//        assertTrue(mainPage.isLogoutButtonVisible(),
//                "Кнопка выхода должна быть видна после авторизации");
//
//        // 3. Выполняем выход
//        authPage = mainPage.logout();
//
//        // 4. Проверяем результат
//        assertTrue(authPage.isLoginFormDisplayed(),
//                "После выхода должна отображаться форма входа");
//        assertTrue(driver.getCurrentUrl().contains("/auth"),
//                "URL должен содержать /auth после выхода");
//    }

    @Test
    @DisplayName("Проверка логики попадания и промаха по области")
    public void testDotHitMissLogic() {
        // Добавляем точку, которая должна попасть в область (1/1 при R=3)
        mainPage.open()
                .selectRValue("3")
                .selectXValue("-1")
                .enterYValue("-1")
                .submitForm();

        mainPage.selectRValue("3")
                .selectXValue("3")
                .enterYValue("3")
                .submitForm();

        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(d -> mainPage.getDotsCount() >= 2);

        assertFalse(mainPage.isDotHit(1), "Точка, которая должна попасть, отмечена как miss");
        assertTrue(mainPage.isDotHit(0), "Точка, которая не должна попасть, отмечена как hit");
    }

    @Test
    @DisplayName("Проверка отображения данных в таблице")
    public void testTableDataFormat() {
        mainPage.open()
                .selectRValue("2")
                .selectXValue("-1")
                .enterYValue("0.5")
                .submitForm();

        assertEquals("-1", mainPage.getResultCellValue(0, 0), "Неверное значение X в таблице");
        assertEquals("0.5", mainPage.getResultCellValue(0, 1), "Неверное значение Y в таблице");
        assertEquals("2", mainPage.getResultCellValue(0, 2), "Неверное значение R в таблице");
    }

    @Test
    @DisplayName("Добавление нескольких точек подряд")
    public void testMultiplePointsSubmission() {
        mainPage.open()
                .selectRValue("2");

        mainPage.selectXValue("1").enterYValue("1").submitForm();
        mainPage.selectXValue("2").enterYValue("0").submitForm();
        mainPage.selectXValue("3").enterYValue("-1").submitForm();

        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(d -> mainPage.getDotsCount() >= 3);

        assertEquals(3, mainPage.getDotsCount(), "Ожидалось 3 точки на графике");
        assertEquals(3, mainPage.getResultsCount(), "Ожидалось 3 строки в таблице");
    }

    @Test
    @DisplayName("Проверка, что результаты изначально отсутствуют")
    public void testNoResultsInitially() {
        mainPage.open();
        mainPage.clearResults();

        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//p[contains(., 'Точек нет')]")));

        assertTrue(mainPage.isNoPointsMessageVisible(), "Сообщение 'Точек нет' должно быть видно при отсутствии результатов");
        assertEquals(0, mainPage.getDotsCount(), "График должен быть пустым при отсутствии точек");
    }

    @Test
    @DisplayName("Клик по графику с разными значениями R")
    public void testGraphClickWithDifferentRValues() {
        mainPage.open();

        // Клик при R=2
        mainPage.selectRValue("2")
                .clickOnGraph(200, 200); // ближе к центру

        // Клик при R=4
        mainPage.selectRValue("4")
                .clickOnGraph(100, 100); // ближе к краю

        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(d -> mainPage.getDotsCount() >= 2);

        assertEquals(2, mainPage.getDotsCount(), "Должно быть 2 точки после кликов на графике");
        assertTrue(mainPage.isResultsTableVisible(), "Таблица должна быть видимой после кликов");
    }
}