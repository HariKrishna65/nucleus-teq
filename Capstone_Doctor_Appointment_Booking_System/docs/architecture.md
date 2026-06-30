# Architecture Overview

## System Goals
- Allow patients to find doctors and book appointments online
- Allow doctors to manage availability and appointments
- Allow admins to monitor platform activity

## Proposed Architecture
Frontend React app
  ├─> Backend API
  │    ├─ User Service
  │    └─ Appointment Service
  └─> MongoDB

- React frontend communicates with backend APIs
- FastAPI backend handles authentication, user management, and appointment workflows
- MongoDB stores users, doctor profiles, and appointment data
- JWT protects authentication and role-based access

## Backend Modules
- `backend/routers/`: separate API route files for auth and appointment workflows
- `backend/services/`: shared business logic and data access helpers
- `backend/database.py`: centralized MongoDB connection configuration

## Frontend Modules
- `src/components/`: reusable UI components
- `src/pages/`: login, register, doctor list, booking, doctor dashboard, admin dashboard
- `src/services/`: API integration utilities
- `src/context/`: auth state and app context

## Security Plan
- JWT-based authentication
- Role-based access for patients, doctors, and admins
- Password hashing and protected routes

## Next Implementation Milestones
1. Initialize backend and frontend projects
2. Create authentication flow
3. Build doctor and appointment models
4. Connect the UI to backend APIs
5. Add testing and deployment documentation
