# ExpenseTrackerGUI
Java Swing + SQLite Expense Tracker with Pie Chart &amp; CSV Export
# 💰 Expense Tracker (Java + SQLite + JFreeChart)

A **simple, clean, and light-themed** expense tracking application built in **Java Swing**, using **SQLite** for storage and **JFreeChart** for visualizing data.

---

 ✨ Features
- ➕ **Add** expenses with name, category, and amount
- ❌ **Delete** selected expenses
- 🔍 **Search** by category and date
- 📊 **View totals** by category
- 🥧 **Pie chart** visualization
- 🗄 **Persistent** storage with SQLite



 📦 Requirements
- Java 8 or higher
- SQLite JDBC driver  
  *(sqlite-jdbc-3.50.3.0.jar)*
- JFreeChart library  
  *(jfreechart-1.5.4.jar, jcommon-1.0.24.jar)*



 ⚙️ How to Compile & Run
1. **Clone the repository**
   ```bash
   git clone https://github.com/Prasanna186/expense-tracker-java.git
   cd expense-tracker-java

2. **Compile**
     javac -cp ".;sqlite-jdbc-3.50.3.0.jar;jfreechart-1.5.4.jar;jcommon-1.0.24.jar" ExpenseTrackerGUI.java
3. **Run**
     java -cp ".;sqlite-jdbc-3.50.3.0.jar;jfreechart-1.5.4.jar;jcommon-1.0.24.jar" ExpenseTrackerGUI

