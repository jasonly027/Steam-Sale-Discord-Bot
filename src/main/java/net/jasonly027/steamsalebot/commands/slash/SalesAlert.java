package net.jasonly027.steamsalebot.commands.slash;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.jasonly027.steamsalebot.steam.AppInfo;

import java.awt.*;

public class SalesAlert {

    public static MessageEmbed createSalesAlertEmbed(AppInfo appName, AppInfo discountPercentage, AppInfo salePrice, AppInfo originalPrice, AppInfo BannerURL, AppInfo storePageURL, AppInfo recommendationsCount) {
        int discountPercentageInt = discountPercentage.getDiscountPercent();
        String appNameString = appName.getName();
        String salePriceString = salePrice.getSalePrice();
        String originalPriceString = originalPrice.getOriginalPrice();
        String BannerURLString = BannerURL.getBannerUrl();
        String storePageURLString = storePageURL.getStorePageUrl();
        String recommendationsCountString = recommendationsCount.getRecommendationsCount() + " recommendations";

        EmbedBuilder builder = new EmbedBuilder()
                .setTitle(appNameString + " is on sale for " + discountPercentageInt + " % off!")
                .setDescription("Link to Steam Page: " + storePageURLString)
                .setColor(getColorBySalePercentage(discountPercentageInt))
                .setThumbnail(BannerURLString)
                .addField("New Price:", salePriceString, true)
                .addField("Original Price:", originalPriceString, true)
                .addField("Recomendations Count:", recommendationsCountString, true);
        return builder.build();
    }

    public static Color getColorBySalePercentage(int discountPercentage) {
        if ((discountPercentage >= 1) && (discountPercentage <= 5)) {
            return new Color(11, 255, 51);
        }
        if ((discountPercentage >= 6) && (discountPercentage <= 10)) {
            return new Color(68, 253, 210);
        }
        if ((discountPercentage >= 11) && (discountPercentage <= 15)) {
            return new Color(68, 253, 253);
        }
        if ((discountPercentage >= 16) && (discountPercentage <= 20)) {
            return new Color(68, 219, 253);
        }
        if ((discountPercentage >= 21) && (discountPercentage <= 25)) {
            return new Color(11, 255, 51);
        }
        if((discountPercentage >= 26) && (discountPercentage <= 30)) {
            return new Color(68, 139, 253);
        }
        if ((discountPercentage >= 31) && (discountPercentage <= 35)) {
            return new Color(68, 90, 253);
        }
        if ((discountPercentage >= 36) && (discountPercentage <= 40)) {
            return new Color(133, 68, 253);
        }
        if((discountPercentage >= 41) && (discountPercentage <= 45)) {
            return new Color(176, 68, 253);
        }
        if((discountPercentage >= 46) && (discountPercentage <= 50)) {
            return new Color(225, 68, 253);
        }
        if((discountPercentage >= 51) && (discountPercentage <= 55)) {
            return new Color(253, 68, 222);
        }
        if((discountPercentage >= 56) && (discountPercentage <=60)){
            return new Color(255, 35, 167);
        }
        if(discountPercentage == 100) {
            return new Color(255, 255, 255);
        }
        if(discountPercentage >= 61) {
            return new Color(255, 0, 0);
        }
        return null;
    }
}
