package pt.feirasonline.e2e;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.time.Duration;

public class MercadoFlowE2ETest {

    private WebDriver driver;

    @BeforeEach
    public void setUp() {
        // 1. Configura as opções do Chrome
        ChromeOptions options = new ChromeOptions();

        // 🎯 DETEÇÃO DE AMBIENTE: Se correr no Jenkins, ativa o modo Headless (sem ecrã)
        if (System.getenv("JENKINS_URL") != null || System.getenv("CI") != null) {
            options.addArguments("--headless=new");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
        }

        // 2. Inicializa o Driver automaticamente
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    @Test
    @DisplayName("Deve carregar a página inicial do Angular e listar os mercados")
    public void deveListarMercadosNoFrontend() {
        // ⚠️ Ajusta para o IP/Porta real do teu frontend no Docker ou Localhost
        String urlFrontend = System.getenv("FRONTEND_URL") != null ? System.getenv("FRONTEND_URL") : "http://localhost:4200";

        driver.get(urlFrontend);

        // Exemplo: Validar se o título da tua app Feiras Online está correto
        String titulo = driver.getTitle();
        Assertions.assertTrue(titulo.contains("Feiras") || driver.getPageSource().contains("mercados"));

        // Exemplo: Clicar num botão com ID ou classe específica
        // WebElement botaoAdicionar = driver.findElement(By.id("btn-novo-mercado"));
        // botaoAdicionar.click();
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}