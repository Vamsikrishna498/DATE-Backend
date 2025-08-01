package com.farmer.Form.Entity;

import jakarta.persistence.*;
import lombok.*;
 
@Entity
@Table(name = "states")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class State {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(nullable = false)
    private String name;
 
    // Many states belong to one country
    @ManyToOne
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;
 
    // Optional: bidirectional
    // @OneToMany(mappedBy = "state")
    // private List<District> districts;
}
