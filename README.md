# SoapUI tests with TestNG and Java.

- put SoapUI project into `main/java/resources/soap` and run `SoapTest` class
- run `mvn clean test -Dsuite=soapui -Dsoap.project.name=<your_project_name>`
- run mvn `allure:serve` to see Allure report