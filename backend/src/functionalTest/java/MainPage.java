import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;

public class MainPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    // Локаторы элементов
    private final By graphImage = By.cssSelector(".image-container img");
    private final By dotsContainer = By.cssSelector(".dots");
    private final By dotElements = By.cssSelector(".dot");
    private final By rRadioButtons = By.cssSelector("input[name='r']");
    private final By xRadioButtons = By.cssSelector("input[name='x']");
    private final By yInput = By.id("input-y");
    private final By submitButton = By.id("submit-button");
    private final By clearButton = By.id("clear-button");
    private final By logoutButton = By.id("logout");
    private final By resultsTable = By.cssSelector(".table-check");
    private final By tableRows = By.cssSelector(".table-row");
    private final By noPointsMessage = By.xpath("//p[contains(., 'Точек нет')]");

    public MainPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public By getLogoutButton() {
        return logoutButton;
    }

    public MainPage open() {
        driver.get("http://localhost:8081/main");
        waitForPageLoad();
        return this;
    }

    // Работа с графиком
    public MainPage clickOnGraph(int xOffset, int yOffset) {
        WebElement graph = wait.until(ExpectedConditions.visibilityOfElementLocated(graphImage));

        // Получаем координаты элемента
        org.openqa.selenium.Point location = graph.getLocation();

        // Используем Actions для клика с offset
        new Actions(driver)
                .moveToElement(graph, xOffset, yOffset)
                .click()
                .perform();

        return this;
    }

    public int getDotsCount() {
        return driver.findElements(dotElements).size();
    }

    // Работа с формой
    public MainPage selectRValue(String value) {
        clickWithWait(By.cssSelector("input[name='r'][value='" + value + "']"));
        return this;
    }

    public MainPage selectXValue(String value) {
        clickWithWait(By.cssSelector("input[name='x'][value='" + value + "']"));
        return this;
    }

    public MainPage enterYValue(String value) {
        enterText(yInput, value);
        return this;
    }

    public MainPage submitForm() {
        clickWithWait(submitButton);
        return this;
    }

    public MainPage clearResults() {
        clickWithWait(clearButton);
        wait.until(ExpectedConditions.numberOfElementsToBe(dotElements, 0)); // <- ожидание, что точек не останется
        return this;
    }

    // Работа с таблицей результатов
    public boolean isResultsTableVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(resultsTable)).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    public boolean isNoPointsMessageVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(noPointsMessage)).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    public int getResultsCount() {
        return driver.findElements(tableRows).size();
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

        ((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView(true);", element);
        ((JavascriptExecutor)driver).executeScript("arguments[0].click();", element);
    }

    private void enterText(By locator, String text) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        element.clear();
        element.sendKeys(text);
    }

    // Проверки результатов
    public boolean isDotHit(int index) {
        WebElement dot = driver.findElements(dotElements).get(index);
        return dot.getAttribute("class").contains("hit");
    }

    public String getResultCellValue(int rowIndex, int cellIndex) {
        WebElement row = driver.findElements(tableRows).get(rowIndex);
        return row.findElements(By.tagName("td")).get(cellIndex).getText();
    }

    public boolean isLogoutButtonVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(logoutButton)).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }
    public AuthPage logout() {
        try {
            WebElement logoutBtn = wait.until(ExpectedConditions.elementToBeClickable(logoutButton));

            // Прокрутка к элементу и клик через JS
            ((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView(true);", logoutBtn);
            ((JavascriptExecutor)driver).executeScript("arguments[0].click();", logoutBtn);

            // Ждем появления формы входа
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.urlContains("/auth"));

            return new AuthPage(driver);
        } catch (TimeoutException e) {
            throw new RuntimeException("Не удалось нажать кнопку выхода", e);
        }
    }

}