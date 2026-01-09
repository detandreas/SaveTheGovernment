# SaveTheGovernment

Government budget management system that allows different types of users to view, manage, and approve changes to the national budget.

## Application Description

**SaveTheGovernment** is a graphical application that manages the government budget. The application supports multiple user types with different access rights and allows:

- Viewing the overall budget
- Viewing the change history
- Viewing statistics
- Managing budget items
- Submitting change requests
- Approving or rejecting change requests
- Recording all changes in a change log

## User Roles

The application supports four types of users with different access rights:

### 1. Citizen
- View the overall budget
- View the change history
- View statistics
- Update personal information (username, password, name)

### 2. Government Member
Includes all Citizen permissions, plus:
- Submit requests for budget fund modifications
- View the history of change requests

### 3. Finance Ministry Member
Includes all Government Member permissions, plus:
-Directly manage budget items

### 4. Prime Minister
- View the overall budget
-View the change history
-View statistics
-Approve or reject pending change requests

**Note**: Only one Prime Minister can exist in the system (Singleton pattern).

## System Architecture (UML Diagram)

Below is the class diagram that describes the structure of the backend and the organization of the packages.

![UML Diagram](diagram/classDiagram.svg)

> 📂 **Diagram Source Code:** [File preview PlantUML](diagram/classDigramCode.puml)

### FrontEnd Controllers Architecture (UML Diagram)

The following is the class diagram for the budget.frontend.controller package.
* The inheritance hierarchy of the Dashboards (Citizen, GovernmentMember, PrimeMinister) derived from the base DashboardController.
* The **Navigation Flow** from the Login screen to the corresponding application screens.
* The **Dependencies**, representing which controllers are dynamically loaded within the main Dashboards
![UML Diagram](diagram/controllersDiagram.svg)

> 📂 **Diagram Source Code:** [View PlantUML file](diagram/controllersDigramCode.puml)

## System Requirements

- **Java**: Version 21 ή latest
- **Maven**: Version 3.9.6 ή latests
- **Operating System**: Windows, macOS, ή Linux

## Installation

### 1.  Repository Clone

```bash
git clone https://github.com/detandreas/SaveTheGoverment.git
cd SaveTheGoverment
```
### 2. Requirements Check
Make sure that you have installed Java 21 and Maven:
```bash
java -version    # It should display the version 21
mvn -version     #  It should display the version 3.9.6 or latest
```
## Running the Application

### Method 1: With Maven (It is recommended)

```bash
mvn compile javafx:run # If you have Maven installed on your computer
```
### Method 2: With Maven Wrapper

```bash
./mvnw compile javafx:run    # Linux/macOS
mvnw.cmd compile javafx:run   # Windows
```
### Method 3: With JAR αρχείο
```bash
# First, create the JAR
mvn clean package

# Then, run it
java -Dbudget.data.dir=/var/app/data \
     -jar savethegovernment-1.0-SNAPSHOT.jar
```
## Software Patterns
- **Singleton pattern:** It is used for uniqueness Prime Minister
- **DI (Dependency Injection):** It is mainly used in the communication of the Services <-> Repositories
- **Repository Design Pattern:**  For clear separation of data and business logic
- **MVC Architecture (Model View Controller):** For efficient management of the GUI

## Algorithms
- **SHA-256:** For password hashing and secure storage in the users.json files

## API / Code Documentation (Javadoc)

The project uses standard Javadoc comments for documenting
its public API.

### Generate Javadoc
To generate the API documentation, run:

```bash
./mvnw javadoc:javadoc
```

### Read API Documentation
```bash
open target/site/apidocs/index.html 
```

## Contribution

This application was developed as an assignment for the course **PROGRAMMING II**.
