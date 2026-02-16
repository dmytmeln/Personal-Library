# Personal-Library


## Requirements

#### Application
* Java 17
* Maven 3.9.x (optionally, you can use the included Maven Wrapper)

#### Database
* Postgres


## Setting up a project

#### 1) Clone the repository
```  
git clone https://github.com/dmytmeln/Personal-Library.git
cd Personal-Library
```

#### 2) Build the project
```
cd library-app
./mvnw clean package
```

#### 3) Set Up the Database with Docker Compose (Optional)
The project includes a docker-compose.yml file to run PostgreSQL in a container. To start the database:
```
docker-compose up -d
```
This will create a PostgreSQL container with the following default settings:
```
Database: postgres
Username: postgres
Password: postgres
Port: 5432
```


#### 4) Run the project
```
cd library-app
./mvnw spring-boot:run
```


## Environment variables:
- `DB_URL` (_default_: jdbc:postgresql://localhost:5432/postgres) - PostgreSQL URL
- `DB_USERNAME` (_default_: postgres) -  username used to access to the database
- `DB_PASSWORD` (_default_: postgres) -  password associated with the database username
- `SERVER_PORT` (_default_: 8080) -  The port on which the application server runs.
