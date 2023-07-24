# Spring Boot 3.0 Security with JWT Implementation
This project demonstrates the implementation of security using Spring Boot 3.0 and JSON Web Tokens (JWT). It includes the following features:

## Features
* User registration and login with JWT authentication
* Password encryption using BCrypt
* Role-based authorization with Spring Security
* Customized access denied handling
* Logout mechanism
* Refresh token

## Technologies
* Spring Boot 3.0
* Spring Security
* JSON Web Tokens (JWT)
* BCrypt
* Maven
 
## Getting Started
To get started with this project, you will need to have the following installed on your local machine:

* JDK 17+
* Maven 3+


To build and run the project, follow these steps:

* Clone the repository: `git clone https://github.com/ali-bouali/spring-boot-3-jwt-security.git`
* Navigate to the project directory: cd spring-boot-security-jwt
* Add database "jwt_security" to postgres 
* Build the project: mvn clean install
* Run the project: mvn spring-boot:run 

-> The application will be available at http://localhost:8080.

## 备注
* http://localhost:8080/api/v1/auth/register 
* body传递raw json数据  {"firstname":"ye",
    "lastname": "cui",
    "email":"ADMIN123@11.com",
    "password":"111111",
    "role":"ADMIN"
    }
* role可以更换为三种 USER, ADMIN , MANAGER
* 注册返回accesstoken，
* http://localhost:8080/api/v1/admin 将token黏贴到下一个request的authorization中前面加Bearer token
* 如果想尝试其他role，可以更改json数据register中的role
* http://localhost:8080/api/v1/management
