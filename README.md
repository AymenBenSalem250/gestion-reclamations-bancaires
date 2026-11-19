# Gestion des réclamations bancaires

Application web full-stack réalisée dans le cadre d’un stage d’été. Elle permet de centraliser les réclamations et de suivre leur traitement.

> Projet de démonstration, non destiné à la production. N’utilisez pas de données bancaires ou personnelles réelles.

## Fonctionnalités

- Création et suivi des réclamations avec une référence unique
- Gestion des accès par rôle et par service
- Traitement des réclamations et suivi de leur statut
- Tableau de bord de suivi des délais SLA
- Notifications par e-mail et SMS
- Messagerie interne en temps réel
- Export XML pour le reporting

## Technologies

- **Frontend :** Angular 17, TypeScript
- **Backend :** Java, Spring Boot, JPA / Hibernate
- **Base de données :** MySQL
- **Communication :** API REST et WebSocket
- **Build backend :** Maven

## Organisation du dépôt

```text
frontend/   Application Angular
backend/    API Spring Boot
