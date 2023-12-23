package net.jasonly027.steamsalebot.steam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

@JsonDeserialize(using = AppInfo.AppInfoDeserializer.class)
@JsonIgnoreProperties(ignoreUnknown = true)
/*
 * POJO for an app's details. If success is false, the other fields will not be set,
 * otherwise they will be assigned values from deserialization. If isFree is true,
 * the price-related fields will not be set, otherwise they will be assigned values
 * from deserialization.
 */
public class AppInfo {
    private boolean success;
    private boolean isFree;
    private String name;
    private String originalPrice;
    private String salePrice;
    private int discountPercent;
    private int recommendationsCount;
    private String bannerUrl;
    private String storePageUrl;

    public boolean isSuccess() {
        return success;
    }

    public boolean isFree() {
        return isFree;
    }

    public String getName() {
        return name;
    }

    public String getOriginalPrice() {
        return originalPrice;
    }

    public String getSalePrice() {
        return salePrice;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    public int getRecommendationsCount() {
        return recommendationsCount;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public String getStorePageUrl() {
        return storePageUrl;
    }

    private AppInfo setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    private AppInfo setFree(boolean free) {
        isFree = free;
        return this;
    }

    private AppInfo setName(String name) {
        this.name = name;
        return this;
    }

    private AppInfo setOriginalPrice(String originalPrice) {
        this.originalPrice = originalPrice;
        return this;
    }

    private AppInfo setSalePrice(String salePrice) {
        this.salePrice = salePrice;
        return this;
    }

    private AppInfo setDiscountPercent(int discountPercent) {
        this.discountPercent = discountPercent;
        return this;
    }

    private AppInfo setRecommendationsCount(int recommendationsCount) {
        this.recommendationsCount = recommendationsCount;
        return this;
    }

    private AppInfo setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
        return this;
    }

    private AppInfo setStorePageUrl(String storePageUrl) {
        this.storePageUrl = storePageUrl;
        return this;
    }

    @Override
    public String toString() {
        return "AppInfo{" +
                "\nsuccess=" + success +
                ", \nisFree=" + isFree +
                ", \nname=" + name +
                ", \noriginalPrice='" + originalPrice + '\'' +
                ", \nsalePrice='" + salePrice + '\'' +
                ", \ndiscountPercent=" + discountPercent +
                ", \nrecommendationsCount=" + recommendationsCount +
                ", \nbannerUrl='" + bannerUrl + '\'' +
                ", \nstorePageUrl='" + storePageUrl + '\'' +
                '}';
    }

    public static class AppInfoDeserializer extends StdDeserializer<AppInfo> {
        public AppInfoDeserializer() {
            this(null);
        }

        public AppInfoDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public AppInfo deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode root = p.getCodec().readTree(p);
            String appField = root.fieldNames().next();
            root = root.get(appField);

            AppInfo appInfo = new AppInfo();
            appInfo.setSuccess(root.get("success").asBoolean());

            // Do not continue deserialization if unsuccessful, i.e., the app id was invalid
            if (!appInfo.success) {
                return appInfo;
            }

            JsonNode data = root.get("data");
            appInfo.setName(data.get("name").textValue())
                    .setBannerUrl(data.get("header_image").textValue())
                    .setStorePageUrl("https://store.steampowered.com/app/" + data.get("steam_appid").asInt())
                    .setRecommendationsCount(data.get("recommendations").get("total").asInt())
                    .setFree(data.get("is_free").asBoolean());

            // Do not continue deserialization if the app is free, because
            // the "price_overview" field will not exist for free apps.
            if (appInfo.isFree) {
                return appInfo;
            }

            JsonNode priceOverview = data.get("price_overview");
            appInfo.setDiscountPercent(priceOverview.get("discount_percent").asInt())
                    .setOriginalPrice(priceOverview.get("initial_formatted").textValue())
                    .setSalePrice(priceOverview.get("final_formatted").textValue());

            return appInfo;
        }
    }
}
