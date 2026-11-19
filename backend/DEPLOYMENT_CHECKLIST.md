# Deployment Checklist - Reclamation App

Complete checklist for testing and deploying your reclamation management backend.

---

## 📋 Development Environment Setup

### ✅ Prerequisites Verification
- [ ] Java 17 installed
  ```bash
  java -version
  # Expected: openjdk version "17.x.x"
  ```
- [ ] MySQL 8.0 running
  ```bash
  mysql --version
  # Expected: mysql Ver 8.0.x
  ```
- [ ] Maven available (wrapper included)
  ```bash
  .\mvnw.cmd --version
  ```

### ✅ Database Setup
- [ ] MySQL service is running
- [ ] Database `reclamation` created
  ```sql
  CREATE DATABASE reclamation CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
  ```
- [ ] Test data loaded
  ```bash
  mysql -u root -p reclamation < init_test_data.sql
  ```
- [ ] Verify roles exist
  ```sql
  USE reclamation;
  SELECT * FROM hab_role;
  # Expected: 5 roles (ADMIN, USER, AGENT, RESPONSABLE, CLIENT)
  ```
- [ ] Verify reference data loaded
  ```sql
  SELECT * FROM rec_statut;
  SELECT * FROM rec_canal;
  SELECT * FROM rec_service;
  ```

### ✅ Application Configuration
- [ ] `application.properties` reviewed
- [ ] Database URL correct
  ```properties
  spring.datasource.url=jdbc:mysql://localhost:3306/reclamation
  ```
- [ ] Database credentials set
  ```properties
  spring.datasource.username=root
  spring.datasource.password=YOUR_PASSWORD
  ```
- [ ] JWT secret configured (change for production!)
  ```properties
  app.jwt.secret=VerySecretKeyForJWTTokenGenerationPleaseChangeInProduction123456789
  ```

### ✅ Build Verification
- [ ] Project compiles without errors
  ```bash
  .\mvnw.cmd clean compile
  # Expected: BUILD SUCCESS
  ```
- [ ] No compilation warnings
- [ ] All dependencies resolved

---

## 🚀 Application Startup

### ✅ First Launch
- [ ] Start application
  ```bash
  .\mvnw.cmd spring-boot:run
  ```
- [ ] Check console for errors
- [ ] Verify successful startup message
  ```
  Started ReclamationAppApplication in X.XXX seconds
  ```
- [ ] Application accessible at `http://localhost:8080`
- [ ] No errors in console output
- [ ] Database tables auto-created by Hibernate

### ✅ Verify Tables Created
```sql
USE reclamation;
SHOW TABLES;
```

Expected tables:
- [ ] hab_users
- [ ] hab_role
- [ ] hab_privilege
- [ ] hab_users_roles
- [ ] hab_role_privilege
- [ ] rec_reclamation
- [ ] rec_reclamation_dt
- [ ] rec_reclamation_generated
- [ ] rec_statut
- [ ] rec_service
- [ ] rec_objet
- [ ] rec_canal
- [ ] rec_nature_client
- [ ] rec_domaine
- [ ] rec_module
- [ ] rec_menu
- [ ] rec_path
- [ ] rec_param_gen
- [ ] rec_person
- [ ] rec_rubrique_xml

---

## 🧪 Postman Testing

### ✅ Postman Setup
- [ ] Postman installed
- [ ] Collection imported: `Reclamation_App_Postman_Collection.json`
- [ ] Environment imported: `Reclamation_Dev_Environment.json`
- [ ] "Reclamation Dev" environment selected (top-right dropdown)
- [ ] Variables verified:
  - `baseUrl`: http://localhost:8080
  - `token`: (empty initially, auto-filled after login)

### ✅ Authentication Tests
- [ ] **Test 1: Signup ADMIN**
  - Endpoint: `POST /api/auth/signup`
  - Body: Create admin user
  - Expected: `200 OK` - "User registered successfully"
  - ✅ PASS / ❌ FAIL: ___________

- [ ] **Test 2: Login ADMIN**
  - Endpoint: `POST /api/auth/signin`
  - Body: admin credentials
  - Expected: `200 OK` with JWT token
  - Token auto-saved to environment
  - ✅ PASS / ❌ FAIL: ___________

- [ ] **Test 3: Signup Regular User**
  - Endpoint: `POST /api/auth/signup`
  - Body: Create regular user with role "USER"
  - Expected: `200 OK`
  - ✅ PASS / ❌ FAIL: ___________

- [ ] **Test 4: Login Regular User**
  - Endpoint: `POST /api/auth/signin`
  - Expected: `200 OK` with JWT token
  - ✅ PASS / ❌ FAIL: ___________

- [ ] **Test 5: Invalid Login**
  - Endpoint: `POST /api/auth/signin`
  - Body: Wrong password
  - Expected: `401 Unauthorized`
  - ✅ PASS / ❌ FAIL: ___________

