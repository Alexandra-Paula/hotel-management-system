# Lab 2 - Creational Design Patterns

## Author: Manea Alexandra-Paula, Group FI-231

## Objectives

* Study Factory Method and Abstract Factory design patterns;
* Implement creational patterns in a chosen domain;
* Improve modularity and code flexibility.

## Used Design Patterns

* **Factory Method** – Defines an interface for creating objects but allows subclasses to decide which class to instantiate.
* **Abstract Factory** – Provides an interface for creating families of related objects without specifying concrete classes.

## Implementation

This project implements a **hotel reservation management system** that allows users to choose between simple room reservation or complete reservation packages.

The system uses two creational design patterns:

- Factory Method is used to create different room types (Standard, Deluxe, Suite).
- Abstract Factory is used to create reservation packages containing compatible rooms and extra services.

The architecture reduces coupling and improves scalability.

### Factory Method

The Factory Method pattern is implemented using the `RoomFactory` interface.

Concrete factories such as `StandardRoomFactory`, `DeluxeRoomFactory`, and `SuiteRoomFactory` generate specific room objects.

```java
public interface RoomFactory {
    Room createRoom();
}

public class DeluxeRoomFactory implements RoomFactory {
    public Room createRoom() {
        return new DeluxeRoom();
    }
}
```

### Abstract Factory

The Abstract Factory pattern is implemented through the ReservationPackageFactory interface.

Each concrete factory creates a room and its compatible extra services.

```java
public interface ReservationPackageFactory {
    Room createRoom();
    ExtraService[] createExtraServices();
}
```

### Results

The user can choose between simple reservation or complete package reservation.

Created objects:
Standard Package - StandardRoom + AirportTransfer
Deluxe Package - DeluxeRoom + SpaAccess + AirportTransfer
Suite Package - SuiteRoom + SpaAccess + RoomService + AirportTransfer

### Conclusions
The project demonstrates the practical use of Factory Method and Abstract Factory patterns for building flexible and maintainable software systems.