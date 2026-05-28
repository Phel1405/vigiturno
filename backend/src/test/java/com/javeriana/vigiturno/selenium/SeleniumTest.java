package com.javeriana.vigiturno.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class SeleniumTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private final String url = "http://localhost:4200";

    @BeforeEach
    public void setUp() {

        // Para NixOS
        System.setProperty("webdriver.chrome.driver", "/run/current-system/sw/bin/chromedriver");

        //WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @Test
    void testFlujoCOmpletoGestionTurno() throws InterruptedException {
        driver.get(url);

        wait.until(ExpectedConditions.urlContains("/dashboard"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/dashboard"));

        WebElement headerAdmin = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(text(), 'Modulo Administrador')]")
        ));
        Assertions.assertNotNull(headerAdmin);

        WebElement linkUsuarios = driver.findElement(By.linkText("Usuarios"));
        Assertions.assertTrue(linkUsuarios.isDisplayed());

        WebElement botonSalir = driver.findElement(By.xpath("//button[contains(text(), 'Salir')]"));
        Assertions.assertNotNull(botonSalir);
        botonSalir.click();

        WebElement textoSeleccionaRol = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(text(), 'Selecciona un rol')]'")
        ));
        Assertions.assertNotNull(textoSeleccionaRol);

        WebElement linkHome = driver.findElement(By.linkText("Home"));
        Assertions.assertTrue(linkHome.isDisplayed());
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
