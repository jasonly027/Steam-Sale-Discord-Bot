package net.jasonly027.steamsalebot.util.database;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import net.jasonly027.steamsalebot.App;
import net.jasonly027.steamsalebot.util.database.pojos.AppPojo;
import net.jasonly027.steamsalebot.util.database.pojos.DiscordPojo;
import net.jasonly027.steamsalebot.util.database.pojos.JunctionPojo;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

public class Database {
    // Connection information
    private static final String DB_KEY = App.config.get(("DB_KEY"));
    private static final String DB_NAME = "SteamSaleBot";
    private static final String APPS_COLLECTION = "apps";
    private static final String JUNCTION_COLLECTION = "junction";
    private static final String DISCORD_COLLECTION = "discord";

    // Collections, i.e., tables
    private final MongoCollection<AppPojo> apps;
    private final MongoCollection<JunctionPojo> junction;
    private final MongoCollection<DiscordPojo> discord;

    // Collection fields
    public static final String APP_ID = "app_id";
    public static final String APP_NAME = "app_name";

    public static final String SERVER_ID = "server_id";
    public static final String CHANNEL_ID = "channel_id";
    public static final String SALE_THRESHOLD = "sale_threshold";

    /*
        The database is a singleton, i.e., there is only once instance of it.
       Therefore, the constructor is private, and we have a static instance of it.
       In this case, there are getters to the collections from that static instance.
    */
    private static final Database database = new Database();

    private Database() {
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = CodecRegistries.fromProviders(
                MongoClientSettings.getDefaultCodecRegistry(), CodecRegistries.fromProviders(pojoCodecProvider));

        MongoClient connection = MongoClients.create(DB_KEY);
        MongoDatabase db = connection.getDatabase(DB_NAME)
                .withCodecRegistry(pojoCodecRegistry);

        apps = db.getCollection(APPS_COLLECTION, AppPojo.class);
        junction = db.getCollection(JUNCTION_COLLECTION, JunctionPojo.class);
        discord = db.getCollection(DISCORD_COLLECTION, DiscordPojo.class);
    }

    private static MongoCollection<AppPojo> getApps() {
        return database.apps;
    }

    private static MongoCollection<JunctionPojo> getJunction() {
        return database.junction;
    }

    private static MongoCollection<DiscordPojo> getDiscord() {
        return database.discord;
    }

    /**
     * Updates the <i>channelId</i> of an entry in the <b>discord</b> collection,
     * identified by its unique <i>serverId</i>.
     * @param serverId for identifying the entry to update
     * @param newChannelId the new channelId for the entry
     * @return true if the update was successful
     */
    public static boolean updateChannelId(long serverId, long newChannelId) {
        Bson filterByServer = Filters.eq(SERVER_ID, serverId);
        Bson fieldToUpdate = Updates.set(CHANNEL_ID, newChannelId);
        return getDiscord().updateOne(filterByServer, fieldToUpdate).wasAcknowledged();
    }

    /**
     * Adds an app to the <b>apps</b> collection if it is not already there.
     * Afterward, adds an entry to the <b>junction</b> collection.
     * @param serverId  id of the server
     * @param appId id of the app
     * @param appName name of the app
     * @return true if the insertion to both collections was successful. Returns
     * false if <i>appId</i> is already in the <b>junction</b> collection or if
     * there is a database insertion error
     */
    public static boolean addAppId(long serverId, long appId, String appName) {
        // Add to apps collection if it's a new appId
        Bson filterByAppId = Filters.eq(APP_ID, appId);
        if (getApps().find(filterByAppId).first() == null) {
            InsertOneResult result = getApps().insertOne(new AppPojo(appId, appName));
            // Check for insertion success
            if (!result.wasAcknowledged()) {
                return false;
            }
        }

        // Add to junction collection
        JunctionPojo entry = new JunctionPojo(appId, serverId);
        // Check if already in collection
        if (getJunction().find(entry.toFilter()).first() != null) {
            return false;
        }
        return getJunction().insertOne(entry).wasAcknowledged();
    }

    // TODO: addAppIds

    /**
     * Remove an entry from the junction collection, identified by its <i>appId</i>.
     * After removal, if there are no more references to this app in the <b>junction</b> collection,
     * remove it from the <b>apps</b> collection.
     * @param serverId id of the server
     * @param appId id of the app
     * @return true if removal from the junction collection was successful
     */
    public static boolean removeAppId(long serverId, long appId) {
        // Delete from junction collection
        DeleteResult result = getJunction().deleteOne(new JunctionPojo(appId, serverId).toFilter());
        // Check for deletion success
        if (!result.wasAcknowledged()) {
            return false;
        }

        // If there are no more references to this app in the junction collection,
        // remove it from the apps collection
        Bson filterByAppId = Filters.eq(APP_ID, appId);
        if (getJunction().find(filterByAppId).first() == null) {
            getApps().deleteOne(filterByAppId);
        }

        return true;
    }

    // TODO: removeAppIds

    /**
     * Updates the <i>saleThreshold</i> of an entry in the <b>discord</b> collection,
     * identified by its unique <i>serverId</i>.
     * @param serverId for identifying the entry to update
     * @param saleThreshold the new salesThreshold for the entry
     * @return true if the update was successful
     */
    public static boolean updateSaleThreshold(long serverId, int saleThreshold) {
        Bson filterByServerId = Filters.eq(SERVER_ID, serverId);
        Bson fieldToUpdate = Updates.set(SALE_THRESHOLD, saleThreshold);
        return getDiscord().updateOne(filterByServerId, fieldToUpdate).wasAcknowledged();
    }
}
