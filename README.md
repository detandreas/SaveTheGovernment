# SaveTheGovernment

Government budget management system that allows different types of users to view, manage, and approve changes to the national budget.

## Application Description

**SaveTheGovernment is a graphical application that manages the government budget. The application supports multiple user types with different access rights and allows:

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

### Αρχιτεκτονική Frontend Controllers (UML Diagram)

Ακολουθεί το διάγραμμα κλάσεων για το πακέτο `budget.frontend.controller`.
Το διάγραμμα αποτυπώνει:
* Την **Ιεραρχία (Inheritance)** των Dashboards (`Citizen`, `GovernmentMember`, `PrimeMinister`) από τον βασικό `DashboardController`.
* Τη **Ροή Πλοήγησης (Navigation Flow)** από το Login προς τις αντίστοιχες οθόνες.
* Τις **Εξαρτήσεις (Dependencies)**, δηλαδή ποιοι controllers φορτώνονται δυναμικά μέσα στα κεντρικά Dashboards.

![UML Diagram](diagram/controllersDiagram.svg)

> 📂 **Πηγαίος Κώδικας Διαγράμματος:** [Προβολή αρχείου PlantUML](diagram/controllersDigramCode.puml)

## Απαιτήσεις Συστήματος
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
