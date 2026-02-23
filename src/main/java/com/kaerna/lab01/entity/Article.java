package com.kaerna.lab01.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "article")
@DiscriminatorValue("ARTICLE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Article extends Publication {

    private String journal;
    private Integer volume;
}
