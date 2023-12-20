package net.jasonly027.steamsalebot.util.database.pojos;

import com.mongodb.client.model.Filters;
import net.jasonly027.steamsalebot.util.database.Database;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.Objects;

public class DiscordPojo implements IToFilter {
    // When creating your own object, DO NOT assign to this id. Keep it null.
    public ObjectId id;

    @BsonProperty(value = Database.SERVER_ID)
    public long serverId;

    @BsonProperty(value = Database.CHANNEL_ID)
    public long channelId;

    @BsonProperty(value = Database.SALE_THRESHOLD)
    public int salesThreshold;

    @BsonProperty(value = Database.IS_TRAILING_SALE_DAY)
    public boolean isTrailingSaleDay;

    // Necessary for POJO codec
    public DiscordPojo() {}

    public DiscordPojo(long serverId, long channelId) {
        this(serverId, channelId, 1, false);
    }

    public DiscordPojo(long serverId, long channelId, int salesThreshold, boolean isTrailingSaleDay) {
        this.serverId = serverId;
        this.channelId = channelId;
        this.salesThreshold = salesThreshold;
        this.isTrailingSaleDay = isTrailingSaleDay;
    }

    @Override
    public Bson toFilter() {
        return Filters.and(
                Filters.eq(Database.SERVER_ID, serverId),
                Filters.eq(Database.CHANNEL_ID, channelId),
                Filters.eq(Database.SALE_THRESHOLD, salesThreshold),
                Filters.eq(Database.IS_TRAILING_SALE_DAY, isTrailingSaleDay)
        );
    }

    @Override
    public String toString() {
        return "DiscordPojo{" +
                "id=" + id +
                ", serverId=" + serverId +
                ", channelId=" + channelId +
                ", salesThreshold=" + salesThreshold +
                ", isTrailingSaleDay=" + isTrailingSaleDay +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiscordPojo that = (DiscordPojo) o;
        return serverId == that.serverId && channelId == that.channelId
                && salesThreshold == that.salesThreshold && isTrailingSaleDay == that.isTrailingSaleDay
                && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, serverId, channelId, salesThreshold, isTrailingSaleDay);
    }
}
