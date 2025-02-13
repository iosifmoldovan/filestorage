# REST File Storage Service

## Overview
This is a REST-based file storage service that stores files on disk and provides CRUD operations, file enumeration, and storage size retrieval. It is optimized for handling a large number of files efficiently.

## Features
- Store files in the local file system.
- File names must be between 1-64 characters and match `[a-zA-Z0-9_-]`.
- Supports up to 10,000,000+ files.
- REST API for creating, reading, updating, and deleting files.
- File listing with regex pattern matching.
- Retrieves the total number of stored files.

## Technologies Used
- Java 8
- Maven
- Spring Boot (for REST API)
- JUnit (for testing)

## Installation & Running

### Prerequisites
- Java 8 installed
- Maven installed

### Steps to Run
1. Clone the repository:
   ```sh
   git clone <repository-url>
   cd <project-folder>

2. mvn clean install

3. mvn spring-boot:run
