# StoreApp

A JavaFX-based store management application for handling customer, support agent, and administrator interfaces.

## Table of Contents
- [Overview](#overview)
- [Features](#features)
- [System Requirements](#system-requirements)
- [Installation](#installation)
- [Running the Application](#running-the-application)
- [Building](#building)
- [Project Structure](#project-structure)
- [Dependencies](#dependencies)
- [License](#license)

## Overview

StoreApp is a comprehensive retail management system built with JavaFX. The application provides different interfaces and functionalities for customers, support agents, and administrators, enabling efficient store operations management.

## Features

- **User Management**
  - Create and manage different user types (Customers, Support Agents, Administrators)
  - Secure authentication system with password hashing
  - Role-based access control

- **UI Components**
  - Modern JavaFX interface
  - Responsive design elements
  - Form validation

- **Database Integration**
  - SQLite database for data persistence
  - Efficient data access patterns

## System Requirements

- Java Development Kit (JDK) 21 or later
- Maven 3.6.0 or later
- At least 256MB of RAM
- At least 100MB of free disk space

## Installation

1. Clone the repository:
```bash
git clone https://github.com/your-username/StoreApp.git
cd StoreApp
```

2. Build the project with Maven:
```bash
mvn clean install
```

## Running the Application

After building the project, you can run it using Maven:

```bash
mvn exec:java
```

Alternatively, you can run the packaged JAR file:

```bash
java -jar target/StoreApp-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Building

### Creating an Executable JAR

To create an executable JAR with all dependencies included:

```bash
mvn clean package
```

This will create two JAR files in the `target` directory:
- `StoreApp-1.0-SNAPSHOT.jar` - The basic JAR without dependencies
- `StoreApp-1.0-SNAPSHOT-jar-with-dependencies.jar` - Complete executable JAR with all dependencies

## Project Structure

```
com.danieljoaco.storeapp
├── Main.java                 # Application entry point
├── menu                      # UI menu components
├── users                     # User management classes
│   ├── Admin.java
│   ├── Customer.java
│   ├── SupportAgent.java
│   └── UserDao.java          # Data access for users
└── utils                     # Utility classes
    ├── LoginInParameters.java
    └── UserValidator.java    # Validation utilities
```

## Dependencies

- **JavaFX 21** - Modern UI framework for Java applications
- **SQLite JDBC 3.45.1** - Database connectivity for local data storage
- **JBCrypt 0.4** - Password hashing library for secure user authentication
- **SLF4J 2.0.9** - Logging facade for Java applications

## License

[Include your license information here]

## Contributing

[Include contribution guidelines here]

## Contact

[Your contact information]