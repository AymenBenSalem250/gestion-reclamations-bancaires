# Reclamation Management System - Backend API

A complete Spring Boot backend application for managing customer complaints (reclamations) with JWT authentication, role-based access control, and comprehensive REST APIs.

---

## 📋 Table of Contents
- [Overview](#overview)
- [Technology Stack](#technology-stack)
- [Features](#features)
- [Quick Start](#quick-start)
- [Documentation](#documentation)
- [Project Structure](#project-structure)
- [API Endpoints](#api-endpoints)
- [Security](#security)
- [Database Schema](#database-schema)
- [Testing](#testing)
- [Production Deployment](#production-deployment)

---

## 🎯 Overview

This application provides a robust backend system for managing customer complaints with features including:
- User authentication and authorization with JWT
- Role-based access control (ADMIN, AGENT, RESPONSABLE, CLIENT)
- Complete complaint lifecycle management
- Reference data management
- Statistics and reporting
- Notification tracking (Email/SMS)

---

## 🛠️ Technology Stack

- **Java 17**
- **Spring Boot 4.1.0**
- **Spring Security 6** with JWT
- **Spring Data JPA**
- **Hibernate 7.x**
- **MySQL 8.0**
- **Maven**
- **JAXB & Jackson** for XML/JSON processing

---

## ✨ Features

### Authentication & Authorization
- ✅ JWT-based authentication
- ✅ Email-based login (not username)
- ✅ BCrypt password encryption
- ✅ Role-based access control (RBAC)
- ✅ Account enable/disable
- ✅ Max login attempts tracking (3 attempts)
- ✅ Password reset functionality

### Reclamation Management
- ✅ Create, read, update, delete complaints
- ✅ Auto-generate unique reference numbers
- ✅ Status workflow management (NOUVEAU → EN_COURS → TRAITE → CLOTURE)
- ✅ Response management
- ✅ Multi-criteria search (status, date, email, object, name)
- ✅ Statistics (count by status, date range)
- ✅ XML generation tracking
- ✅ Email/SMS notification tracking

### Reference Data Management
- ✅ Status (Statut)
- ✅ Services (Service)
- ✅ Objects (Objet)
- ✅ Communication channels (Canal)
- ✅ Complainant types (NatureReclamant)
- ✅ Business domains (Domaine)
- ✅ Application modules (Module)
- ✅ Parameters (ParamGen)

### User Management
- ✅ CRUD operations for users
- ✅ Role assignment
- ✅ Account management
- ✅ Email validation
- ✅ Search by email

### Role & Privilege Management
- ✅ Hierarchical role system
- ✅ Fine-grained privileges
- ✅ Role-privilege mapping
- ✅ User-role assignment

---

## 🚀 Quick Start

### Prerequisites
- Java 17
- MySQL 8.0
- Maven (included via wrapper)

### 1. Create Database
```sql
CREATE DATABASE reclamation CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Initialize Reference Data
```bash
mysql -u root -p reclamation < init_test_data.sql
```

### 3. Configure Database
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/reclamation
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
```

### 4. Run Application
```bash
.\mvnw.cmd spring-boot:run
```

Application starts on: `http://localhost:8080`

### 5. Test with Postman
1. Import `Reclamation_App_Postman_Collection.json`
2. Import `Reclamation_Dev_Environment.json`
3. Select "Reclamation Dev" environment
4. Run requests in order: Signup → Login → Test endpoints

**See [QUICKSTART.md](QUICKSTART.md) for detailed step-by-step guide.**

---

## 📚 Documentation

| Document | Description |
|----------|-------------|
| **[QUICKSTART.md](QUICKSTART.md)** | 5-minute getting started guide |
| **[API_DOCUMENTATION.md](API_DOCUMENTATION.md)** | Complete API reference (40+ endpoints) |
| **[POSTMAN_TESTING_GUIDE.md](POSTMAN_TESTING_GUIDE.md)** | Comprehensive testing guide with examples |
| **[PROJECT_STATUS.md](PROJECT_STATUS.md)** | Detailed project status and implementation details |
| **[FIX_SUMMARY.md](FIX_SUMMARY.md)** | Technical fixes and solutions applied |

---

## 📁 Project Structure

```
src/main/java/tn/reclamationapp/
├── config/
│   └── SecurityConfig.java              # Spring Security configuration
├── controller/
│   ├── AuthController.java              # Authentication endpoints
│   ├── ReclamationController.java       # Complaint management
│   ├── UsersController.java             # User management
│   ├── RoleController.java              # Role management
│   ├── ReferenceDataController.java     # Reference data
│   └── GlobalExceptionHandler.java      # Error handling
├── dto/
│   ├── LoginRequest.java                # Login payload
│   ├── SignupRequest.java               # Registration payload
│   ├── JwtResponse.java                 # JWT token response
│   └── MessageResponse.java             # Generic response
├── model/
│   ├── Users.java                       # User entity
│   ├── Role.java                        # Role entity
│   ├── Privilege.java                   # Privilege entity
│   ├── Reclamation.java                 # Complaint entity
│   ├── Statut.java                      # Status entity
│   └── ... (16 more entities)
├── repository/
│   ├── UsersRepository.java             # User data access
│   ├── ReclamationRepository.java       # Complaint data access
│   └── ... (12 more repositories)
├── security/
│   ├── AuthTokenFilter.java             # JWT filter
│   ├── AuthEntryPointJwt.java           # Auth error handler
│   ├── JwtUtils.java                    # JWT utilities
│   ├── UserDetailsImpl.java             # User details
│   └── UserDetailsServiceImpl.java      # User loading
├── service/
│   ├── UsersService.java                # User business logic
│   ├── ReclamationService.java          # Complaint business logic
│   ├── RoleService.java                 # Role business logic
│   └── ReferenceDataService.java        # Reference data logic
└── ReclamationAppApplication.java       # Main application
```

---

## 🔌 API Endpoints

### Authentication (`/api/auth/*`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/signup` | Register new user | No |
| POST | `/api/auth/signin` | Login and get JWT | No |

### Users (`/api/users/*`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/users` | Get all users | Yes (ADMIN) |
| GET | `/api/users/{id}` | Get user by ID | Yes |
| GET | `/api/users/email/{email}` | Get user by email | Yes |
| PUT | `/api/users/{id}` | Update user | Yes |
| DELETE | `/api/users/{id}` | Delete user | Yes (ADMIN) |
| PUT | `/api/users/{id}/role` | Assign role | Yes (ADMIN) |

### Reclamations (`/api/reclamations/*`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/reclamations` | Get all complaints | Yes |
| GET | `/api/reclamations/{refRec}` | Get by reference | Yes |
| POST | `/api/reclamations` | Create complaint | Yes |
| PUT | `/api/reclamations/{refRec}` | Update complaint | Yes |
| DELETE | `/api/reclamations/{refRec}` | Delete complaint | Yes |
| GET | `/api/reclamations/status/{codSta}` | Search by status | Yes |
| GET | `/api/reclamations/statistics/*` | Get statistics | Yes |

**See [API_DOCUMENTATION.md](API_DOCUMENTATION.md) for complete endpoint list.**

---

## 🔐 Security

### Authentication Flow
1. User sends credentials to `/api/auth/signin`
2. Server validates and returns JWT token
3. Client includes token in `Authorization: Bearer {token}` header
4. Server validates token on each request

### JWT Configuration
- **Algorithm:** HMAC SHA-512
- **Expiration:** 24 hours (86400000 ms)
- **Secret:** Configurable in `application.properties`

### Roles
- **ADMIN:** Full system access
- **AGENT:** Complaint processing and management
- **RESPONSABLE:** Team/department management
- **CLIENT:** Limited access (own complaints)
- **USER:** Basic authenticated access

### Public Endpoints
- `/api/auth/**` - Authentication endpoints
- `/api/public/**` - Public resources

### Protected Endpoints
All other endpoints require valid JWT token.

---

## 🗄️ Database Schema

### Core Tables
- **hab_users** - User accounts
- **hab_role** - Roles (ADMIN, AGENT, etc.)
- **hab_privilege** - Fine-grained permissions
- **hab_users_roles** - User-role mapping
- **hab_role_privilege** - Role-privilege mapping
- **rec_reclamation** - Complaints
- **rec_statut** - Status reference
- **rec_service** - Service reference
- **rec_objet** - Object reference
- **rec_canal** - Channel reference
- **rec_nature_client** - Complainant type reference

**Total:** 21 entities with proper JPA relationships

---

## 🧪 Testing

### With Postman
1. **Import Collection:** `Reclamation_App_Postman_Collection.json`
2. **Import Environment:** `Reclamation_Dev_Environment.json`
3. **Run Tests:** Follow sequence in collection

### Manual Testing
```bash
# Create user
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Test","lastName":"User","email":"test@test.com","password":"Test123!","roles":["USER"]}'

# Login
curl -X POST http://localhost:8080/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"Test123!"}'

# Get users (with token)
curl http://localhost:8080/api/users \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

**See [POSTMAN_TESTING_GUIDE.md](POSTMAN_TESTING_GUIDE.md) for comprehensive testing guide.**

---

## 🚢 Production Deployment

### Pre-Deployment Checklist
- [ ] Change JWT secret in `application.properties`
- [ ] Update database credentials
- [ ] Change `hibernate.ddl-auto` to `validate` or `none`
- [ ] Configure production database (connection pooling, SSL)
- [ ] Update CORS configuration for production frontend URL
- [ ] Enable HTTPS/SSL
- [ ] Set up proper logging (file-based, log rotation)
- [ ] Configure monitoring and health checks
- [ ] Set up backup strategy
- [ ] Review and apply security best practices
- [ ] Configure rate limiting
- [ ] Set up CDN for static resources (if any)

### Build for Production
```bash
.\mvnw.cmd clean package -DskipTests
```

Generates: `target/reclamation-app-0.0.1-SNAPSHOT.jar`

### Run Production Build
```bash
java -jar target/reclamation-app-0.0.1-SNAPSHOT.jar
```

### Environment Variables
```bash
export SPRING_DATASOURCE_URL=jdbc:mysql://prod-host:3306/reclamation
export SPRING_DATASOURCE_USERNAME=prod_user
export SPRING_DATASOURCE_PASSWORD=secure_password
export JWT_SECRET=very_long_and_secure_secret_key_here
export SERVER_PORT=8080
```

---

## 🐛 Known Issues & Solutions

### ✅ Fixed Issues
1. **JPA Module Entity Mapping Conflict** - Resolved (see FIX_SUMMARY.md)
2. **401 Unauthorized on /api/auth endpoints** - Resolved (shouldNotFilter added)

### Common Issues
See [QUICKSTART.md](QUICKSTART.md) for troubleshooting common startup issues.

---

## 📈 Future Enhancements

- [ ] Pagination for list endpoints
- [ ] File upload for complaint attachments
- [ ] Email notification service integration
- [ ] SMS notification service integration
- [ ] Audit logging
- [ ] API versioning
- [ ] Swagger/OpenAPI documentation
- [ ] Integration tests
- [ ] Caching (Redis)
- [ ] Export to PDF/Excel
- [ ] Real-time notifications (WebSocket)
- [ ] Multi-language support (i18n)

---

## 📄 License

[Your License Here]

---

## 👥 Authors

[Your Name/Team Here]

---

## 📞 Support

For questions or issues:
1. Check documentation files
2. Review Postman testing guide
3. Consult API documentation
4. Check known issues in FIX_SUMMARY.md

---

## 🎉 Status

✅ **Backend Complete and Operational**

- 55 Java source files compiled successfully
- 40+ REST endpoints implemented
- JWT authentication configured
- All CRUD operations functional
- Postman collection ready for testing
- Comprehensive documentation provided

**Ready for testing and deployment!**

---

**Last Updated:** June 17, 2026
**Version:** 0.0.1-SNAPSHOT
**Spring Boot:** 4.1.0
**Java:** 17
