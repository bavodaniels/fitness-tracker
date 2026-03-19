# Fitness Tracker - Product Requirements Document

## Overview

A fitness tracking application designed to demonstrate agentic development capabilities. The application allows users to log their workout exercises, track performance metrics, and maintain a historical record of their training data.

## Purpose

This application serves as a demo platform for agentic development workflows, showcasing how agents can coordinate across frontend and backend implementation while maintaining clean separation of concerns.

## User Personas

- **Fitness Enthusiast**: A user who regularly performs strength training and wants to track their workout progress over time.
- **Gym Member**: Someone who uses the application to monitor their performance on key compound lifts.

## Core Features

### 1. Authentication
- **User Registration & Login**: Users can create an account and log in using a plain text username and password
- **Session Management**: Maintain user sessions during an active application session
- **Simple Auth**: Username/password authentication (no OAuth, no multi-factor auth at this stage)

### 2. Workout Logging
Users can log workouts with support for three core exercises:
- **Deadlift**
- **Back Squat**
- **Bench Press**

For each exercise logged, users must record:
- Exercise type (dropdown selection)
- Number of sets
- Number of reps
- Weight (in lbs)

### 3. Workout History
- **View Logged Workouts**: Users can view their complete workout history
- **Persistent Storage**: All workout data is persisted to PostgreSQL

## Technical Requirements

### Frontend
- **Framework**: ReactJS
- **Responsibilities**:
  - Login/registration page
  - Workout logging form
  - Workout history display
  - API communication with backend

### Backend
- **Language**: Java (JDK25)
- **Framework**: Spring Boot 4
- **Responsibilities**:
  - RESTful API endpoints for authentication and workout management
  - User session management
  - Database operations via Spring Data/JPA
  - OpenAPI/Swagger specification
  - Request validation and error handling

### Database
- **System**: PostgreSQL
- **Deployment**: Docker container
- **Responsibilities**:
  - User account storage
  - Workout history storage

## Data Model

### User Entity
- `id` (UUID, primary key)
- `username` (String, unique, not null)
- `password` (String, not null)
- `created_at` (Timestamp)

### Workout Entity
- `id` (UUID, primary key)
- `user_id` (UUID, foreign key to User)
- `exercise_type` (Enum: DEADLIFT, BACK_SQUAT, BENCH_PRESS)
- `sets` (Integer, not null)
- `reps` (Integer, not null)
- `weight` (Integer, in lbs, not null)
- `created_at` (Timestamp)

## API Specification

All API responses should follow standard REST conventions and include OpenAPI/Swagger documentation.

### Authentication Endpoints
- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Log in user
- `POST /api/auth/logout` - Log out user

### Workout Endpoints
- `POST /api/workouts` - Create a new workout log entry
- `GET /api/workouts` - Retrieve all workouts for the authenticated user
- `GET /api/workouts/{id}` - Retrieve a specific workout

### Health Check
- `GET /health` - Service health check endpoint

### Documentation
- `GET /swagger-ui.html` - Swagger UI
- `GET /v3/api-docs` - OpenAPI JSON specification

## Acceptance Criteria

### Authentication
- [ ] User can successfully register with a unique username and password
- [ ] User can log in with valid credentials
- [ ] Invalid credentials are rejected with appropriate error message
- [ ] User sessions persist across page refreshes
- [ ] User can log out

### Workout Logging
- [ ] User can log a deadlift with sets, reps, and weight
- [ ] User can log a back squat with sets, reps, and weight
- [ ] User can log a bench press with sets, reps, and weight
- [ ] Workout data is saved to PostgreSQL
- [ ] Invalid inputs are rejected with validation messages

### Workout History
- [ ] User can view all their logged workouts
- [ ] Workouts display exercise type, sets, reps, weight, and timestamp
- [ ] Workouts are sorted by most recent first
- [ ] Only the authenticated user's workouts are displayed

### Backend
- [ ] Swagger/OpenAPI documentation is accessible at `/swagger-ui.html`
- [ ] All endpoints return appropriate HTTP status codes
- [ ] Error responses include descriptive messages
- [ ] Database migrations are managed via Spring Data JPA or Flyway

### Infrastructure
- [ ] PostgreSQL runs in a Docker container
- [ ] Application can connect to PostgreSQL on startup
- [ ] Database schema is created automatically on first run

## Non-Goals
- Advanced authentication (OAuth, MFA, password reset)
- Exercise customization or additional exercise types
- Workout editing or deletion
- Mobile app
- Social features
- Analytics or progress charts
- User profiles or preferences

## Success Metrics
- Successful demonstration of agent-driven development workflow
- Clean separation between frontend and backend responsibilities
- Fully functional authentication and workout logging
- API properly documented via Swagger/OpenAPI
