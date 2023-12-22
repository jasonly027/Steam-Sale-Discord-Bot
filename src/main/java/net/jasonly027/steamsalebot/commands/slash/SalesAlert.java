package net.jasonly027.steamsalebot.commands.slash;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.jasonly027.steamsalebot.steam.AppInfo;

import java.awt.*;

public class SalesAlert {

    public static MessageEmbed createSalesAlertEmbed(AppInfo app) {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle(app.getName() + " is on sale for " + app.getDiscountPercent() + " % off!",
                        app.getStorePageUrl())
                .setColor(getColorBySalePercentage(app.getDiscountPercent()))
                .setThumbnail(app.getBannerUrl())
                .addField("New Price:", app.getSalePrice(), true)
                .addField("Original Price:", app.getOriginalPrice(), true)
                .addField("Recommendations:", String.valueOf(app.getRecommendationsCount()), true);
        return builder.build();
    }

    public static Color getColorBySalePercentage(int discountPercentage) {
        if ((discountPercentage >= 1) && (discountPercentage <= 5)) {
            return new Color(11, 255, 51);
        }
        else if ((discountPercentage >= 6) && (discountPercentage <= 10)) {
            return new Color(68, 253, 210);
        }
        else if ((discountPercentage >= 11) && (discountPercentage <= 15)) {
            return new Color(68, 253, 253);
        }
        else if ((discountPercentage >= 16) && (discountPercentage <= 20)) {
            return new Color(68, 219, 253);
        }
        else if ((discountPercentage >= 21) && (discountPercentage <= 25)) {
            return new Color(11, 255, 51);
        }
        else if((discountPercentage >= 26) && (discountPercentage <= 30)) {
            return new Color(68, 139, 253);
        }
        else if ((discountPercentage >= 31) && (discountPercentage <= 35)) {
            return new Color(68, 90, 253);
        }
        else if ((discountPercentage >= 36) && (discountPercentage <= 40)) {
            return new Color(133, 68, 253);
        }
        else if((discountPercentage >= 41) && (discountPercentage <= 45)) {
            return new Color(176, 68, 253);
        }
        else if((discountPercentage >= 46) && (discountPercentage <= 50)) {
            return new Color(225, 68, 253);
        }
        else if((discountPercentage >= 51) && (discountPercentage <= 55)) {
            return new Color(253, 68, 222);
        }
        else if((discountPercentage >= 56) && (discountPercentage <=60)){
            return new Color(255, 35, 167);
        }
        else if((discountPercentage >= 61) && (discountPercentage < 100)) {
            return new Color(255, 0, 0);
        }
        else {
            return new Color(255, 255, 255);
        }
    }
}
