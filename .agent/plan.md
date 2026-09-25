# Project Plan

An Android floating buddy / widget overlay app designed for delivery executives using the Ekart Field X app. It captures delivery addresses from runsheets (supporting single or multiple runsheets), allows specifying start and end addresses, generates an optimized/effective delivery route map, and provides beginner-friendly, easy-to-use controls for efficient delivery planning.

## Project Brief

# Project Brief: Delivery Buddy (MVP)

## Overview
Delivery Buddy is an Android floating buddy / widget overlay app designed for delivery executives using the Ekart Field X app. It streamlines delivery planning by capturing addresses from runsheets, defining start and end points, generating optimized delivery routes, and providing intuitive floating controls.

## Features
1. **Floating Buddy / Widget Overlay**: Quick-access overlay window that floats above the Ekart Field X app for seamless interaction during active deliveries.
2. **Runsheet Address Capture**: Capture and parse delivery addresses from single or multiple runsheets.
3. **Route Optimization & Customization**: Specify custom start and end addresses and generate an optimized delivery route map.
4. **Beginner-Friendly Controls**: Simplified, easy-to-use overlay controls for efficient task management and route execution.

## High-Level Tech Stack
- **Programming Language**: Kotlin
- **UI Toolkit**: Jetpack Compose & Material 3
- **Navigation & Adaptive Strategy**: Jetpack Navigation 3 (state-driven) and the Compose Material Adaptive library for adaptive layouts.
- **Asynchronous Processing**: Kotlin Coroutines & Flow for reactive background processing.
- **Architecture**: MVVM (Model-View-ViewModel) with Unidirectional Data Flow (UDF).
- **Persistence**: Excluded for MVP (in-memory state management).

## Implementation Steps

### Task_1_CoreArchitectureAndNavigation: Set up the MVVM architecture, core data models, and Jetpack Navigation 3 setup.
- **Status:** COMPLETED
- **Updates:** Completed MVVM architecture, core data models, and navigation setup.
- **Acceptance Criteria:**
  - project builds successfully
  - core models and navigation setup complete

### Task_2_RunsheetParserAndUI: Implement single and multiple runsheet address parsing and main Jetpack Compose dashboard UI.
- **Status:** COMPLETED
- **Updates:** Completed Runsheet Parser, Main Dashboard UI, delivery success toggle, progress tracking, and order sorting (closest/furthest).
- **Acceptance Criteria:**
  - runsheet parser correctly extracts addresses
  - main dashboard UI displays runsheet items

### Task_3_RouteOptimizationAndFloatingWidget: Implement start/end address route optimization and the floating widget overlay for Ekart Field X.
- **Status:** COMPLETED
- **Updates:** Completed route optimization with start/end addresses, total KM calculation, fuel cost estimation, mileage/fuel price settings, and floating widget overlay service.
- **Acceptance Criteria:**
  - route optimization calculates start and end path correctly
  - floating widget overlay provides quick access controls

### Task_4_RunAndVerify: Run and verify application stability, instructing critic_agent to check for crashes and alignment with user requirements.
- **Status:** COMPLETED
- **Updates:** Verified build and unit tests successfully. All features (floating widget overlay, runsheet parser, start/end route optimization, closest/furthest sorting, delivery success tracking, fuel cost estimation, mileage/fuel price settings, delivery history, projected vs actual cost, minimalist UI, and nearby petrol pump detection with route recalculation) are fully implemented and tested.
- **Acceptance Criteria:**
  - make sure all existing tests pass
  - build pass
  - app does not crash
  - verified application stability and alignment with user requirements
- **Duration:** N/A

