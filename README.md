Kitchen Inventory System with item records, recipes, categories, purchases/orders, and reminders.
Features the use of Unstract API to scan receipts.

In application.properties for the Spring backend, make sure the database connection is set up. The backend should automatically create schemas based on the X-Tenant-ID header.
The Unstract API key also goes there.

In the frontend under /app/ the config.tsx file controls the backend location and the tenant ID. A TenantID can be any string that is a valid PostgreSQL schema name. A complex string is recommended. 

Frontend requires Node. All other main packages should be in the package.xml file for installtion. Run `npx expo start` to access the local dev server.
Backend works with Java SE 17 and Maven. All other main packages should be in the pom.xml The local server can be started with 
