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

@RunWith(ConcurrentParameterized.class)
public class SampleRundeckTest implements SauceOnDemandSessionIdProvider {

    /**
     * Constant for base URL to test
     */
    public static final String SITE_TO_TEST = "http://35.194.88.217";

    /**
     * Constructs a {@link SauceOnDemandAuthentication} instance using the supplied user name/access key.  To use the authentication
     * supplied by environment variables or from an external file, use the no-arg {@link SauceOnDemandAuthentication} constructor.
     */
    public SauceOnDemandAuthentication authentication = new SauceOnDemandAuthentication(
            System.getenv("SAUCE_USER"), // Username set via environment var for flexibility
            System.getenv("SAUCE_ACCESS_KEY")); // Access key set via environment var since secret

    /**
     * JUnit Rule which will mark the Sauce Job as passed/failed when the test succeeds or fails.
     */
    @Rule
    public SauceOnDemandTestWatcher resultReportingTestWatcher = new SauceOnDemandTestWatcher(this, authentication);

    /**
     * Represents the browser to be used as part of the test run.
     */
    private String browser;
    /**
     * Represents the operating system to be used as part of the test run.
     */
    private String os;
    /**
     * Represents the version of the browser to be used as part of the test run.
     */
    private String version;
    /**
     * Instance variable which contains the Sauce Job Id.
     */
    private String sessionId;

    /**
     * The {@link WebDriver} instance which is used to perform browser interactions with.
     */
    private WebDriver driver;

    /**
     * Constructs a new instance of the test.  The constructor requires three string parameters, which represent the operating
     * system, version and browser to be used when launching a Sauce VM.  The order of the parameters should be the same
     * as that of the elements within the {@link #browsersStrings()} method.
     *
     * @param os
     * @param version
     * @param browser
     */
    public SampleRundeckTest(String os, String version, String browser) {
        super();
        this.os = os;
        this.version = version;
        this.browser = browser;
    }

    /**
     * @return a LinkedList containing String arrays representing the browser combinations the test should be run against. The values
     * in the String array are used as part of the invocation of the test constructor
     */
    @ConcurrentParameterized.Parameters
    public static LinkedList browsersStrings() {
        LinkedList browsers = new LinkedList();
        browsers.add(new String[]{"Windows 10", "11", "internet explorer"});
        browsers.add(new String[]{"macOS 10.13", "11.0", "safari"});
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
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        driver.quit();
    }

    /**
     * TODO: Last time I used this the `@Override` didn't get marked as an error in IntelliJ, what changed?
     * @return the value of the Sauce Job id.
     */
    //@Override
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

    /**
     * Runs a simple test verifying the title of the Rundeck login page
     *
     * @throws Exception
     */
    @Test
    public void checkLoginPage() throws Exception {
        driver.get(SITE_TO_TEST);
        System.out.println("Site under test via '" + browser + " " + version + "' on '" + os + "' - page title: '" + driver.getTitle() + "'");
        assertEquals("Rundeck - Login", driver.getTitle());
    }


    /**
     * Attempts an actual login into Rundeck then validates a link
     * @throws Exception
     */
    @Test
    public void validateLogin() throws Exception {
        int timeout = 10;

        driver.get(SITE_TO_TEST);

        for (int second = 0; ; second++) {
            if (second >= timeout) fail("timeout");
            try {
                if (isElementPresent(By.name("j_username"))) {
                    break;
                }
            } catch (Exception e) {
            }
            Thread.sleep(1000);
        }

        driver.findElement(By.name("j_username")).clear();
        driver.findElement(By.name("j_username")).sendKeys("admin");
        driver.findElement(By.name("j_password")).clear();
        driver.findElement(By.name("j_password")).sendKeys("admin");
        driver.findElement(By.className("btn-primary")).click();

        Thread.sleep(1000);
        assertEquals("Documentation »", driver.findElement(By.linkText("Documentation »")).getText());
    }
}
