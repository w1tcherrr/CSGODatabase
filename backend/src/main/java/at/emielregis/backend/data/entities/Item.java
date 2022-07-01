package at.emielregis.backend.data.entities;

import at.emielregis.backend.data.enums.Exterior;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private ClassID classID;

    @Column(nullable = false, updatable = false)
    private int amount;

    @Column(updatable = false)
    private String nameTag;

    @Column(nullable = false, updatable = false)
    private boolean tradable;

    @Column(nullable = false, updatable = false)
    private boolean statTrak;

    @Column(nullable = false, updatable = false)
    private boolean souvenir;

    @ManyToOne(optional = false)
    private ItemName name;

    @ManyToMany
    private List<Sticker> stickers;

    @Column(updatable = false)
    private Exterior exterior;

    @ManyToOne(optional = false)
    private ItemCategory category;
}
