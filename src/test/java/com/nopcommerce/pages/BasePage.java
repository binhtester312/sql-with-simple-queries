package com.nopcommerce.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class BasePage {
    // viết các method của selenium dùng chung
    // findElemt, click, sendKeys, getText, isDisplayed
    protected WebDriver driver;
    protected WebDriverWait wait;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    protected By getLocator(String locatorValue) {
        if (locatorValue.startsWith("Xpath=") || locatorValue.startsWith("xpath=") || locatorValue.startsWith("XPATH=")) {
            return By.xpath(locatorValue.substring(6));
        } else if (locatorValue.startsWith("id=") || locatorValue.startsWith("Id=") || locatorValue.startsWith("ID=")) {
            return By.id(locatorValue.substring(3));
        } else if (locatorValue.startsWith("css=") || locatorValue.startsWith("Css=") || locatorValue.startsWith("CSS=")) {
            return By.cssSelector(locatorValue.substring(4));
        }
        return By.xpath(locatorValue);
    }

    protected WebElement findElement(By locator) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    protected WebElement findElement(String locator) {
        try {
            return findElement(getLocator(locator));
        } catch (Exception e) {
            System.out.println("⚠️ [AUTO-HEALING] Primary locator failed: '" + locator + "'. Initiating auto-healing...");
            WebElement healed = autoHealLocator(locator);
            if (healed != null) {
                return healed;
            }
            throw e;
        }
    }

    protected void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    protected void click(String locator) {
        try {
            click(getLocator(locator));
        } catch (Exception e) {
            System.out.println("⚠️ [AUTO-HEALING] Click failed for primary locator: '" + locator + "'. Initiating auto-healing...");
            WebElement healed = autoHealLocator(locator);
            if (healed != null) {
                wait.until(ExpectedConditions.elementToBeClickable(healed)).click();
                return;
            }
            throw e;
        }
    }

    protected void sendKeys(By locator, String text) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        element.clear();
        element.sendKeys(text);
    }

    protected void sendKeys(String locator, String text) {
        try {
            sendKeys(getLocator(locator), text);
        } catch (Exception e) {
            System.out.println("⚠️ [AUTO-HEALING] sendKeys failed for primary locator: '" + locator + "'. Initiating auto-healing...");
            WebElement healed = autoHealLocator(locator);
            if (healed != null) {
                healed.clear();
                healed.sendKeys(text);
                return;
            }
            throw e;
        }
    }

    protected String getText(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).getText();
    }

    protected String getText(String locator) {
        try {
            return getText(getLocator(locator));
        } catch (Exception e) {
            System.out.println("⚠️ [AUTO-HEALING] getText failed for primary locator: '" + locator + "'. Initiating auto-healing...");
            WebElement healed = autoHealLocator(locator);
            if (healed != null) {
                return healed.getText();
            }
            throw e;
        }
    }

    protected boolean isDisplayed(By locator) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    protected boolean isDisplayed(String locator) {
        try {
            return isDisplayed(getLocator(locator));
        } catch (Exception e) {
            WebElement healed = autoHealLocator(locator);
            return healed != null && healed.isDisplayed();
        }
    }

    /**
     * Heuristic Auto-Healing strategy to locate elements when primary locator fails
     */
    private WebElement autoHealLocator(String brokenLocator) {
        String rawVal = brokenLocator.contains("=") ? brokenLocator.substring(brokenLocator.indexOf("=") + 1) : brokenLocator;
        // Clean special xpath characters if any to extract key term
        String keyTerm = rawVal.replaceAll("[^a-zA-Z0-9_-]", " ").trim();
        if (keyTerm.contains(" ")) {
            String[] parts = keyTerm.split("\\s+");
            keyTerm = parts[parts.length - 1]; // take last word or main identifier
        }

        List<By> candidateLocators = new ArrayList<>();
        if (!keyTerm.isEmpty()) {
            candidateLocators.add(By.className("ico-" + keyTerm));
            candidateLocators.add(By.className(keyTerm));
            candidateLocators.add(By.xpath("//a[contains(@class, '" + keyTerm + "')]"));
            candidateLocators.add(By.xpath("//*[contains(@class, '" + keyTerm + "')]"));
            candidateLocators.add(By.xpath("//a[contains(translate(text(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + keyTerm.toLowerCase() + "')]"));
            candidateLocators.add(By.xpath("//*[contains(translate(text(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + keyTerm.toLowerCase() + "')]"));
            candidateLocators.add(By.xpath("//*[contains(@href, '" + keyTerm.toLowerCase() + "')]"));
            candidateLocators.add(By.xpath("//*[@id='" + keyTerm + "']"));
            candidateLocators.add(By.xpath("//*[@name='" + keyTerm + "']"));
        }

        // Generic fallback for Register link if term is register
        if (brokenLocator.toLowerCase().contains("register")) {
            candidateLocators.add(By.xpath("//a[@class='ico-register']"));
            candidateLocators.add(By.xpath("//a[contains(text(),'Register')]"));
        }

        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(2));
        for (By candidate : candidateLocators) {
            try {
                WebElement el = shortWait.until(ExpectedConditions.presenceOfElementLocated(candidate));
                if (el.isDisplayed()) {
                    System.out.println("🩹 [AUTO-HEALING SUCCESS] Broken locator '" + brokenLocator + "' healed using candidate: " + candidate);
                    return el;
                }
            } catch (Exception ignored) {
            }
        }
        System.err.println("❌ [AUTO-HEALING FAILED] Could not heal broken locator: '" + brokenLocator + "'");
        return null;
    }
}
