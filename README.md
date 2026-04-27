# 💰 PROG7313_POE_BankingApp

## 👥 Group Project By

- Neha Singh - ST10433945  
- Mofenyi Kubheka - ST10439309  
- Oarabile Marwane - ST10436124  
- Lesego Raphahla Marota - ST10448763  
- Mamaphale Leago Tema - ST10443515  

---

# 📌 Zentavo Budget Tracker

## 🧠 Overview

Zentavo is a mobile budgeting application designed to help users manage their personal finances in a structured and intuitive way.

The application allows users to track expenses, organise spending into categories, set monthly budget plans, and view financial summaries.

The goal of the system is to promote responsible financial behaviour by giving users clear visibility of their spending and financial activity.

---

## 🎯 Design Approach

The application was designed with the following key principles:

- **User-friendly interface** → simple navigation and clear layouts  
- **Consistency** → uniform colours, spacing, and structure across all screens  
- **Stability** → prevents crashes and handles incorrect input properly  
- **Clarity** → financial data is easy to read and understand  

These design choices ensure the application is both functional and easy to use.

---

## ⚙️ Core Features

### 🔐 User Authentication
Users can register and log in securely using their credentials.  
This ensures controlled access to the application.

---

### 📂 Categories
The app uses predefined categories such as:
- Groceries  
- Transport  
- Bills  
- Entertainment  
- Other  

These categories are used to organise expenses.

---

### ➕ Add Expense
Users can create an expense by entering:
- Amount  
- Date  
- Description  
- Category  

Once saved, the expense is stored in the database and updates the system immediately.

---

### 🧾 Receipt / Photo Feature (Prototype)
Users can attach a receipt indicator when adding an expense.  
This represents linking proof of purchase to transactions.

---

### 📊 Monthly Planner
Users can define a monthly budget and allocate spending limits across categories.

The system calculates:
- Allocated budget  
- Remaining balance  

---

### 🏠 Dashboard
The dashboard provides an overview of:
- Total budget  
- Amount spent  
- Remaining balance  
- Spending insights  

All values update dynamically.

---

### 📉 Finance Screen
Displays:
- Spending summaries  
- Category totals  
- Charts / visualisations  

This helps users analyse spending behaviour.

---

### 📜 Expense History
Users can:
- View all expenses  
- Edit entries  
- Delete entries  

---

### 📊 Category Totals
The system calculates total spending per category, allowing users to understand spending distribution.

---

### 🏆 Gamification
Displays user engagement and budgeting progress to encourage better financial habits.

---

### ⚙️ Settings
Provides a central place for app-related options and completes the app structure.

---

## 💾 Data Storage

All data is stored locally using a Room (SQLite) database.

This ensures:
- Data persistence  
- Offline functionality  
- Data remains after restarting the app  

---

## 🧪 Logging & Code

- Code includes comments explaining core logic  
- Logging is used to track key actions such as:
  - Login  
  - Expense creation  
  - Data retrieval  

---

## 🖼️ Screenshots

All screenshots are stored in the `screenshots` folder in this repository.

---

### 🔐 Login Screen  
[View Full Image](screenshots/login.png)  
![Login Screen](screenshots/login.png)

---

### 🏠 Dashboard  
[View Full Image](screenshots/dashboard.png)  
![Dashboard](screenshots/dashboard.png)

---

### 📂 Category Screen  
[View Full Image](screenshots/category.png)  
![Category](screenshots/category.png)

---

### ➕ Add Expense  
[View Full Image](screenshots/add_expense.png)  
![Add Expense](screenshots/add_expense.png)

---

### 🧾 Expense with Receipt  
[View Full Image](screenshots/photo.png)  
![Expense with Photo](screenshots/photo.png)

---

### 📊 Monthly Planner  
[View Full Image](screenshots/planner.png)  
![Planner](screenshots/planner.png)

---

### 📜 Expense History  
[View Full Image](screenshots/expense_history.png)  
![Expense History](screenshots/expense_history.png)

---

### 📈 Category Totals  
[View Full Image](screenshots/category_totals.png)  
![Category Totals](screenshots/category_totals.png)

---

### 🏆 Gamification  
[View Full Image](screenshots/gamification.png)  
![Gamification](screenshots/gamification.png)

---

### ⚙️ Settings  
[View Full Image](screenshots/settings.png)  
![Settings](screenshots/settings.png)

---

## 🔗 GitHub Usage

This project is managed using GitHub for version control.

The repository includes:
- Full Android project  
- Kotlin source code  
- Layout files  
- Resources  
- Documentation  

---

## ▶️ How to Run the App

1. Clone or download the repository  
2. Open in Android Studio  
3. Allow Gradle to sync  
4. Run on emulator or Android device  
5. Log in or register  

---

## 📱 APK File

The APK file is included in the final submission and can be installed directly.

---

## 🎥 Demo Video

👉 

---

## 📦 Submission Includes

- Full source code  
- README documentation  
- Screenshots  
- Demo video  
- APK file  
- Part 1 document  

---

## 📝 Final Notes

Zentavo demonstrates a functional budgeting application with core financial tracking features.

The system includes authentication, expense tracking, budgeting, financial summaries, and local data storage, all implemented in a structured and user-friendly way.

The application runs in the emulator and meets the requirements for the Part 2 App Prototype Development submission.
