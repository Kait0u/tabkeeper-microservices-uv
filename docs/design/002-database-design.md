# Tabkeeper - Data Model & Database Design

This document will focus on the design of the data model and, by extension, 
the database design for each microservice.

## User Service

```
Table Name: users
Columns:
 - id: UUID, primary key
 - email: VARCHAR(320), unique, NOT NULL
 - display_name: VARCHAR(100), NOT NULL
 - status: VARCHAR(20) [ACTIVE, DISABLED], NOT NULL
 - created_ts: TIMESTAMPTZ, NOT NULL
 - updated_ts: TIMESTAMPTZ, NOT NULL
```

```
Table Name: user_credentials
Columns:
 - user_id: UUID, primary key, foreign key -> users.id, NOT NULL
 - password_hash: TEXT, NOT NULL
 - created_ts: TIMESTAMPTZ
 - updated_ts: TIMESTAMPTZ
```

The idea to split the password into a separate table is meant to improve lookups for auth.

