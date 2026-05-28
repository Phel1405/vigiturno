package com.javeriana.vigiturno.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SeleniumTest {
    private WebDriver driver;
    private WebDriverWait wait;
    private static final String BASE_URL = "http://localhost:4200";

    private static final String ADMIN_EMAIL = "test@test.com";
    private static final String ADMIN_PASSWORD = "123456";

    private static final String TIMESTAMP = String.valueOf(System.currentTimeMillis());
    private static final String DOCENTE_NAME = "Docente Operativo " + TIMESTAMP;
    private static final String DOCENTE_EMAIL = "docente_" + TIMESTAMP + "@javeriana.edu.co";
    private static final String DOCENTE_PASSWORD = "PasswordDocente123";

    @BeforeEach
    public void setUp() {

        System.setProperty("webdriver.chrome.driver", "/run/current-system/sw/bin/chromedriver");
        ChromeOptions options = new ChromeOptions();

        //WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void Prueba_e2e() throws InterruptedException {

        driver.get(BASE_URL + "/register");
        Thread.sleep(2000);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("nombre"))).sendKeys(DOCENTE_NAME);
        driver.findElement(By.name("email")).sendKeys(DOCENTE_EMAIL);
        driver.findElement(By.name("password")).sendKeys(DOCENTE_PASSWORD);

        Select selectRol = new Select(driver.findElement(By.name("rol")));
        selectRol.selectByValue("DOCENTE");
        Thread.sleep(2000);

        driver.findElement(By.cssSelector("form")).submit();

        wait.until(ExpectedConditions.urlContains("/login"));
        Thread.sleep(2000);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")))
                .sendKeys(ADMIN_EMAIL);
        driver.findElement(By.name("password")).sendKeys(ADMIN_PASSWORD);
        Thread.sleep(2000);

        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(ExpectedConditions.urlContains("/dashboard"));
        Thread.sleep(2000);

        WebElement linkTurnos = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[@routerLink='/turnos' and contains(text(), 'Turnos')]")
        ));
        linkTurnos.click();
        Thread.sleep(2000);

        WebElement btnNuevoTurno = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), 'Nuevo turno')]")
        ));
        btnNuevoTurno.click();
        Thread.sleep(2000);

        Select selectEstado = new Select(driver.findElement(By.name("estado")));
        wait.until(d -> selectEstado.getOptions().size() > 0);
        selectEstado.selectByIndex(0);

        LocalTime ahora = LocalTime.now();
        DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("HH:mm");
        String horaInicioStr = ahora.format(formatoHora);
        String horaFinStr = ahora.plusHours(2).format(formatoHora);

        driver.findElement(By.name("horaInicio")).sendKeys(horaInicioStr);
        driver.findElement(By.name("horaFin")).sendKeys(horaFinStr);

        Select selectDocente = new Select(driver.findElement(By.name("usuarioId")));
        wait.until(d -> selectDocente.getOptions().size() > 1);
        selectDocente.selectByVisibleText(DOCENTE_NAME);

        WebElement elementoZona = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("zonaId")));

        Select selectZona = new Select(elementoZona);

        wait.until(driver -> selectZona.getOptions().size() > 0);

        selectZona.selectByIndex(0);

        Thread.sleep(2000);

        driver.findElement(By.cssSelector("form.form")).submit();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("table tbody tr")));
        Thread.sleep(3000);

        WebElement btnSalir = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), 'Cerrar Sesión') or contains(text(), 'Salir')]")
        ));
        btnSalir.click();

        wait.until(ExpectedConditions.urlContains("/login"));
        Thread.sleep(2000);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email"))).sendKeys(DOCENTE_EMAIL);
        driver.findElement(By.name("password")).sendKeys(DOCENTE_PASSWORD);
        Thread.sleep(2000);

        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(ExpectedConditions.urlContains("/turnos"));
        Thread.sleep(2000);

        WebElement btnCheckIn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), 'Iniciar turno')]")
        ));
        btnCheckIn.click();
        Thread.sleep(1500);

        WebElement btnConfirmarInicio = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), 'Sí, iniciar')]")
        ));
        btnConfirmarInicio.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("table tbody tr")));
        Thread.sleep(3000);
    }
}
