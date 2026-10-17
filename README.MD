# Tabkeeper

Tabkeeper: an expense splitting application. A credit project for the Microservices class, at VIZJA University (2026/2027).

## General

This project consists of 4 microservices and a gateway. It assumes a hybrid approach, 
where some aspects of the communications, like on-demand data fetching, are synchronous, 
whereas others (e.g. notifications) are asynchronous and event-driven.

## Tech Stack

* Java 25
* Spring Boot 4
* Spring Cloud
* PostgreSQL
* RabbitMQ