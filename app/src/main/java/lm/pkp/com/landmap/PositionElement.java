package lm.pkp.com.landmap;

/**
 * Created by USER on 10/16/2017.
 */
public class PositionElement {

    private Long id;
    private Integer areaId;
    private String name;
    private String description;
    private double lat;
    private double lon;
    private String tags;
    private Integer viewPos;

    public Integer getAreaId() {
        return areaId;
    }

    public void setAreaId(Integer areaId) {
        this.areaId = areaId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getViewPos() {
        return viewPos;
    }

    public void setViewPos(Integer viewPos) {
        this.viewPos = viewPos;
    }

    public boolean isPositionValid(){
        if(lat != 0.0 && lon != 0.0){
            return true;
        }else {
            return false;
        }
    }
}
