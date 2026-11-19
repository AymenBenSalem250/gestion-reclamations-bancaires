# Quick Start Guide - Reclamation App Backend

## 🚀 Get Started in 5 Minutes

---

## Step 1: Create Database (30 seconds)

Open MySQL command line:
```bash
mysql -u root -p
```

Create database:
```sql
CREATE DATABASE reclamation CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
exit;
```

---

## Step 2: Initialize Reference Data (1 minute)

Load test data:
```bash
mysql -u root -p reclamation < init_test_data.sql
```

This creates:
- ✅ 5 Roles (ADMIN, USER, AGENT, RESPONSABLE, CLIENT)
- ✅ 5 Status (NOUVEAU, EN_COURS, TRAITE, CLOTURE, ANNULE)
- ✅ 3 Canals (EMAIL, TELEPHONE, GUICHET)
- ✅ 3 Services (IT, RH, FINANCE)
- ✅ Sample objects, domains, modules

---

## Step 3: Start Application (30 seconds)

```bash
.\mvnw.cmd spring-boot:run
```

Wait for:
```
Started ReclamationAppApplication in X.XXX seconds
```

**Application URL:** `http://localhost:8080`

---

## Step 4: Test with Postman (2 minutes)

### Import Files
1. Open Postman
2. Import `Reclamation_App_Postman_Collection.json`
3. Import `Reclamation_Dev_Environment.json`
4. Select "Reclamation Dev" environment (top-right dropdown)

### Test Sequence

#### 4.1 Create Admin User
- Open: **01. Auth → Signup**
- Body (already configured):
```json
{
  "firstName": "Admin",
  "lastName": "User",
  "email": "admin@reclamation.tn",
  "password": "Admin123!",
  "roles": ["ADMIN"]
}
```
- Click **Send**
- Expected: `200 OK` - "User registered successfully"

#### 4.2 Login
- Open: **01. Auth → Login**
- Body:
```json
{
  "email": "admin@reclamation.tn",
  "password": "Admin123!"
}
```
- Click **Send**
- Expected: `200 OK` with JWT token
- ✅ Token is **automatically saved** to environment

#### 4.3 Test Protected Endpoint
- Open: **02. Users → Get All Users**
- Click **Send** (token is auto-included)
- Expected: `200 OK` with user list

#### 4.4 Create Reclamation
- Open: **03. Reclamations → Create Reclamation**
- Body (already configured):
```json
{
  "nomRec": "Dupont",
  "prenRec": "Jean",
  "emailRec": "jean.dupont@email.com",
  "gsmRec": "21612345678",
  "descRec": "Ma connexion internet ne fonctionne pas depuis hier",
  "canal": { "codCnl": "EMAIL" },
  "objet": { "codObj": "OBJ001" },
  "service": { "codSrv": "IT" },
  "natureReclamant": { "codNtr": "PART" }
}
```
- Click **Send**
- Expected: `200 OK` with auto-generated reference (RECxxxxxxxxxx)

#### 4.5 Get All Reclamations
- Open: **03. Reclamations → Get All Reclamations**
- Click **Send**
- Expected: `200 OK` with reclamation list

---

## ✅ Success!

If all steps worked, your backend is fully operational!

---

## Common Issues & Solutions

### Issue: "Connection refused" on MySQL
**Solution:** Start MySQL service
```bash
# Windows
net start MySQL80

# Or use MySQL Workbench to start server
```

### Issue: "Database 'reclamation' doesn't exist"
**Solution:** Create database (see Step 1)

### Issue: "401 Unauthorized" on protected endpoints
**Solution:** 
1. Login first (Step 4.2)
2. Check that token was saved ({{token}} variable in environment)
3. Verify "Authorization" header contains: `Bearer {{token}}`

### Issue: "Role not found" during signup
**Solution:** Run `init_test_data.sql` (Step 2)

### Issue: Port 8080 already in use
**Solution:** Stop other application or change port in `application.properties`:
```properties
server.port=8081
```

---

## What's Next?

### Test More Features
- Create users with different roles (USER, AGENT, RESPONSABLE, CLIENT)
- Update reclamation status workflow
- Add responses to reclamations
- Search reclamations by criteria
- Get statistics (count by status, date range)
- Test role management endpoints
- Test reference data endpoints

### Explore API Documentation
See **API_DOCUMENTATION.md** for:
- Complete endpoint list (40+ endpoints)
- Request/response examples
- Authentication requirements
- Error codes

### Production Deployment
Before deploying to production:
1. ⚠️ **Change JWT secret** in `application.properties`
2. ⚠️ Update database credentials
3. ⚠️ Change `hibernate.ddl-auto` to `validate`
4. ⚠️ Configure CORS for production frontend URL
5. ⚠️ Enable HTTPS
6. ⚠️ Set up proper logging

---

## File Structure Reference

```
reclamation-app/
├── src/main/java/tn/reclamationapp/
│   ├── controller/        → REST endpoints
│   ├── service/           → Business logic
│   ├── repository/        → Data access
│   ├── model/             → JPA entities
│   ├── security/          → JWT & auth
│   ├── config/            → Security config
│   └── dto/               → Data transfer objects
├── src/main/resources/
│   └── application.properties  → Configuration
├── init_test_data.sql           → Database seed
├── API_DOCUMENTATION.md         → API reference
├── POSTMAN_TESTING_GUIDE.md     → Testing guide
├── PROJECT_STATUS.md            → Project overview
└── QUICKSTART.md                → This file
```

---

## Need Help?

1. **API Details:** Read `API_DOCUMENTATION.md`
2. **Testing Guide:** Read `POSTMAN_TESTING_GUIDE.md`
3. **Project Status:** Read `PROJECT_STATUS.md`
4. **Technical Fixes:** Read `FIX_SUMMARY.md`

---

**Happy Testing! 🎉**
