package net.jasonly027.steamsalebot.util.database.pojos;

import com.mongodb.client.model.Filters;
import net.jasonly027.steamsalebot.util.database.Database;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.Objects;

public class JunctionPojo implements IToFilter {
    // When creating your own object, DO NOT assign to this id. Keep it null.
    public ObjectId id;

    @BsonProperty(value = Database.APP_ID)
    public long appId;

    @BsonProperty(value = Database.SERVER_ID)
    public long serverId;

    @BsonProperty(value = Database.IS_TRAILING_SALE_DAY)
    public boolean isTrailingSaleDay;

    // Necessary for POJO codec
    public JunctionPojo() {}

    public JunctionPojo(long appId, long serverId) {
        this(appId, serverId, false);
    }

    public JunctionPojo(long appId, long serverId, boolean isTrailingSaleDay) {
        this.appId = appId;
        this.serverId = serverId;
        this.isTrailingSaleDay = isTrailingSaleDay;
    }

    @Override
    public Bson toFilter() {
        return Filters.and(
                Filters.eq(Database.APP_ID, appId),
                Filters.eq(Database.SERVER_ID, serverId),
                Filters.eq(Database.IS_TRAILING_SALE_DAY, isTrailingSaleDay)
        );
    }

    @Override
    public String toString() {
        return "JunctionPojo{" +
                "id=" + id +
                ", appId=" + appId +
                ", serverId=" + serverId +
                ", isTrailingSaleDay=" + isTrailingSaleDay +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JunctionPojo that = (JunctionPojo) o;
        return appId == that.appId && serverId == that.serverId && isTrailingSaleDay == that.isTrailingSaleDay && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, appId, serverId, isTrailingSaleDay);
    }
}
