# Doctor Appointment Booking System

## Overview
This project is a full-stack doctor appointment booking platform built with FastAPI for the backend and React for the frontend.

## Project Structure
- backend/: FastAPI application, API routes, schemas, services, and tests
- frontend/: React single-page application
- docs/: Architecture notes and implementation documentation

## Architecture Summary
- Frontend: React SPA with routing and API integration
- Backend: Two FastAPI microservices
  - User Service: authentication, user registration, role handling
  - Appointment Service: doctor management, slots, appointments, booking flow
- Authentication: JWT-based auth with role-based access control
- Database: MongoDB with async access patterns
- Messaging/Integration: REST APIs between services

## Getting Started
### Backend
1. cd backend
2. python -m venv .venv
3. .venv\Scripts\activate
4. pip install -r requirements.txt
5. uvicorn main:app --reload

### Frontend
1. cd frontend
2. npm install
3. npm run dev

## Next Steps
- Define user roles and data models
- Implement authentication endpoints
- Build doctor search and appointment booking flow
- Add tests and documentation
