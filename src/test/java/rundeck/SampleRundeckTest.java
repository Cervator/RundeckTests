package rundeck;

import com.saucelabs.common.SauceOnDemandAuthentication;
import com.saucelabs.common.SauceOnDemandSessionIdProvider;
import com.saucelabs.junit.ConcurrentParameterized;
import com.saucelabs.junit.SauceOnDemandTestWatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;
import java.util.LinkedList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Contains some sample tests for Rundeck meant to run on Sauce.io
 */
@RunWith(ConcurrentParameterized.class)
public class SampleRundeckTest implements SauceOnDemandSessionIdProvider {

    /** Constant for base URL to test */
    public static final String SITE_TO_TEST = "http://35.194.88.217";

    /** Constructs a {@link SauceOnDemandAuthentication} instance using the supplied user name/access key. */
    public SauceOnDemandAuthentication authentication = new SauceOnDemandAuthentication(
            System.getenv("SAUCE_USER"), // Username set via environment var for flexibility
            System.getenv("SAUCE_ACCESS_KEY")); // Access key set via environment var since secret

    /** JUnit Rule which will mark the Sauce Job as passed/failed when the test succeeds or fails. */
    @Rule
    public SauceOnDemandTestWatcher resultReportingTestWatcher = new SauceOnDemandTestWatcher(this, authentication);

    /** Represents the browser to be used as part of the test run. */
    private String browser;

    /** Represents the operating system to be used as part of the test run. */
    private String os;

    /** Represents the version of the browser to be used as part of the test run. */
    private String version;

    /** Instance variable which contains the Sauce Job Id. */
    private String sessionId;

    /** The {@link WebDriver} instance which is used to perform browser interactions with. */
    private WebDriver driver;

    /**
     * Constructs a new instance of the test.  The constructor requires three string parameters, which represent the operating
     * system, version and browser to be used when launching a Sauce VM.  The order of the parameters should be the same
     * as that of the elements within the {@link #browsersStrings()} method.
     *
     * @param os the target operating system
     * @param version the version of the browser
     * @param browser the target browser
     */
    public SampleRundeckTest(String os, String version, String browser) {
        super();
        this.os = os;
        this.version = version;
        this.browser = browser;
    }

    /**
     * Defines target browsers. The values in the String array are used as part of the invocation of the test constructor.
     *
     * @return a LinkedList containing String arrays representing the browser combinations the test should be run against.
     */
    @ConcurrentParameterized.Parameters
    public static LinkedList browsersStrings() {
        LinkedList<String[]> browsers = new LinkedList<>();
        browsers.add(new String[]{"Windows 10", "11", "internet explorer"});
        browsers.add(new String[]{"macOS 10.13", "11.0", "safari"});
        browsers.add(new String[]{"Linux", "45.0", "firefox"});

        return browsers;
    }

    /**
     * Constructs a new {@link RemoteWebDriver} instance which is configured to use the capabilities defined by the {@link #browser},
     * {@link #version} and {@link #os} instance variables, and which is configured to run against ondemand.saucelabs.com, using
     * the username and access key populated by the {@link #authentication} instance.
     *
     * @throws Exception if an error occurs during the creation of the {@link RemoteWebDriver} instance.
     */
    @Before
    public void setUp() throws Exception {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(CapabilityType.BROWSER_NAME, browser);
        if (version != null) {
            capabilities.setCapability(CapabilityType.VERSION, version);
        }
        capabilities.setCapability(CapabilityType.PLATFORM, os);
        capabilities.setCapability("name", "Rundeck Sample Test");
        this.driver = new RemoteWebDriver(
                new URL("http://" + authentication.getUsername() + ":" + authentication.getAccessKey() + "@ondemand.saucelabs.com:80/wd/hub"),
                capabilities);
        this.sessionId = (((RemoteWebDriver) driver).getSessionId()).toString();
    }

    /**
     * Closes the {@link WebDriver} session.
     * @throws Exception in case of any error
     */
    @After
    public void tearDown() throws Exception {
        driver.quit();
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    private boolean isElementPresent(By by) {
        try {
            driver.findElement(by);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    private void waitForElement(By elementToFind) {
        waitForElement(elementToFind, 10);
    }

    private void waitForElement(By elementToFind, int timeout) {
        for (int second = 0; second <= timeout; second++) {
            try {
                if (isElementPresent(elementToFind)) {
                    return;
                }
                System.out.println("Sleeping a second (" + second + "/" + timeout + ") while waiting for element: " + elementToFind);
                Thread.sleep(1000);
            } catch (Exception e) {
                System.err.println("Exception thrown waiting for element By " + elementToFind + ": " + e);
                fail("Exception while waiting for element");
            }
        }
        System.out.println("Timed out waiting for element: " + elementToFind);
        fail("Timeout after " + timeout + " seconds");
    }

    /**
     * Runs a simple test verifying the title of the Rundeck login page.
     *
     * @throws Exception in case of any error
     */
    @Test
    public void checkLoginPage() throws Exception {
        driver.get(SITE_TO_TEST);
        System.out.println("Site under test via '" + browser + " " + version + "' on '" + os + "' - page title: '" + driver.getTitle() + "'");
        assertEquals("Rundeck - Login", driver.getTitle());
    }

    /**
     * Attempts an actual login into Rundeck then validates a link.
     * @throws Exception in case of any error
     */
    @Test
    public void validateLogin() throws Exception {
        int timeout = 10;
        driver.get(SITE_TO_TEST);

        // Use the username box to consider the page loaded
        By usernameBox = By.name("j_username");
        waitForElement(usernameBox);

        driver.findElement(usernameBox).clear();
        driver.findElement(usernameBox).sendKeys("admin");
        driver.findElement(By.name("j_password")).clear();
        driver.findElement(By.name("j_password")).sendKeys("admin");
        driver.findElement(By.className("btn-primary")).click();

        // Use the new project button to consider the page loaded
        By newProjectButton = By.partialLinkText("New Project");
        waitForElement(newProjectButton);

        String newProjectButtonText = driver.findElement(newProjectButton).getText();
        System.out.println("New project button text: " + newProjectButtonText);
        // Interestingly the trim only matters for Safari - Win10/IE + Linux/Firefox auto-trim?
        assertEquals("New Project", newProjectButtonText.trim());
    }
}
