# Syncplicity-Java-CLI-Sample

Shows examples of various API calls including the initial OAuth2 call.

## Description

The purpose of the command-line sample app is to show examples of various API calls including the initial OAuth2 call.
This type of application would not support SSO-based authentication,
so would be the basis of an application typically used by administrator, not by a typical corporate user.

## System Requirements

* Java SE SDK: 1.7+

## Usage

This sample application demonstrates usage of Syncplicity APIs. This is what you need to know or do before you begin to use Syncplicity APIs:

* Make sure you have an Enterprise Edition account you can use to login to the <https://developer.syncplicity.com>.
* First time login to Syncplicity:
  * You can log into Syncplicity Developer Portal using your Syncplicity login credentials.
    Only Syncplicity Enterprise Edition users are allowed to login to the Developer Portal.
    Based on the configuration done by your Syncplicity administrator,
    Syncplicity Developer Portal will present one of the following options for login:
    * Basic Authentication using Syncplicity username and password.
    * Enterprise Single Sign-on using the Web-SSO service used by your organization. We support ADFS, OneLogin, Ping and Okta.
* Once you have successfully logged in for the first time,
  the Syncplicity Developer Portal automatically creates an Enterprise Edition sandbox account to help you develop and test your application.
  Here is how it works:
  * The Syncplicity Developer Portal automatically creates your sandbox account
    by appending "-apidev" to the email address you used for logging into the Developer Portal.
    For e.g. if you logged into Syncplicity Developer Portal using user@domain.com as your email address,
    then your associated sandbox account email is user-apidev@domain.com.
  * The Developer Portal will prompt you to set your password for this sandbox account.
  * After you have successfully setup your password,
    you can use the sandbox email address and the newly configured password for logging into your sandbox account
    by visiting <https://my.syncplicity.com> and using "-apidev" email address.
    So, in the example above, you will have to use "user-apidev@domain.com" email address to log in to your sandbox account.
* Setup your developer sandbox account by configuring your password:
  * Login to your developer sandbox account by visiting <https://my.syncplicity.com> to make sure its correctly provisioned and that you can access it.
  * Through your user profile in the developer sandbox account,
    create an "Application Token" that you will need to authenticate yourself before making API calls.
    Learn more about this [here](https://syncplicity.zendesk.com/hc/en-us/articles/115002028926-Getting-Started-with-Syncplicity-APIs).
  * Review API documentation by visiting Docs page on the <https://developer.syncplicity.com>.
  * Register you app in the Developer Portal to obtain the "App Key" and "App Secret".
  
## Running

### Basic sample

1. Clone the sample project.
2. Use your favorite Java IDE to open the `.project` file.
3. Define new app on <https://developer.syncplicity.com>. The app key and app secret values are found in the application page.
  The Syncplicity admin token is found on the "My Account" page of the Syncplicity administration page.
  Use the "Application Token" field on that page to generate a token.
4. Update key values in `resources\config.properties`:
    * Update the the consumer key value (`<App Key>`)
    * Update the consumer secret (`<App Secret>`)
    * Update the Syncplicity admin token (`<Admin Token>`)
    * Update the EE account owner email, typically the sandbox owner email for development purposes (`<Owner Email>`)
5. Build and Run the application.

### Debugging with Fiddler

By default, the sample app will not be captured by Fiddler.
To make Fiddler capture requests, specify proxy address and port in JVM startup parameters:

    -DproxyHost=127.0.0.1
    -DproxyPort=8888

* If you run the sample from console, add these two parameters to `java.exe` command-line arguments.
* If you use IntelliJ IDEA, make sure to add them to 'VM options' parameter at Run -> Edit Configurations.
* For other IDEs, please refer to their documentation.

Additionally, you will most likely need to disable SSL certificate validation to see HTTPS traffic to Syncplicity.
WARNING: this should never be done in production code! Disabling validation puts application users at risk of
a malicious person intercepting their data without their knowledge.
SSL validation can be disabled by uncommenting this line in `SampleApp.java`:

    static {
      // unsafeDisableSslVerification();
    }

## Contributing

See contribute.md file in the root directory if you want to submit an issue or add new sample use case etc.

## Team

![alt text][Axwaylogo] Axway Syncplicity Team

[Axwaylogo]: https://github.com/Axway-syncplicity/Assets/raw/master/AxwayLogoSmall.png "Axway logo"

## License

Apache License 2.0