### ✅ User Management Tests (as ADMIN)
- [ ] **Test 6: Get All Users**
  - Endpoint: `GET /api/users`
  - Expected: `200 OK` with user list
  - ✅ PASS / ❌ FAIL: ___________

- [ ] **Test 7: Get User by ID**
  - Endpoint: `GET /api/users/{id}`
  - Expected: `200 OK` with user details
  - ✅ PASS / ❌ FAIL: ___________

- [ ] **Test 8: Get User by Email**
  - Endpoint: `GET /api/users/email/{email}`
  - Expected: `200 OK`
  - ✅ PASS / ❌ FAIL: ___________

- [ ] **Test 9: Update User**
  - Endpoint: `PUT /api/users/{id}`
  - Expected: `200 OK`
  - ✅ PASS / ❌ FAIL: ___________

- [ ] **Test 10: Assign Role**
  - Endpoint: `PUT /api/users/{id}/role?roleName=AGENT`
  - Expected: `200 OK`
  - ✅ PASS / ❌ FAIL: ___________

- [ ] **Test 11: Disable User**
  - Endpoint: `PUT /api/users/{id}/disable`
  - Expected: `200 OK`
  - ✅ PASS / ❌ FAIL: ___________

- [ ] **Test 12: Enable User**
  - Endpoint: `PUT /api/users/{id}/enable`
  - Expected: `200 OK`
  - ✅ PASS / ❌ FAIL: ___________

### ✅ Reclamation Tests
- [ ] **Test 13: Create Reclamation**
  - Endpoint: `POST /api/reclamations`
  - Body: Complete reclamation data
  - Expected: `200 OK` with auto-generated refRec
  - Note reference: ___________
  - ✅ PASS / ❌ FAIL: ___________

- [ ] **Test 14: Get All Reclamations**
  - Endpoint: `GET /api/reclamations`
  - Expected: `200 OK` with list
  - ✅ PASS / ❌ FAIL: ___________

- [ ] **Test 15: Get Reclamation by Reference**
  - Endpoint: `GET /api/reclamations/{refRec}`
  - Expected: `200 OK`
  - ✅ PASS / ❌ FAIL: ___________

- [ ] **Test 16: Update Reclamation**
  - Endpoint: `PUT /api/reclamations/{refRec}`
  - Expected: `200 OK`
  - ✅ PASS / ❌ FAIL: ___________

- [ ] **Test 17: Update Status**
  - Endpoint: `PUT /api/reclamations/{refRec}/status?codSta=EN_COURS`
  - Expected: `200 OK`
  - ✅ PASS / ❌ FAIL: ___________

- [ ] **Test 18: Add Response**
  - Endpoint: `POST /api/reclamations/{refRec}/response`
  - Expected: `200 OK`
  - ✅ PASS / ❌ FAIL: ___________

- [ ] **Test 19: Search by Status**
  - Endpoint: `GET /api/reclamations/status/NOUVEAU`
  - Expected: `200 OK` with filtered list
  - ✅ PASS / ❌ FAIL: ___________

- [ ] **Test 20: Search by Email**
  - Endpoint: `GET /api/reclamations/email/{email}`
  - Expected: `200 OK`
  - ✅ PASS / ❌ FAIL: ___________

- [ ] **Test 21: Statistics by Status**
  - Endpoint: `GET /api/reclamations/statistics/status?codSta=NOUVEAU`
  - Expected: `200 OK` with count
  - ✅ PASS / ❌ FAIL: ___________

### ✅ Reference Data Tests
- [ ] **Test 22: Get All Status**
  - Endpoint: `GET /api/reference/statuts`
  - Expected: `200 OK` with 5 status
  - ✅ PASS / ❌ FAIL: ___________

- [ ] **Test 23: Get All Services**
  - Endpoint: `GET /api/reference/services`
  - Expected: `200 OK`
  - ✅ PASS / ❌ FAIL: ___________

- [ ] **Test 24: Get All Objects**
  - Endpoint: `GET /api/reference/objets`
  - Expected: `200 OK`
  - ✅ PASS / ❌ FAIL: ___________

- [ ] **Test 25: Get All Canals**
  - Endpoint: `GET /api/reference/canals`
  - Expected: `200 OK` with 3 canals
  - ✅ PASS / ❌ FAIL: ___________

### ✅ Role Management Tests (as ADMIN)
- [ ] **Test 26: Get All Roles**
  - Endpoint: `GET /api/roles`
  - Expected: `200 OK` with 5 roles
  - ✅ PASS / ❌ FAIL: ___________

- [ ] **Test 27: Create Privilege**
  - Endpoint: `POST /api/roles/privileges`
  - Expected: `200 OK`
  - ✅ PASS / ❌ FAIL: ___________

- [ ] **Test 28: Assign Privilege to Role**
  - Endpoint: `POST /api/roles/{roleId}/privileges/{privilegeId}`
  - Expected: `200 OK`
  - ✅ PASS / ❌ FAIL: ___________

