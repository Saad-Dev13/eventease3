package com.eventease;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class EventEaseE2ETests {

  private static final String BASE_URL = Optional
      .ofNullable(System.getProperty("baseUrl"))
      .or(() -> Optional.ofNullable(System.getenv("BASE_URL")))
      .orElse("http://16.171.139.26:5173");

  private WebDriver driver;
  private WebDriverWait wait;

  @BeforeEach
  void setUp() {
    ChromeOptions options = new ChromeOptions();
    options.addArguments(
        "--headless=new",
        "--no-sandbox",
        "--disable-dev-shm-usage",
        "--disable-gpu",
        "--disable-extensions",
        "--disable-software-rasterizer",
        "--remote-allow-origins=*",
        "--user-data-dir=/tmp/chrome-user-data"
    );
    driver = new ChromeDriver(options);
    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
    wait = new WebDriverWait(driver, Duration.ofSeconds(15));
  }

  @AfterEach
  void tearDown() {
    if (driver != null) {
      driver.quit();
    }
  }

  // ---------- Helpers ----------

  private String uniqueEmail(String prefix) {
    return prefix + System.currentTimeMillis() + "@testmail.com";
  }

  private void signup(String email, String password, String role) {
    driver.get(BASE_URL + "/signup");
    driver.findElement(By.cssSelector("input[type='email']")).sendKeys(email);
    driver.findElement(By.cssSelector("input[type='password']")).sendKeys(password);
    new Select(driver.findElement(By.tagName("select"))).selectByValue(role);
    driver.findElement(By.cssSelector("button[type='submit']")).click();
    wait.until(ExpectedConditions.urlContains("/home"));
  }

  private void login(String email, String password) {
    driver.get(BASE_URL + "/login");
    driver.findElement(By.cssSelector("input[type='email']")).sendKeys(email);
    driver.findElement(By.cssSelector("input[type='password']")).sendKeys(password);
    driver.findElement(By.cssSelector("button[type='submit']")).click();
  }

  private void logoutViaNavbar() {
    WebElement profileButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.profile-btn")));
    profileButton.click();
    WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.logout-btn")));
    logoutButton.click();
    wait.until(ExpectedConditions.urlContains("/login"));
  }

  private void openAdminDashboard() {
    wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Dashboard"))).click();
    wait.until(ExpectedConditions.urlContains("/admin-dashboard"));
  }

  private String createEventThroughUI(String title) {
    driver.findElement(By.xpath("//button[contains(.,'Create New Event')]")).click();

    String description = "Auto description for " + title;
    String location = "Islamabad";
    String dateValue = LocalDateTime.now().plusDays(2).withSecond(0).withNano(0)
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

    wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[placeholder='Event Title']")))
        .sendKeys(title);
    driver.findElement(By.cssSelector("textarea[placeholder='Event Description']")).sendKeys(description);
    driver.findElement(By.cssSelector("input[type='datetime-local']")).sendKeys(dateValue);
    driver.findElement(By.cssSelector("input[placeholder='Location']")).sendKeys(location);
    driver.findElement(By.cssSelector("form button[type='submit']")).click();

    // Wait for modal to close and event list to refresh (backend + frontend processing)
    try {
      Thread.sleep(4000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h3[normalize-space()=" + quote(title) + "]")));
    return title;
  }

  private String quote(String text) {
    return "'" + text + "'";
  }

  // ---------- Tests ----------

  @Test
  void signup_redirectsToHome_withNewUser() {
    String email = uniqueEmail("user_signup_");
    signup(email, "Passw0rd!", "user");
    assertTrue(driver.getCurrentUrl().contains("/home"));
    assertTrue(driver.findElement(By.cssSelector(".navbar"))
        .getText().toLowerCase().contains(email.split("@")[0].toLowerCase()));
  }

  @Test
  void login_succeeds_afterSignup() {
    String email = uniqueEmail("user_login_");
    String password = "Passw0rd!";
    signup(email, password, "user");
    logoutViaNavbar();
    login(email, password);
    wait.until(ExpectedConditions.urlContains("/home"));
    assertTrue(driver.getCurrentUrl().contains("/home"));
  }

  @Test
  void login_failure_keepsUserOnLoginPage() {
    login("nosuchuser@testmail.com", "wrongpass");
    wait.until(ExpectedConditions.urlContains("/login"));
    assertTrue(driver.getCurrentUrl().contains("/login"));
  }

  @Test
  void adminRoute_requiresAuthentication() {
    driver.get(BASE_URL + "/admin-dashboard");
    wait.until(ExpectedConditions.urlContains("/login"));
    assertTrue(driver.getCurrentUrl().contains("/login"));
  }

  @Test
  void contactForm_submitsAndClearsFields() {
    driver.get(BASE_URL + "/contact");
    driver.findElement(By.cssSelector("input[placeholder='Name']")).sendKeys("Test User");
    driver.findElement(By.cssSelector("input[placeholder='E-mail']")).sendKeys(uniqueEmail("contact_"));
    driver.findElement(By.cssSelector("input[placeholder='Subject']")).sendKeys("Hello");
    driver.findElement(By.cssSelector("textarea[placeholder='Message']")).sendKeys("Testing contact form");
    driver.findElement(By.cssSelector("button[type='submit']")).click();

    wait.until(ExpectedConditions.attributeToBe(By.cssSelector("input[placeholder='Name']"), "value", ""));
    assertTrue(driver.findElement(By.cssSelector("input[placeholder='Name']")).getAttribute("value").isEmpty());
  }

  @Test
  void servicesPage_showsEventsSection() {
    driver.get(BASE_URL + "/services");
    WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[normalize-space()='Upcoming Events']")));
    assertTrue(heading.isDisplayed());
  }

  @Test
  void admin_canCreateEvent() {
    String email = uniqueEmail("admin_create_");
    String password = "Passw0rd!";
    signup(email, password, "admin");
    openAdminDashboard();

    String title = "Auto Event " + System.currentTimeMillis();
    String createdTitle = createEventThroughUI(title);
    assertTrue(driver.findElement(By.xpath("//h3[normalize-space()=" + quote(createdTitle) + "]"))
        .isDisplayed());
  }

  @Test
  void admin_canEditEventTitle() {
    String email = uniqueEmail("admin_edit_");
    String password = "Passw0rd!";
    signup(email, password, "admin");
    openAdminDashboard();

    String originalTitle = "Edit Event " + System.currentTimeMillis();
    createEventThroughUI(originalTitle);

    WebElement card = driver.findElement(By.xpath("//div[contains(@class,'event-card-admin')]//h3[normalize-space()=" + quote(originalTitle) + "]/ancestor::div[contains(@class,'event-card-admin')]"));
    card.findElement(By.xpath(".//button[normalize-space()='Edit']")).click();

    WebElement titleInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[placeholder='Event Title']")));
    titleInput.clear();
    String updatedTitle = originalTitle + " Updated";
    titleInput.sendKeys(updatedTitle);
    driver.findElement(By.cssSelector("form button[type='submit']")).click();

    wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h3[normalize-space()=" + quote(updatedTitle) + "]")));
    assertTrue(driver.findElements(By.xpath("//h3[normalize-space()=" + quote(originalTitle) + "]")).isEmpty());
  }

  @Test
  void admin_canDeleteEvent() {
    String email = uniqueEmail("admin_delete_");
    String password = "Passw0rd!";
    signup(email, password, "admin");
    openAdminDashboard();

    String title = "Delete Event " + System.currentTimeMillis();
    createEventThroughUI(title);

    WebElement card = driver.findElement(By.xpath("//div[contains(@class,'event-card-admin')]//h3[normalize-space()=" + quote(title) + "]/ancestor::div[contains(@class,'event-card-admin')]"));
    card.findElement(By.xpath(".//button[normalize-space()='Delete']")).click();

    Alert confirm = wait.until(ExpectedConditions.alertIsPresent());
    confirm.accept();

    wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//h3[normalize-space()=" + quote(title) + "]")));
    assertTrue(driver.findElements(By.xpath("//h3[normalize-space()=" + quote(title) + "]")).isEmpty());
  }

  @Test
  void logout_clearsSession() {
    String email = uniqueEmail("user_logout_");
    signup(email, "Passw0rd!", "user");
    logoutViaNavbar();
    assertTrue(driver.getCurrentUrl().contains("/login"));
  }
}
