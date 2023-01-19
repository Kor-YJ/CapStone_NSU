package togethers.togethers.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class Smoking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false,unique = true)
    private Long smoking_id;

    private Integer smoking_value;
}
