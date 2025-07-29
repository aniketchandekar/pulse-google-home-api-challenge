Pulse - AI-Powered Mood-Based Smart Home Automation
===================================================

Pulse is an Android app that combines Google Home APIs with AI-powered mood analysis to create personalized smart home automation. The app analyzes your emotional state and generates custom routines that adapt your environment to support your well-being.

Built with Google Home APIs: https://developers.home.google.com/apis

# Core Features

- **Permission API**: Authenticate your application seamlessly through Authentication API, which provides a set of standardized screens and functions to request access to structures and devices.

- **Device API**: Retrieve states of smart home devices on a structure, modify attributes, and issue commands.

- **Structure API**: Retrieve the representational graph for a structure, with rooms and assigned devices.

- **Commissioning API**: Add new matter devices to Google Home Ecosystem.

- **Automation API**: Create and schedule household routines that trigger device commands based on defined triggers and conditions.

- **Discovery API**: Retrieve a list of automations that can be created on a structure given the set of devices.

# AI-Powered Mood-Based Automation Features

## 🧠 Mood Check-In System
- **Emotion Selection**: Users can select multiple emotions from a comprehensive list
- **Thought Journaling**: Optional text input for additional context
- **Mood Analytics**: Track emotional patterns over time with visualizations
- **Journal Management**: View, edit, and manage mood check-in history

## 🤖 AI Therapy Suggestions
- **Gemini AI Integration**: Generates personalized automation suggestions based on emotions
- **Device-Aware Recommendations**: Analyzes available smart home devices to create relevant suggestions
- **Mood-Specific Environments**: Creates different lighting and temperature settings for:
  - **Anxiety Relief**: Warm, dim lighting (30%) at 2700K, comfortable temperature (22°C)
  - **Mood Boost**: Bright lighting (85%) at 5000K, slightly cool temperature (21°C)
  - **Focus Clarity**: Focused lighting (75%) at 6500K, cool temperature (20°C)
  - **Deep Relaxation**: Very dim lighting (20%) at 2200K, warm temperature (23°C)

## 🏠 Smart Home Integration
- **Multi-Device Control**: Supports lights, thermostats, color lights, and sensors
- **Automatic Triggers**: Uses motion sensors and contact sensors when available
- **Manual Fallback**: Always includes manual triggers for reliability
- **Real-Time Execution**: Immediately runs automations while creating Google Home routines

## 📊 Pulse Dashboard
- **Mood Analytics**: Visual charts showing emotion distribution and trends
- **Top Emotion Highlights**: Shows most common emotions with encouraging messages
- **Active Suggestions**: Displays AI-generated automation recommendations
- **Quick Actions**: One-tap execution of mood-based automations

## 🔄 Automation Workflow
1. **User Check-In**: Select emotions and optionally add thoughts
2. **AI Analysis**: Gemini analyzes emotional state and available devices
3. **Suggestion Generation**: Creates 3 personalized automation suggestions
4. **One-Tap Execution**: User clicks "Start" to create and run automation
5. **Google Home Integration**: Automation appears in Google Home Routines tab
6. **Multi-Device Control**: Affects multiple compatible devices simultaneously

## 🎯 Supported Device Types
- **Lights**: OnOff control with mood-specific dimming levels
- **Color Lights**: Additional color temperature control for ambiance
- **Thermostats**: Temperature adjustments based on emotional needs
- **Motion Sensors**: Automatic triggers for hands-free activation
- **Contact Sensors**: Door/window-based automation triggers
- **Switches & Outlets**: Basic OnOff control for other devices

## 💡 Technical Implementation
- **Kotlin Compose UI**: Modern Android UI with Material Design 3
- **Room Database**: Local storage for mood data and automation history
- **Google Home API**: Direct integration with Google Home ecosystem
- **Gemini AI**: Advanced natural language processing for mood analysis
- **Coroutines**: Asynchronous operations for smooth user experience
- **StateFlow**: Reactive state management for real-time updates

This sample demonstrates how AI can enhance smart home experiences by creating personalized, emotion-aware automation that adapts to users' mental and emotional well-being.

# Getting Started with Pulse

1. **Setup Google Home API**: Follow the setup guide at https://developers.home.google.com/apis
2. **Configure Gemini AI**: Add your Gemini API key to the project
3. **Install Pulse**: Build and install on your Android device
4. **Sign In**: Tap "Sign in with Google" to authenticate with your Google Home account
5. **Add Smart Devices**: Ensure you have compatible devices (lights, thermostats, sensors) in Google Home
6. **First Mood Check-In**: Navigate to Pulse tab and tap the floating "Check-in" button to start

# Architecture

- **MVVM Pattern**: Clean separation of UI, business logic, and data
- **Repository Pattern**: Centralized data access for mood and automation data
- **Service Layer**: Dedicated services for AI analysis, device discovery, and automation generation
- **Dependency Injection**: Modular architecture for testability and maintainability