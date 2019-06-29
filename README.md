# SoapUI tests with TestNG and Java.

- place SoapUI projects into `main/java/resources/soap` so that they could be ran by `SoapTest` class
- run `mvn clean test -Dsuite=soapui -Dsoap.project.name=<your_project_name>` command to run soap project
- run `mvn allure:serve` to see Allure report

### Packages
#### main
- `allure/attachment/` - contains class that builds allure attachment and attaches it to report
- `allure/listeners/` - contains classes that listen for soap-tests and build allure
report in each step (before suite, before test, before step, after step, after test, after suite)
- `runner/` - contains class previously processing soap suite and running it
- `utils/` - contains util methods for logging and setting properties of Soap-project objects
#### test
- `./` - contains `SoapTest` class that finds SoapProject in main resources and runs it


### Resources
#### main
- `soap/` - package with SoapUI-xml projects
- `log4j.properties` - log4j properties
- `soap-log4j.xml` - log4j properties for soap (log4j won't log without it)
#### test
- `soapui.xml` - TestNG suite that contains only `SoapTest` class
