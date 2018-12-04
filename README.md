# Syncplicity-Java-CLI-Sample

Shows examples of various API calls including the initial OAuth2 call.

## Description

The purpose of the command-line sample application is to show examples of various API calls including the initial OAuth2 call.
This type of application would not support SSO-based authentication,
so would be the basis of an application typically used by administrator, not by a regular Syncplicity user.

## System Requirements

* Java SE SDK: 1.8+

## Usage

This sample application demonstrates usage of Syncplicity APIs. This is what you need to know or do before you begin to use Syncplicity APIs:

* Make sure you have an Enterprise Edition account you can use to login to the Developer Portal at <https://developer.syncplicity.com>.
* Log into Syncplicity Developer Portal using your Syncplicity login credentials.
  Only Syncplicity Enterprise Edition users are allowed to login to the Developer Portal.
  Based on the configuration done by your Syncplicity administrator,
  Syncplicity Developer Portal will present one of the following options for login:
  * Basic Authentication using Syncplicity username and password.
  * Enterprise Single Sign-on using the web SSO service used by your organization. We support ADFS, OneLogin, Ping and Okta.
* Once you have successfully logged in for the first time,
  you must create an Enterprise Edition sandbox account in the Developer Portal.
  This account can be used to safely test your application using all Syncplicity features
  without affecting your company production data.
  * Log into Syncplicity Developer Portal. Click 'My Profile' and then 'Create sandbox'.
    Refer to the documentation for guidance: <https://developer.syncplicity.com/documentation/overview>.
  * You can log into <https://my.syncplicity.com> using the sandbox account.
    Note that the sandbox account email has "-apidev" suffix.
    So, assuming you regular account email is user@domain.com,
    use user-apidev@domain.com email address to log in to your sandbox account.
* Setup your developer sandbox account:
  * Log into the sandbox account at <https://my.syncplicity.com> to make sure its correctly provisioned and that you can access it.
  * Go to the 'Account' menu.
  * Click "Create" under "Application Token" section.
    The token is used to authenticate an application before making API calls.
    Learn more [here](https://syncplicity.zendesk.com/hc/en-us/articles/115002028926-Getting-Started-with-Syncplicity-APIs).
* Review API documentation by visiting documentation section on the <https://developer.syncplicity.com>.
* Register you application in the Developer Portal to obtain the "App Key" and "App Secret".
  
## Running

### Basic sample

1. Clone the sample project.
2. Use your favorite Java IDE to open the `.project` file.
3. Define new application on <https://developer.syncplicity.com>. The app key and app secret values are found in the application page.
  The Syncplicity admin token is found on the "My Account" page of the Syncplicity administration page.
  Use the "Application Token" field on that page to generate a token.
4. Update key values in `resources\config.properties`:
    * Update the the consumer key value (`<App Key>`)
    * Update the consumer secret (`<App Secret>`)
    * Update the Syncplicity admin token (`<Admin Token>`)
    * Update the EE account owner email, typically the sandbox owner email for development purposes (`<Owner Email>`)
5. Configure the IDE to run as an application and use the `SampleApp` class on launch.
6. Build and Run the application.

### Storage Vault Authentication Sample

__Note:__ This is an advanced concept.
If your company does not use the SVA, you don't need to study it.
[Learn more about SVA.](https://syncplicity.zendesk.com/hc/en-us/articles/202659170-About-Syncplicity-StorageVaults-with-authentication-)

Working with Storage Vaults protected with SVA requires additional authentication procedures.
To run SVA sample:

1. Obtain **Storage Token**, **Machine Id** and **Machine Token**
    used to authenticate calls to Storage Vault.
    Follow the 'Setup Procedure' of the [Content Migration Guide](https://developer.syncplicity.com/content-migration-guide) to get those.
2. Configure the sample (`Resources\config.properties`):
    1. Set Storage Token value (`<Storage Token>`)
    2. Set Machine Token value (`<Machine Token>`)
    3. Set Machine Id value (`<Machine Id>`)
3. Build and run the sample application

### Debugging with Fiddler

By default, the sample application will not be captured by Fiddler.
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
