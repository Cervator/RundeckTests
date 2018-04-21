# Rundeck Sample Tests

This is a quick demo repo showcasing [sauce.io](https://saucelabs.com) tests working on [Rundeck](https://www.rundeck.com).

## Setup

Initially IntelliJ has been used, instructions may vary slightly based on IDE.

* Import project via Maven.
* Pick a Java 8 JDK if needed
* Define environment variables with Sauce credentials
  * `SAUCE_USER` - username for sauce.io account
  * `SAUCE_ACCESS_KEY` - access key from "My Account" section
* Run tests with the Maven test target

Current testing target URL is hard coded for proof of concept, set in `SampleRundeckTest.SITE_TO_TEST`

## Example

The initial tests are run by a Maven style job in the associated Jenkins X instance: http://jenkins.jx.35.186.180.178.nip.io/job/SimpleMavenRundeckTest/ (login required)

Target Rundeck environment is living in the same Kubernetes cluster as the one hosting Jenkins X: http://35.194.88.217

Sample execution on the Sauce side accessible [here](https://saucelabs.com/beta/tests/426fb6326bc34409bef8c495b26b6dd0/commands#15)

## Adding more tests

At the moment simply see the `SampleRundeckTest` class and mimic the existing `@Test` methods. All usual Selenium type mechanics are available.

More advanced CI-based tests are possible, with it would come more structure of test classes.

## Future improvements

* Adapting the main Rundeck repo to a format that Jenkins X will accept
  * This will provide automatic provisioning of test environments
* Use an environment variable to indicate target test environment (to support PR preview envs)
* Applying the Sauce plugin for Jenkins to include rich test results within Jenkins
* Adding the [Groovy Wrapper](https://github.com/MovingBlocks/groovy-wrapper) for raw API tests via Groovy rather than Selenium
* Support one-off test environments by utilizing the Jenkins X Kubernetes control functions
