package net.jasonly027.steamsalebot.util.database;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import net.jasonly027.steamsalebot.App;
import net.jasonly027.steamsalebot.util.database.pojos.AppPojo;
import net.jasonly027.steamsalebot.util.database.pojos.DiscordPojo;
import net.jasonly027.steamsalebot.util.database.pojos.JunctionPojo;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Database {
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
    public static final String IS_TRAILING_SALE_DAY = "is_trailing_sale_day";

    /*
        The database is a singleton, i.e., there is only once instance of it.
       Therefore, the constructor is private, and we have a static instance of it.
       In this case, there are getters to the collections from that static instance.
    */
    private static final Database database = new Database();

    private Database() {
        // Connection information
        final String DB_KEY = App.config.get(("DB_KEY"));
        final String DB_NAME = "SteamSaleBot";
        final String APPS_COLLECTION = "apps";
        final String JUNCTION_COLLECTION = "junction";
        final String DISCORD_COLLECTION = "discord";

        // Register POJOs
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = CodecRegistries.fromProviders(
                MongoClientSettings.getDefaultCodecRegistry(), CodecRegistries.fromProviders(pojoCodecProvider));

        // Establish connection
        MongoClient connection = MongoClients.create(DB_KEY);
        MongoDatabase db = connection.getDatabase(DB_NAME)
                .withCodecRegistry(pojoCodecRegistry);

        // Get collections
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
     * Insert an entry to the <b>discord</b> collection. <i>salesThreshold</i> will be 1
     * by default.
     * @param serverId id of the server
     * @param defaultChannelId id of the default channel
     * @return true if the insertion was successful or if the server
     * is already in the discord collection
     */
    public static boolean addServerToDiscordCollection(long serverId, long defaultChannelId) {
        // Check if server is already in the collection
        Bson filterByServerId = Filters.eq(SERVER_ID, serverId);
        if (getDiscord().find(filterByServerId).first() != null) {
            return true;
        }

        return getDiscord().insertOne(new DiscordPojo(serverId, defaultChannelId)).wasAcknowledged();
    }

    /**
     * Remove all entries in the <b>junction</b> collection with the supplied <i>serverId</i>.
     * Afterward, remove the entry from the <b>discord</b> collection.
     * @param serverId id of the server
     * @return true if the entry is no longer in the <b>discord</b> collection
     */
    public static boolean removeServerFromDiscordCollection(long serverId) {
        removeAppIdsFromAServer(serverId,
                getAppsTrackedByAServer(serverId).stream().map(appPojo -> appPojo.appId).toList());

        Bson filterByServerId = Filters.eq(SERVER_ID, serverId);
        return getDiscord().deleteOne(filterByServerId).wasAcknowledged();
    }

    /**
     * Gets a MongoCursor (iterator) for iterating through all entries in the <b>apps</b> collection.
     * The cursor <b>MUST</b> be closed with its <i>close</i> method, or preferably,
     * used through a try-with-resources statement
     * @return a cursor for iterating through every entry in the <b>apps</b> collection
     */
    public static MongoCursor<AppPojo> getAllAppsCursor() {
        return getApps().find().cursor();
    }

    /**
     * <p>
     * Check if a sale alert should be sent to the specified server. Inverts the value of the
     * <i>isTrailingSaleDay</i> field of a server entry in the <b>discord</b> collection
     * if <i>isTrailingSaleDay</i> and <i>isOnSale</i> are dissimilar.
     * </p>
     * <p>There are two cases where <i>isTrailingSaleDay</i> and <i>isOnSale</i> are dissimilar: </p>
     * <p>1. When it's the first day of a sale or when an app, which was freshly added to a server's
     * tracked list and is currently undergoing a sale, is checked for its first time by that server.
     * In this case, the app <b>is on sale</b> and it <b>is not a trailing sale day</b>.</p>
     * <p>2. When it was previously known to a server that an app was on sale, but on this check,
     * it is no longer on sale. In this case, the app <b>is no longer on sale</b> but it <b>was
     * last known as a trailing sale day</b>.</p>
     * <p>As mentioned earlier in the event of a dissimilarity, the <b>isTrailingSaleDay</b>
     *  field of a server entry is flipped.</p>
     * @param serverId id of the server
     * @param isOnSale whether the app is currently on sale
     * @return true if a sale alert should be sent
     */
    public static boolean shouldSendAlertAndUpdateTrailingSaleDay(long serverId, boolean isOnSale) {
        Bson filterByServerId = Filters.eq(SERVER_ID, serverId);
        DiscordPojo serverInfo = getDiscord().find(filterByServerId).first();
        // Check if the server entry exists
        if (serverInfo == null) {
            return false;
        }

        boolean isTrailingSaleDay = serverInfo.isTrailingSaleDay;
        // An alert should be sent if there's a sale and it's not a trailing sale day
        boolean shouldSendAlert = isOnSale && !isTrailingSaleDay;

        // If dissimilar truth values, invert isTrailingSaleDay and update the entry
        // in the server collection
        if (isOnSale && !isTrailingSaleDay || !isOnSale && isTrailingSaleDay) {
            isTrailingSaleDay = !isTrailingSaleDay;
            Bson fieldToUpdate = Updates.set(IS_TRAILING_SALE_DAY, isTrailingSaleDay);
            getDiscord().updateOne(filterByServerId, fieldToUpdate);
        }

        return shouldSendAlert;
    }

    /**
     * Updates the <i>channelId</i> of an entry in the <b>discord</b> collection,
     * identified by its unique <i>serverId</i>.
     * @param serverId for identifying the entry to update
     * @param newChannelId the new channelId for the entry
     * @return true if the update was successful or if entry doesn't exist
     */
    public static boolean updateChannelIdOfAServer(long serverId, long newChannelId) {
        Bson filterByServerId = Filters.eq(SERVER_ID, serverId);
        Bson fieldToUpdate = Updates.set(CHANNEL_ID, newChannelId);
        return getDiscord().updateOne(filterByServerId, fieldToUpdate).wasAcknowledged();
    }

    // If the app is not in the apps collection, insert it.
    // Otherwise, replace it. The method needs to replace to account for the event
    // of an app being renamed.
    private static void upsertAppIdToAppsCollection(AppPojo appPojo) {
        Bson filterByAppId = Filters.eq(APP_ID, appPojo.appId);
        ReplaceOptions options = new ReplaceOptions().upsert(true);
        getApps().replaceOne(filterByAppId, appPojo, options);
    }

    /**
     * Adds an app to the <b>apps</b> collection if it is not already there.
     * Afterward, adds an entry to the <b>junction</b> collection.
     * @param serverId  id of the server
     * @param appId id of the app
     * @param appName name of the app
     * @return true if insertion to the junction collection was successful or if
     * the entry is already in the junction collection
     */
    public static boolean addAppIdToAServer(long serverId, long appId, String appName) {
        AppPojo appPojo = new AppPojo(appId, appName);
        // Update/Insert app into apps collection
        upsertAppIdToAppsCollection(appPojo);

        // Check if not already in the collection then add to collection
        JunctionPojo entry = new JunctionPojo(appPojo.appId, serverId);
        if (getJunction().find(entry.toFilter()).first() != null) {
            return true;
        }
        return getJunction().insertOne(entry).wasAcknowledged();
    }

    /**
     * Tries to add all the apps from the iterator. If an insertion fails before the
     * iterator is exhausted, the method ends prematurely and <b>unsuccessfulApp</b>
     * is updated.
     * @param serverId id of the server
     * @param appPojoIterator iterator of apps to add
     * @param unsuccessfulApp this POJOs fields will be set by the first app with a failed insertion
     *                        if there's a failure. If there's no failure, appId will be assigned -1.
     * @return a list of successfully added apps. If there were no insertion fails, the list
     * will be the same as the elements from the iterator.
     */
    public static List<AppPojo> addAppIdsToAServer(long serverId, Iterator<AppPojo> appPojoIterator, AppPojo unsuccessfulApp) {
        List<AppPojo> successfullyAddedPojos = new ArrayList<>();
        boolean successfullyAdded = true;
        while (appPojoIterator.hasNext() && successfullyAdded) {
            AppPojo appPojo = appPojoIterator.next();
            // If insertion fails, end method prematurely
            if (!addAppIdToAServer(serverId, appPojo.appId, appPojo.appName)) {
                unsuccessfulApp.appId = appPojo.appId;
                unsuccessfulApp.appName = appPojo.appName;
                successfullyAdded = false;
                continue;
            }
            successfullyAddedPojos.add(appPojo);
        }

        unsuccessfulApp.appId = -1;
        return successfullyAddedPojos;
    }

    // If the appId is not referenced in the junction collection, remove
    // it from the apps collection
    private static void removeNonReferencedAppIdFromAppsCollection(long appId) {
        Bson filterByAppId = Filters.eq(APP_ID, appId);
        if (getJunction().find(filterByAppId).first() == null) {
            getApps().deleteOne(filterByAppId);
        }
    }

    /**
     * Remove an entry from the junction collection, identified by its <i>appId</i>.
     * After removal, if there are no more references to this app in the <b>junction</b> collection,
     * remove it from the <b>apps</b> collection.
     * @param serverId id of the server
     * @param appId id of the app
     * @return true if the entry is no longer in the <b>junction</b> collection
     */
    public static boolean removeAppIdFromAServer(long serverId, long appId) {
        // Delete from junction collection
        DeleteResult result = getJunction().deleteOne(new JunctionPojo(appId, serverId).toFilter());
        // Check for deletion success
        if (!result.wasAcknowledged()) {
            return false;
        }

        // If there are no more references to this app in the junction collection,
        // remove it from the apps collection
        removeNonReferencedAppIdFromAppsCollection(appId);

        return true;
    }

    /**
     * Remove all entries from the <b>junction</b> collection with the supplied <i>serverId</i>.
     * Afterward, remove any apps from the <b>apps</b> collection that do not have anymore
     * references in the <b>junction</b> collection.
     * @param serverId id of the server
     * @param appIds ids of the apps
     * @return true if the entries don't exist in the junction collection anymore
     */
    public static boolean removeAppIdsFromAServer(long serverId, List<Long> appIds) {
        Bson filterByServerIdAndAppIds = Filters.and(
                Filters.eq(SERVER_ID, serverId),
                Filters.in(APP_ID, appIds)
        );
        DeleteResult result = getJunction().deleteMany(filterByServerIdAndAppIds);
        if (!result.wasAcknowledged()) {
            return false;
        }

        for (Long appId: appIds) {
            removeNonReferencedAppIdFromAppsCollection(appId);
        }

        return true;
    }

    /**
     * Updates the <i>saleThreshold</i> of an entry in the <b>discord</b> collection,
     * identified by its unique <i>serverId</i>.
     * @param serverId for identifying the entry to update
     * @param saleThreshold the new salesThreshold for the entry
     * @return true if the update was successful or if entry doesn't exist
     */
    public static boolean updateSaleThresholdOfAServer(long serverId, int saleThreshold) {
        Bson filterByServerId = Filters.eq(SERVER_ID, serverId);
        Bson fieldToUpdate = Updates.set(SALE_THRESHOLD, saleThreshold);
        return getDiscord().updateOne(filterByServerId, fieldToUpdate).wasAcknowledged();
    }

    /**
     * Gets a list of AppPojos by finding <i>appIds</i> from the <b>junction</b> collection
     * with the specified <i>serverId</i> and using those <i>appIds</i> to get their entry
     * in the <b>apps</b> collection
     * @param serverId id of the server
     * @return a list of AppPojos
     */
    public static List<AppPojo> getAppsTrackedByAServer(long serverId) {
        Bson filterByServerId = Filters.eq(SERVER_ID, serverId);
        List<Long> appIds = new ArrayList<>();
        getJunction().find(filterByServerId).map(junctionPojo -> junctionPojo.appId).into(appIds);

        List<AppPojo> appPojos = new ArrayList<>();
        getApps().find(Filters.in(APP_ID, appIds)).into(appPojos);

        return appPojos;
    }
}
