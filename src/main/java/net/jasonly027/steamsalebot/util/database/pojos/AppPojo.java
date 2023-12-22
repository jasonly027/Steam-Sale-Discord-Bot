package net.jasonly027.steamsalebot.util.database.pojos;

import com.mongodb.client.model.Filters;
import net.jasonly027.steamsalebot.util.database.Database;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.Objects;

public class AppPojo implements IToFilter {
    // When creating your own object, DO NOT assign to this id. Keep it null.
    public ObjectId id;

    @BsonProperty(value = Database.APP_ID)
    public long appId;

    @BsonProperty(value = Database.APP_NAME)
    public String appName;

    // Necessary for POJO codec
    public AppPojo() {}

    public AppPojo(long appId, String appName) {
        this.appId = appId;
        this.appName = appName;
    }

    @Override
    public Bson toFilter() {
        return Filters.and(
                Filters.eq(Database.APP_ID, appId),
                Filters.eq(Database.APP_NAME, appName)
        );
    }

    @Override
    public String toString() {
        return appName + " (" + appId + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppPojo appPojo = (AppPojo) o;
        return appId == appPojo.appId && Objects.equals(id, appPojo.id) && Objects.equals(appName, appPojo.appName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, appId, appName);
    }
}
