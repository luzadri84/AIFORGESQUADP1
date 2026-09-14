package local.booking.space;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Sort;
@RestController @RequestMapping("/api/spaces")
public class SpaceController {
    private final SpaceRepository spaces;
    public SpaceController(SpaceRepository spaces) { this.spaces=spaces; }
    @GetMapping public List<SpaceView> list() { return spaces.findAll(Sort.by("id")).stream().map(s -> new SpaceView(s.getId(),s.getName(),s.getType(),s.getCapacity(),s.getSite())).toList(); }
    public record SpaceView(Long id,String name,SpaceType type,int capacity,String site) { }
}
