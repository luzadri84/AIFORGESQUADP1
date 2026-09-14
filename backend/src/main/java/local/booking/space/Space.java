package local.booking.space;

import jakarta.persistence.*;

@Entity
@Table(name = "BKG_SPACE")
public class Space {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "space_ids")
    @SequenceGenerator(name = "space_ids", sequenceName = "BKG_SPACE_SEQ", allocationSize = 1)
    private Long id;
    @Column(nullable = false, length = 120)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(name = "SPACE_TYPE", nullable = false, length = 20)
    private SpaceType type;
    @Column(nullable = false)
    private int capacity;
    @Column(name = "SITE", nullable = false, length = 160)
    private String site;

    protected Space() { }
    public Space(String name, SpaceType type, int capacity, String site) {
        this.name = name; this.type = type; this.capacity = capacity; this.site = site;
    }
    public Long getId() { return id; }
    public String getName() { return name; }
    public SpaceType getType() { return type; }
    public int getCapacity() { return capacity; }
    public String getSite() { return site; }
}
