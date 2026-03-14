#  Advanced Traffic Simulation Wrapper (SUMO)

A robust, Object-Oriented Java wrapper built around the SUMO (Simulation of Urban MObility) engine. This project bridges a heavy, C++ based traffic simulation environment with a responsive Java backend and GUI, showcasing advanced software engineering principles.

##  Engineering Highlights

Building a wrapper for a complex external engine requires more than basic coding. This project demonstrates enterprise-level Java capabilities:

* **Multi-Threading & Asynchronous Execution:** A common pitfall in simulation software is UI freezing. I engineered a multi-threaded architecture where the heavy simulation loop runs asynchronously in the background. This guarantees that the dynamic GUI remains fully responsive and updates real-time traffic data without bottlenecking the main thread.
* **Strict Object-Oriented Design (OOP):** The entire wrapper is built on solid OOP principles (Encapsulation, Abstraction, and Polymorphism). It provides a clean, modular Java API to interact with the underlying SUMO engine, making the codebase scalable and easy to maintain.
* **Custom String Parsing & Data Extraction:** SUMO generates massive amounts of raw, text-based routing data. I implemented a custom parsing logic to programmatically extract and process specific network elements (such as auxiliary edges / 'hilfskanten' prefixed with `:`) directly from the engine's output, converting raw text logs into actionable Java objects.
* **Dynamic GUI Integration:** Developed a real-time graphical interface to visualize traffic flow and engine states, tightly coupled with the backend threads for seamless data rendering.

## Tech Stack
* **Language:** Java (Advanced OOP)
* **Concurrency:** Java Multi-threading (Runnables, Background Tasks)
* **Simulation Engine:** SUMO (Simulation of Urban MObility)
* **Data Processing:** Custom Text/String Parsing Algorithms

##  About the Developer
I am Kaan Degirmenci, a Computer Science student focused on System Architecture and Backend Development. This project reflects my ability to design scalable, concurrent Java applications that interact seamlessly with complex, external software ecosystems.
