# Spring Boot Application with Redis

## Overview

This example demonstrates a Spring Boot application connected to a Redis service using Docker Compose. The Spring Boot application exposes a single endpoint for executing statements, and Redis runs on port 6379.
### Prerequisites

* Java 17 or later
* Maven 3.3 or later

## Services

### 1. Spring Boot Application
- **Port:** 8080
- **Endpoint:** [http://localhost:8080/database/executeStatements](http://localhost:8080/database/executeStatements)

### 2. Redis Service
- **Port:** 6379

## Docker Compose Configuration

```yaml
version: '3'
	services:
		app:
		  build: .
		  ports:
			  - "8080:8080"
		  links:
			  - redis
	redis:
		image: redis
		  ports:
			  - "6379:6379"  
```
## Usage

1.  Create the Docker Compose file with the above configuration.
    
2.  Run the following command in the directory containing the Docker Compose file:
    
    `` docker-compose up `` 
    
3.  Access the Spring Boot application endpoint in your browser or using tools like Postman:
    
    [http://localhost:8080/database/executeStatements](http://localhost:8080/database/executeStatements)
    
4.  Verify the connection and functionality of your Spring Boot application with the Redis service.
    
5.  To stop the services, run
    
    `docker-compose down`
