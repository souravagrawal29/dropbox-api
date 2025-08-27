# dropbox-api
A basic Java application implementing OAuth 2.0 for authentication and calling Dropbox APIs to get information.


## 📋 Prerequisites
- Java 8 or higher
- Maven 3.x
- A Dropbox developer account

## 🔐 Configuration
Create a Dropbox App from the [Dropbox App Console](https://www.dropbox.com/developers/apps) and note the following credentials:


Populate the following fields under existing file `dropbox.properties`:

```properties
dropbox.clientId=
dropbox.clientSecret=
dropbox.redirectUrl=
dropbox.scope=
```

After populating the properties, run `mvn clean install`. This command should generate a jar named `dropbox-api.jar` under the target folder.

Run the jar using the following command

```bash
java -jar dropbox-api.jar
```

Follow the instructions on the cli to get the team information.