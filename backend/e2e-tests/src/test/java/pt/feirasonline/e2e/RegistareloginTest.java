package pt.feirasonline.e2e;

import org.aspectj.lang.annotation.After;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Alert;
import org.openqa.selenium.Keys;
import java.util.*;
import java.net.MalformedURLException;
import java.net.URL;

public class RegistareloginTest {
  private WebDriver driver;
  private Map<String, Object> vars;
  JavascriptExecutor js;
  @BeforeEach
  public void setUp() {
    driver = new FirefoxDriver();
    js = (JavascriptExecutor) driver;
    vars = new HashMap<String, Object>();
  }
  @After("")
  public void tearDown() {
    driver.quit();
  }
  @Test
  public void registarelogin() {
      String urlFrontend = System.getenv("FRONTEND_URL") != null ? System.getenv("FRONTEND_URL") : "http://localhost:4200";
    driver.get(urlFrontend);
    driver.manage().window().setSize(new Dimension(1936, 1048));
    driver.findElement(By.linkText("Entrar no Portal")).click();
    driver.findElement(By.linkText("Registe-se aqui")).click();
    driver.findElement(By.id("nomeFeirante")).click();
    driver.findElement(By.id("nomeFeirante")).click();
    driver.findElement(By.id("nomeFeirante")).sendKeys("Feirante");
    driver.findElement(By.id("email")).click();
    driver.findElement(By.id("email")).sendKeys("feirante@test.com");
    driver.findElement(By.cssSelector(".min-h-screen")).click();
    driver.findElement(By.id("password")).click();
    driver.findElement(By.id("password")).sendKeys("password");
    driver.findElement(By.cssSelector(".btn-primary")).click();
    {
      WebElement element = driver.findElement(By.id("email"));
      Actions builder = new Actions(driver);
      builder.moveToElement(element).clickAndHold().perform();
    }
    {
      WebElement element = driver.findElement(By.id("email"));
      Actions builder = new Actions(driver);
      builder.moveToElement(element).perform();
    }
    {
      WebElement element = driver.findElement(By.id("email"));
      Actions builder = new Actions(driver);
      builder.moveToElement(element).release().perform();
    }
    driver.findElement(By.id("email")).click();
    driver.findElement(By.id("email")).sendKeys("feirante@test.com");
    driver.findElement(By.cssSelector(".w-full > span")).click();
  }
}