### ✅ Authorization Tests
- [ ] **Test 29: Access Protected Endpoint without Token**
  - Remove Authorization header
  - Endpoint: `GET /api/users`
  - Expected: `401 Unauthorized`
  - ✅ PASS / ❌ FAIL: ___________

- [ ] **Test 30: Access Admin Endpoint as Regular User**
  - Login as USER role
  - Endpoint: `DELETE /api/users/{id}`
  - Expected: `403 Forbidden`
  - ✅ PASS / ❌ FAIL: ___________

---

## 📊 Test Summary

**Total Tests:** 30
**Passed:** _____ / 30
**Failed:** _____ / 30
**Pass Rate:** _____ %

**Issues Found:**
1. _______________________________________
2. _______________________________________
3. _______________________________________

---

## 🔍 Performance Verification

### ✅ Response Time Checks
- [ ] Login response < 500ms
- [ ] Get all reclamations < 1000ms
- [ ] Create reclamation < 500ms
- [ ] Search operations < 1000ms

### ✅ Database Performance
- [ ] Queries are optimized (check console SQL logs)
- [ ] No N+1 query issues
- [ ] Proper indexes exist on foreign keys

---

## 🔒 Security Verification

### ✅ Security Checks
- [ ] JWT token required for protected endpoints
- [ ] Token expiration works (24 hours)
- [ ] Invalid tokens rejected
- [ ] Password stored as BCrypt hash (not plaintext)
- [ ] CORS configured correctly
- [ ] SQL injection prevented (parameterized queries via JPA)
- [ ] XSS prevented (proper JSON encoding)

### ✅ Verify in Database
```sql
-- Check password is hashed
SELECT email, password FROM hab_users;
-- Password should start with $2a$ (BCrypt)
```

---

## 🚢 Production Deployment Checklist

### ✅ Pre-Production Configuration
- [ ] JWT secret changed to production value
  ```properties
  app.jwt.secret=GENERATE_LONG_RANDOM_SECRET_HERE
  ```
- [ ] Database credentials updated
  ```properties
  spring.datasource.username=prod_user
  spring.datasource.password=SECURE_PASSWORD
  ```
- [ ] Hibernate DDL auto changed
  ```properties
  spring.jpa.hibernate.ddl-auto=validate
  # or: none
  ```
- [ ] CORS updated for production frontend
  ```java
  // In SecurityConfig.java
  .allowedOrigins("https://your-production-domain.com")
  ```
- [ ] Logging configured for production
  ```properties
  logging.level.root=WARN
  logging.level.tn.reclamationapp=INFO
  logging.file.name=logs/reclamation-app.log
  ```

### ✅ Build for Production
- [ ] Clean build successful
  ```bash
  .\mvnw.cmd clean package -DskipTests
  ```
- [ ] JAR file created in `target/` folder
- [ ] JAR file size reasonable (< 100MB)

### ✅ Production Environment
- [ ] Production database created and accessible
- [ ] Reference data loaded in production DB
- [ ] Database backups configured
- [ ] SSL/HTTPS configured
- [ ] Firewall rules configured
- [ ] Monitoring and alerting set up
- [ ] Log aggregation configured
- [ ] Health check endpoint accessible

### ✅ Deployment
- [ ] JAR deployed to server
- [ ] Environment variables configured
- [ ] Application starts successfully
- [ ] Health check passes
- [ ] Basic smoke test passes (login, create reclamation)
- [ ] Production URL accessible
- [ ] HTTPS working

### ✅ Post-Deployment
- [ ] Monitor application logs for errors
- [ ] Check database connections
- [ ] Verify API response times
- [ ] Test critical workflows
- [ ] Document any issues
- [ ] Create rollback plan

---

## 📋 Documentation Review

### ✅ Documentation Completeness
- [ ] README.md reviewed
- [ ] QUICKSTART.md followed successfully
- [ ] API_DOCUMENTATION.md accurate
- [ ] POSTMAN_TESTING_GUIDE.md complete
- [ ] PROJECT_STATUS.md up to date
- [ ] All endpoints documented
- [ ] Example requests/responses provided

---

## ✅ Final Sign-Off

**Completed By:** ___________________
**Date:** ___________________
**Environment:** ☐ Development  ☐ Staging  ☐ Production

**Status:** ☐ Ready for Deployment  ☐ Issues Found (see below)

**Notes:**
_______________________________________
_______________________________________
_______________________________________

**Approval:**
_______________________________________
Signature & Date

---

## 📞 Support Contacts

**Development Issues:**
- Review: API_DOCUMENTATION.md
- Check: POSTMAN_TESTING_GUIDE.md

**Deployment Issues:**
- Review: PROJECT_STATUS.md
- Check: Application logs in console or `logs/` folder

**Database Issues:**
- Verify: init_test_data.sql executed
- Check: Database connection in application.properties

---

**Last Updated:** June 17, 2026
