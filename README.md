# Homeless Support System

## Project Overview
The Homeless Support System is a comprehensive and compassionate initiative designed to streamline and enhance the support mechanisms for the homeless population. The primary objective of this project is to create an efficient, technology-driven system that facilitates collaboration among donors, shelters, and staff members to provide effective aid to those in need.

The system is designed to tackle homelessness by looking at shelter occupancy, funding, and inspections. We aim to understand which shelters have the most variable usage and which ones are almost full. We also want to identify shelters that need more funding based on their capacity. Additionally, we're checking how much each donor contributes over time and creating inspection schedules for staff members. The goal is to build a responsive and data-driven system to support those facing homelessness.

## Steps to Run the Project

### Step 1: Set up the Database
- Run the SQL script at `docs and files` -> `Database Design and ERD` -> `SQL.sql` to set up the database.

### Step 2: Configure Database Connection
- Add the database URL, username, and password in the `credentials.prop` file.

### Step 3: Add MySQL Connector JAR
- Include the MySQL connector JAR module in the project.

## Folder Structure

```bash
.
├── .gitignore
├── credentials.prop
├── output.txt
├── docs and files
│   ├── Class Design and UML
│   │   ├── Final UML.png
│   │   └── Initial UML.png
│   └── Database Design and ERD
│       ├── ERD.mwb
│       ├── ERD.pdf
│       └── SQL.sql
├── lib
├── out
└── src
    ├── Constants.java
    ├── DatabaseManager.java
    ├── DataGenerator.java
    ├── DataManipulator.java
    ├── DonationRecord.java
    ├── Donor.java
    ├── FundsDisbursement.java
    ├── HelperMethod.java
    ├── HomelessSupport.java
    ├── Locatable.java
    ├── Main.java
    ├── OccupancyRecord.java
    ├── Pair.java
    ├── Point.java
    ├── Service.java
    ├── Shelter.java
    ├── Staff.java
├── test
```
## Further Reading 
If you want to learn more about the project including class design, database design, design principles,  test cases, files and external data, data structures and their relation to each other, assumptions and choices made, key algorithms and design elements, and limitations of the project you can refer this [report](Bhishman_Desai__B00945177.pdf).

## Contributing
Contributions to enhance the game's functionality or address any issues are welcome. Feel free to use the provided source code as a reference for creating similar applications for your institution.

## License
This project is licensed under the [MIT License](LICENSE).