# Railway Deployment Guide for Shnoor Trade Management

## Quick Deploy (1 minute)

### Step 1: Go to Railway.app
1. Visit https://railway.app
2. Sign up with GitHub (easiest)

### Step 2: Create New Project
- Click "Create New Project"
- Select "Deploy from GitHub"
- Connect your GitHub account
- Fork/upload this repo to GitHub

### Step 3: Add PostgreSQL Database
1. Click "Add Service" in your Railway project
2. Select "PostgreSQL"
3. Railway will auto-create the database

### Step 4: Set Environment Variables
In your Railway project, go to **Variables** and add:
```
DATABASE_URL=postgresql://user:password@host:5432/railway
DB_USERNAME=postgres
DB_PASSWORD=<generated-password>
```

(Railway auto-generates these — copy from the PostgreSQL service)

### Step 5: Deploy
1. Push this code to your GitHub repo (or let Railway auto-deploy)
2. Railway automatically builds with Maven
3. Your app deploys in ~2-3 minutes

### Step 6: Access Your App
- Railway gives you a public URL
- Your app runs on that URL

---

## Manual Build (if needed)

```bash
mvn clean package -DskipTests
java -jar target/shnoor-trade-management.jar
```

---

## Database Setup

The SQL schema is in `sql/schema.sql`. Railway's PostgreSQL will auto-create the database, but you may need to manually run:

```sql
-- Connect to your Railway PostgreSQL
-- Run the SQL from sql/schema.sql
```

---

## Files I Modified for Deployment

1. **DBConnection.java** — Now reads `DATABASE_URL`, `DB_USERNAME`, `DB_PASSWORD` from environment variables
2. **Procfile** — Tells Railway how to run the app
3. **railway.json** — Railway configuration

---

## Troubleshooting

- **App crashes on startup?** Check logs in Railway dashboard → Logs tab
- **Database connection error?** Verify environment variables match your PostgreSQL credentials
- **Port issues?** Railway auto-assigns a port (usually `8080`)

---

## Your Deployment Link
After deploying, you'll get a URL like:
```
https://your-project-name.up.railway.app
```

This is your live app!
