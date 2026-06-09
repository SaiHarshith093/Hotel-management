# 🏨 Hotel Management System

A full-stack Hotel Management System developed using **Java, Spring Boot, Spring Security, JDBC, MySQL, Thymeleaf, and Maven**. The application helps hotel staff manage rooms, customers, bookings, food orders, billing, and revenue tracking through a secure role-based dashboard.

---

## 🚀 Features

### 🔐 Authentication & Authorization
- Secure login using Spring Security
- BCrypt password encryption
- Role-based access control
    - ADMIN
    - MANAGER
    - RECEPTIONIST

### 🏠 Room Management
- Add rooms
- Edit room details
- Delete rooms
- Room status tracking
    - Available
    - Reserved
    - Occupied
    - Maintenance

### 👥 Customer Management
- Add customers
- Update customer details
- Search customers
- Customer history tracking

### 📅 Booking Management
- Create bookings
- Cancel bookings
- Booking status tracking
    - Confirmed
    - Checked In
    - Checked Out
    - Completed
    - Cancelled

### 🚪 Check-In / Check-Out
- Room occupancy management
- Automatic room status updates
- Booking lifecycle management

### 🍽 Food Order Management
- Create food orders
- Link food orders to bookings
- Food bill calculation

### 💳 Billing System
- Generate bills
- GST calculation
- Payment tracking
- Payment methods:
    - Cash
    - Card
    - UPI
    - Online

### 📄 PDF Invoice Generation
- Download bill as PDF
- Printable invoice format

### 📊 Dashboard Analytics
- Total Rooms
- Available Rooms
- Occupied Rooms
- Total Customers
- Active Bookings
- Daily Revenue

### 📈 Charts & Reports
- Room Status Chart
- Booking Status Chart
- Revenue Trends

---

## 🛠 Tech Stack

### Backend
- Java 17
- Spring Boot
- Spring Security
- Spring JDBC

### Frontend
- Thymeleaf
- HTML5
- CSS3
- JavaScript
- Chart.js

### Database
- MySQL

### Build Tool
- Maven

### PDF Generation
- OpenPDF (iText Compatible)

### Version Control
- Git
- GitHub

---

## 📂 Project Structure

```text
src/main/java/com/hotel
│
├── config
├── controller
├── dao
├── service
├── model
├── security
├── exception
├── util
└── HotelManagementApplication
```

---

## ⚙️ Database Setup

Create a MySQL database:

```sql
CREATE DATABASE hotel_management;
```

Update `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/hotel_management
spring.datasource.username=root
spring.datasource.password=your_password

spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

---

## ▶️ Running the Project

### Clone Repository

```bash
git clone https://github.com/SaiHarshith093/Hotel-management.git
cd Hotel-management
```

### Build Project

```bash
mvn clean install
```

### Run Application

```bash
mvn spring-boot:run
```

Application will start at:

```text
http://localhost:8080
```

---

## 🔑 Default Roles

| Role | Access |
|--------|--------|
| ADMIN | Full access |
| MANAGER | Reports, Dashboard, Operations |
| RECEPTIONIST | Customer, Booking, Billing Operations |

---

## 📷 Screenshots

### Login Page
![Login Page](screenshots/login.png)
 
### Dashboard
![Dashboard](screenshots/dashboard.png)

### Food Order
![Food order](screenshots/food-orders.png)
 
### Room Management
![Rooms](screenshots/room-management.png)

### Customer Management
![Customers](screenshots/customer-management.png)
 
### Booking Management
![Bookings](screenshots/booking-management.png)
 
### Billing
![Billing](screenshots/billing.png)

### Invoice
![Invoice](screenshots/bill-details.png)
 
---

## 🔄 Booking Workflow

```text
Booking Created
       ↓
   CONFIRMED
       ↓
   CHECKED_IN
       ↓
   CHECKED_OUT
       ↓
 Bill Generated
       ↓
 Payment Received
       ↓
   COMPLETED
```

---

## 📊 Dashboard Metrics

The dashboard displays:

- Total Rooms
- Available Rooms
- Occupied Rooms
- Total Customers
- Active Bookings
- Revenue Today
- Room Status Distribution
- Booking Status Distribution
- Revenue Trends

---

## 🔒 Security Features

- Spring Security Authentication
- Session Management
- Role-Based Authorization
- Password Encryption using BCrypt
- CSRF Protection

---

## 🎯 Future Enhancements

- Docker Deployment
- Email Notifications
- Online Payment Gateway
- REST API Integration
- Room Service Module
- Inventory Management
- Customer Feedback System
- Cloud Deployment (AWS/Render/Railway)

---

## 👨‍💻 Author

**Sai Harshith**

GitHub:
https://github.com/SaiHarshith093

---

## 📄 License

This project is developed for educational and portfolio purposes.
