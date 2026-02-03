package com.empresa.demo.model;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Table(name = "tickets")
@Data
public class TicketEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String description;
    @Column(name = "suggestion_ia")
    private String suggestion;
}
