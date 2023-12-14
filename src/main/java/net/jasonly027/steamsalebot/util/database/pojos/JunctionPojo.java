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

    // Necessary for POJO codec
    public JunctionPojo() {}

    public JunctionPojo(long appId, long serverId) {
        this.appId = appId;
        this.serverId = serverId;
    }

    @Override
    public Bson toFilter() {
        return Filters.and(
                Filters.eq(Database.APP_ID, appId),
                Filters.eq(Database.SERVER_ID, serverId)
        );
    }

    @Override
    public String toString() {
        return "JunctionPojo{" +
                "id=" + id +
                ", appId=" + appId +
                ", serverId=" + serverId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JunctionPojo junction = (JunctionPojo) o;
        return appId == junction.appId && serverId == junction.serverId && Objects.equals(id, junction.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, appId, serverId);
    }
}
