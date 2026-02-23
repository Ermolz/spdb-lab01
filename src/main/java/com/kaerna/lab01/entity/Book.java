package com.kaerna.lab01.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "book")
@DiscriminatorValue("BOOK")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Book extends Publication {

    private String isbn;
    private Integer pageCount;
}
