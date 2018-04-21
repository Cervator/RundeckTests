# Rundeck Sample Tests

This is a quick demo repo showcasing [sauce.io](https://saucelabs.com) tests working on [Rundeck](https://www.rundeck.com).

## Setup

Initially IntelliJ has been used, instructions may vary slightly based on IDE.

* Import project via Maven.
* Pick a Java 8 JDK if needed
* Define environment variables with Sauce credentials
  * `SAUCE_USER` - username for sauce.io account
  * `SAUCE_ACCESS_KEY` - access key from "My Account" section

Current testing URL is hard coded for proof of concept, set in `SampleRundeckTest.SITE_TO_TEST`